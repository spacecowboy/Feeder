package com.nononsenseapps.feeder.localtranslation

import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.filePathProvider
import io.airlift.compress.zstd.ZstdOutputStream
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class BergamotModelManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val server = MockWebServer()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun prepareDownloadsDirectLanguagePairAndDeletesIt() =
        runTest {
            server.start()
            val model = "model".toByteArray()
            val lex = "lex".toByteArray()
            val vocab = "vocab".toByteArray()
            val registry = registry("deen", model = model, lex = lex, vocab = vocab)
            server.enqueue(MockResponse().setResponseCode(200).setBody(registry))
            server.enqueue(MockResponse().setResponseCode(200).setBody(String(model)))
            server.enqueue(MockResponse().setResponseCode(200).setBody(String(lex)))
            server.enqueue(MockResponse().setResponseCode(200).setBody(String(vocab)))

            val manager = modelManager()
            val preparation = manager.prepare(sourceLanguage = "German", targetLanguage = "English")

            assertTrue(preparation.toString(), preparation is BergamotModelPreparation.Ready)
            val ready = preparation as BergamotModelPreparation.Ready
            assertEquals(listOf("de" to "en"), ready.modelRegistry.map { it.from to it.to })
            assertTrue(
                ready.modelRegistry
                    .single()
                    .files.values
                    .all { it.url?.startsWith("file:") == true },
            )
            assertEquals(BergamotLanguagePairStatus.Downloaded, manager.languagePairStatus("de", "en"))
            assertNull(manager.downloadProgress.value)

            val progressEvents = mutableListOf<BergamotModelDownloadProgress>()
            val progressCollector =
                backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    manager.downloadProgress.filterNotNull().toList(progressEvents)
                }

            val cachedPreparation = manager.prepare(sourceLanguage = "German", targetLanguage = "English")

            progressCollector.cancel()
            assertTrue(cachedPreparation is BergamotModelPreparation.Ready)
            assertTrue(progressEvents.isEmpty())
            assertNull(manager.downloadProgress.value)

            manager.deleteLanguagePair("de", "en")

            assertEquals(BergamotLanguagePairStatus.AvailableToDownload, manager.languagePairStatus("de", "en"))
        }

    @Test
    fun prepareDownloadsCurrentMozillaSwedishModel() =
        runTest {
            server.start()
            val model = "swedish model".toByteArray()
            val lex = "swedish lex".toByteArray()
            val vocab = "swedish vocab".toByteArray()
            val compressedFiles = listOf(model, lex, vocab).map(::zstdCompress)
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(mozillaRegistry("sv", "en", model, lex, vocab, compressedFiles)),
            )
            compressedFiles.forEach { content ->
                server.enqueue(
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(okio.Buffer().write(content)),
                )
            }

            val manager = modelManager()
            val preparation = manager.prepare(sourceLanguage = "Swedish", targetLanguage = "English")

            assertTrue(preparation.toString(), preparation is BergamotModelPreparation.Ready)
            assertEquals(BergamotLanguagePairStatus.Downloaded, manager.languagePairStatus("sv", "en"))
            val files = (preparation as BergamotModelPreparation.Ready).modelRegistry.single().files
            assertEquals(setOf("model", "lex", "vocab"), files.keys)
            assertTrue(files.values.all { it.url?.startsWith("file:") == true })
            assertEquals(model.size.toLong(), manager.pairDir("sv", "en").resolve("model.sven.intgemm.alphas.bin").length())
        }

    @Test
    fun downloadRuntimeStoresVerifiedExecutable() =
        runTest {
            server.start()
            val runtime = "verified runtime".toByteArray()
            server.enqueue(MockResponse().setResponseCode(200).setBody(String(runtime)))

            val manager = modelManager(runtimeSha256 = sha256(runtime))

            assertTrue(manager.downloadRuntime())
            assertTrue(manager.isRuntimeDownloaded())
            assertTrue(manager.runtimeFileUrl()?.startsWith("file:") == true)
            assertNull(manager.downloadProgress.value)
        }

    @Test
    fun downloadRuntimeRejectsExecutableWithWrongHash() =
        runTest {
            server.start()
            server.enqueue(MockResponse().setResponseCode(200).setBody("corrupted runtime"))

            val manager = modelManager(runtimeSha256 = sha256("expected runtime".toByteArray()))

            assertTrue(!manager.downloadRuntime())
            assertTrue(!manager.isRuntimeDownloaded())
            assertNull(manager.runtimeFileUrl())
            assertNull(manager.downloadProgress.value)
        }

    @Test
    fun downloadRuntimeDoesNotFetchVerifiedExecutableAgain() =
        runTest {
            server.start()
            val runtime = "verified runtime".toByteArray()
            server.enqueue(MockResponse().setResponseCode(200).setBody(String(runtime)))
            val manager = modelManager(runtimeSha256 = sha256(runtime))

            assertTrue(manager.downloadRuntime())
            assertTrue(manager.downloadRuntime())

            assertEquals(1, server.requestCount)
        }

    @Test
    fun prepareReportsProgressWhileLoadingRegistry() =
        runTest {
            server.start()
            val model = "model".toByteArray()
            val registry = registry("deen", model = model)
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(registry)
                    .setBodyDelay(200, TimeUnit.MILLISECONDS),
            )
            listOf(model, "lex".toByteArray(), "vocab".toByteArray())
                .forEach { server.enqueue(MockResponse().setResponseCode(200).setBody(String(it))) }

            val manager = modelManager()
            val progress = async { withTimeout(1_000L) { manager.downloadProgress.filterNotNull().first() } }
            val preparation = async(Dispatchers.IO) { manager.prepare(sourceLanguage = "de", targetLanguage = "en") }
            val emittedProgress = progress.await()

            assertEquals("de", emittedProgress.sourceLanguage)
            assertEquals("en", emittedProgress.targetLanguage)
            assertTrue(emittedProgress.isIndeterminate)
            assertEquals("registry-v2.json", emittedProgress.fileName)
            assertTrue(preparation.await() is BergamotModelPreparation.Ready)
            assertNull(manager.downloadProgress.value)
        }

    @Test
    fun prepareUsesEnglishPivotWhenDirectLanguagePairIsUnavailable() =
        runTest {
            server.start()
            val deEnModel = "de-en-model".toByteArray()
            val enFrModel = "en-fr-model".toByteArray()
            val registry =
                buildString {
                    append("{")
                    append(registryEntry("deen", model = deEnModel))
                    append(",")
                    append(registryEntry("enfr", model = enFrModel))
                    append("}")
                }
            server.enqueue(MockResponse().setResponseCode(200).setBody(registry))
            listOf(deEnModel, "lex".toByteArray(), "vocab".toByteArray(), enFrModel, "lex".toByteArray(), "vocab".toByteArray())
                .forEach { server.enqueue(MockResponse().setResponseCode(200).setBody(String(it))) }

            val preparation = modelManager().prepare(sourceLanguage = "de", targetLanguage = "fr")

            assertTrue(preparation is BergamotModelPreparation.Ready)
            val ready = preparation as BergamotModelPreparation.Ready
            assertEquals(listOf("de" to "en", "en" to "fr"), ready.modelRegistry.map { it.from to it.to })
        }

    @Test
    fun languagePairStatusReportsRegistryMissingWhenRegistryCannotBeLoaded() =
        runTest {
            server.start()
            server.enqueue(MockResponse().setResponseCode(404))

            assertEquals(BergamotLanguagePairStatus.RegistryMissing, modelManager().languagePairStatus("de", "en"))
        }

    @Test
    fun languagePairStatusReportsUnavailableWhenRegistryHasNoPath() =
        runTest {
            server.start()
            server.enqueue(MockResponse().setResponseCode(200).setBody(registry("fren", model = "model".toByteArray())))

            assertEquals(BergamotLanguagePairStatus.Unavailable, modelManager().languagePairStatus("de", "fr"))
        }

    @Test
    fun prepareFailsWhenDownloadedModelHashDoesNotMatchRegistry() =
        runTest {
            server.start()
            val registry = registry("deen", model = "expected".toByteArray())
            server.enqueue(MockResponse().setResponseCode(200).setBody(registry))
            server.enqueue(MockResponse().setResponseCode(200).setBody("wrong"))

            val preparation = modelManager().prepare(sourceLanguage = "de", targetLanguage = "en")

            assertTrue(preparation is BergamotModelPreparation.Error)
            assertEquals(
                BergamotModelPreparation.ErrorReason.DownloadFailed,
                (preparation as BergamotModelPreparation.Error).reason,
            )
        }

    @Test
    fun prepareFailsWhenRegistryFileHasNoExpectedHash() =
        runTest {
            server.start()
            val registry =
                """
                {
                  "deen": {
                    "model": {
                      "name": "${server.url("/deen/model.bin")}",
                      "size": 5
                    }
                  }
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(registry))

            val preparation = modelManager().prepare(sourceLanguage = "de", targetLanguage = "en")

            assertTrue(preparation is BergamotModelPreparation.Error)
        }

    private fun modelManager(runtimeSha256: String = sha256("runtime".toByteArray())): BergamotModelManager =
        BergamotModelManager(
            di =
                DI {
                    bind<FilePathProvider>() with
                        singleton {
                            filePathProvider(
                                cacheDir = temporaryFolder.newFolder(),
                                filesDir = temporaryFolder.newFolder(),
                            )
                        }
                    bind<OkHttpClient>() with singleton { OkHttpClient() }
                },
            registryUrl = server.url("/registry.json").toString(),
            attachmentBaseUrl = server.url("/").toString(),
            runtimeUrl = server.url("/bergamot-translator-worker.wasm").toString(),
            runtimeSha256 = runtimeSha256,
        )

    private fun mozillaRegistry(
        from: String,
        to: String,
        model: ByteArray,
        lex: ByteArray,
        vocab: ByteArray,
        compressedFiles: List<ByteArray>,
    ): String {
        val files =
            listOf(
                Triple("model", "model.${from}$to.intgemm.alphas.bin", model),
                Triple("lex", "lex.50.50.${from}$to.s2t.bin", lex),
                Triple("vocab", "vocab.${from}$to.spm", vocab),
            )
        return files
            .mapIndexed { index, (fileType, name, content) ->
                val compressed = compressedFiles[index]
                """
                {
                  "name": "$name",
                  "version": "3.0",
                  "fileType": "$fileType",
                  "architecture": "tiny",
                  "sourceLanguage": "$from",
                  "targetLanguage": "$to",
                  "decompressedHash": "${sha256(content)}",
                  "decompressedSize": ${content.size},
                  "attachment": {
                    "hash": "${sha256(compressed)}",
                    "size": ${compressed.size},
                    "location": "$from$to/$name.zst"
                  }
                }
                """.trimIndent()
            }.joinToString(prefix = "{\"data\":[", postfix = "]}")
    }

    private fun zstdCompress(content: ByteArray): ByteArray =
        ByteArrayOutputStream().use { output ->
            ZstdOutputStream(output).use { it.write(content) }
            output.toByteArray()
        }

    private fun registry(
        pair: String,
        model: ByteArray,
        lex: ByteArray = "lex".toByteArray(),
        vocab: ByteArray = "vocab".toByteArray(),
    ): String = "{${registryEntry(pair, model = model, lex = lex, vocab = vocab)}}"

    private fun registryEntry(
        pair: String,
        model: ByteArray,
        lex: ByteArray = "lex".toByteArray(),
        vocab: ByteArray = "vocab".toByteArray(),
    ): String =
        """
        "$pair": {
          "model": ${fileJson("$pair/model.bin", model)},
          "lex": ${fileJson("$pair/lex.bin", lex)},
          "vocab": ${fileJson("$pair/vocab.spm", vocab)}
        }
        """.trimIndent()

    private fun fileJson(
        path: String,
        content: ByteArray,
    ): String =
        """
        {
          "name": "${server.url("/$path")}",
          "size": ${content.size},
          "expectedSha256Hash": "${sha256(content)}"
        }
        """.trimIndent()

    private fun sha256(content: ByteArray): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(content)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
