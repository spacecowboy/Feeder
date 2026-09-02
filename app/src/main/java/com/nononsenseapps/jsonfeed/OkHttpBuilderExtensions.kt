package com.nononsenseapps.jsonfeed

import okhttp3.OkHttpClient
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.trustAllCerts() {
    try {
        val trustManager =
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        val sslSocketFactory = ProtocolFilteringSslSocketFactory(sslContext.socketFactory)

        sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
    } catch (e: NoSuchAlgorithmException) {
        // ignore
    } catch (e: KeyManagementException) {
        // ignore
    }
}

internal fun filterLegacyTlsProtocols(enabledProtocols: Array<String>): Array<String> {
    val filteredProtocols = enabledProtocols.filterNot(::isLegacyTlsProtocol).toTypedArray()
    if (filteredProtocols.isEmpty()) {
        throw IOException("No acceptable TLS protocols remain enabled")
    }
    return filteredProtocols
}

private fun isLegacyTlsProtocol(protocol: String): Boolean =
    protocol.startsWith(prefix = "SSL", ignoreCase = true) ||
        protocol.equals("TLSv1", ignoreCase = true) ||
        protocol.equals("TLSv1.0", ignoreCase = true) ||
        protocol.equals("TLSv1.1", ignoreCase = true)

private class ProtocolFilteringSslSocketFactory(
    private val delegate: SSLSocketFactory,
) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(): Socket = configure(delegate.createSocket())

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket =
        configure(delegate.createSocket(host, port))

    @Throws(IOException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int,
    ): Socket = configure(delegate.createSocket(host, port, localHost, localPort))

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket =
        configure(delegate.createSocket(host, port))

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = configure(delegate.createSocket(address, port, localAddress, localPort))

    @Throws(IOException::class)
    override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket =
        configure(delegate.createSocket(socket, host, port, autoClose))

    private fun configure(socket: Socket): Socket {
        if (socket is SSLSocket) {
            try {
                socket.enabledProtocols = filterLegacyTlsProtocols(socket.enabledProtocols)
            } catch (e: IOException) {
                try {
                    socket.close()
                } catch (_: IOException) {
                    // Preserve the protocol-filtering failure.
                }
                throw e
            }
        }
        return socket
    }
}