package net.fehmicansaglam.tepkin.util

import java.security.MessageDigest
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import javax.crypto.{Mac, SecretKeyFactory}

trait Crypto extends Codec {

  def sha1(value: String): String = {
    val digest = sha1(value.getBytes("UTF-8"))
    encodeBase64(digest)
  }

  def sha1(value: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("SHA-1").digest(value)
  }

  def hmac(value: Array[Byte], key: String): Array[Byte] = {
    val signingKey = new SecretKeySpec(value, "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signingKey)
    mac.doFinal(decodeUtf8(key))
  }

  def keyDerive(password: String, salt: Array[Byte], iterations: Int): Array[Byte] = {
    val spec = new PBEKeySpec(password.toCharArray, salt, iterations, 20 * 8 /* 20 bytes */)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    keyFactory.generateSecret(spec).getEncoded
  }

  def xor(as: Array[Byte], bs: Array[Byte]): Array[Byte] = {
    as.zip(bs).map { case (a, b) => (a ^ b).toByte }
  }
}

object Crypto extends Crypto
