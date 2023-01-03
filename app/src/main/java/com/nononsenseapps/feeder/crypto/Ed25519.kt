package com.nononsenseapps.feeder.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.exceptions.SodiumException
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.KeyPair
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class Alan(override val di: DI) : DIAware {
    private val lazySodium: LazySodiumAndroid by instance()

    /**
     * @return returns a newly generated key pair
     */
    fun generateKeys(): KeyPair =
        lazySodium.cryptoBoxKeypair()

    /**
     * @param messageBytes the data you wish to encrypt
     * @param publicKey the public key of the receiver
     * @param secretKey the secret key of the sender
     * @return an encrypted message
     */
    fun encryptMessage(
        messageBytes: ByteArray,
        publicKey: ByteArray,
        secretKey: ByteArray,
    ): EncryptedMessage {
        val nonce = lazySodium.nonce(Box.NONCEBYTES)
        val cipherBytes = ByteArray(Box.MACBYTES + messageBytes.size)
        val res: Boolean = lazySodium.cryptoBoxEasy(
            cipherBytes,
            messageBytes,
            messageBytes.size.toLong(),
            nonce,
            publicKey,
            secretKey,
        )
        if (!res) {
            throw SodiumException("Could not encrypt your message.")
        }

        return EncryptedMessage(
            cipherBytes = cipherBytes,
            nonce = nonce,
        )
    }

    /**
     * @param encryptedMessage the ciphertext and nonce
     * @param publicKey the public key of the sender
     * @param secretKey the secret key of the receiver
     * @return decrypted message
     */
    fun decryptMessage(
        encryptedMessage: EncryptedMessage,
        publicKey: ByteArray,
        secretKey: ByteArray,
    ): ByteArray {
        val cipher: ByteArray = encryptedMessage.cipherBytes
        val message = ByteArray(cipher.size - Box.MACBYTES)
        val res: Boolean = lazySodium.cryptoBoxOpenEasy(
            message,
            cipher,
            cipher.size.toLong(),
            encryptedMessage.nonce,
            publicKey,
            secretKey,
        )

        if (!res) {
            throw SodiumException("Could not decrypt your message.")
        }

        return message
    }
}

class EncryptedMessage(
    val cipherBytes: ByteArray,
    val nonce: ByteArray,
)
