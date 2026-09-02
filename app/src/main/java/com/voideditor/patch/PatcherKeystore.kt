package com.voideditor.patch

import android.content.Context
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Date

object PatcherKeystore {

    private const val Alias = "editores-patcher"
    private const val Password = "editores-patcher"
    private const val ValidityMillis = 30L * 365L * 24L * 60L * 60L * 1000L

    fun entry(context: Context): KeyStore.PrivateKeyEntry {
        val file = File(context.filesDir, "patcher.keystore")
        val password = Password.toCharArray()
        if (file.isFile) {
            val store = KeyStore.getInstance("PKCS12")
            FileInputStream(file).use { store.load(it, password) }
            return store.getEntry(Alias, KeyStore.PasswordProtection(password)) as KeyStore.PrivateKeyEntry
        }
        val provider = BouncyCastleProvider()
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048, SecureRandom())
        val pair = generator.generateKeyPair()
        val notBefore = Date()
        val notAfter = Date(notBefore.time + ValidityMillis)
        val subject = X500Name("CN=EditorEs Patcher,O=EditorEs,C=US")
        val serial = BigInteger(63, SecureRandom())
        val builder = JcaX509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, pair.public)
        val signer = JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(pair.private)
        val certificate = JcaX509CertificateConverter().setProvider(provider).getCertificate(builder.build(signer))
        val store = KeyStore.getInstance("PKCS12")
        store.load(null, password)
        store.setEntry(Alias, KeyStore.PrivateKeyEntry(pair.private, arrayOf(certificate)), KeyStore.PasswordProtection(password))
        FileOutputStream(file).use { store.store(it, password) }
        return store.getEntry(Alias, KeyStore.PasswordProtection(password)) as KeyStore.PrivateKeyEntry
    }
}
