package com.nononsenseapps.feeder.crypto

import com.nononsenseapps.feeder.di.cryptoModule
import kotlin.test.assertEquals
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class AlanTest : DIAware {
    override val di: DI by DI.lazy {
        import(cryptoModule)
    }

    private val alan: Alan by instance()

    @Test
    fun testBackAndForth() {
        val aliceKeys = alan.generateKeys()
        val bobKeys = alan.generateKeys()

        // Alice sends message to Bob
        alan.encryptMessage(
            "Hi Bob!".encodeToByteArray(),
            bobKeys.publicKey,
            aliceKeys.secretKey,
        ).let { encryptedMessage ->
            // Bob receives it
            val message = alan.decryptMessage(
                encryptedMessage,
                aliceKeys.publicKey,
                bobKeys.secretKey,
            )

            assertEquals("Hi Bob!", message.decodeToString())
        }

        // Bob then sends a reply to Alice
        alan.encryptMessage(
            "Hi Alice!".encodeToByteArray(),
            aliceKeys.publicKey,
            bobKeys.secretKey,
        ).let { encryptedMessage ->
            // Alice receives it
            val message = alan.decryptMessage(
                encryptedMessage,
                bobKeys.publicKey,
                aliceKeys.secretKey,
            )

            assertEquals("Hi Alice!", message.decodeToString())
        }
    }
}
