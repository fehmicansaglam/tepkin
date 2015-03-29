package net.fehmicansaglam.tepkin.util

import java.util.concurrent.ThreadLocalRandom

trait Randomizer {

  def random: ThreadLocalRandom = ThreadLocalRandom.current()

  def randomString(alphabet: String)(n: Int): String = {
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
  }

  def randomString(n: Int): String = {
    randomString {
      """!"#$%&'()*+-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~"""
    }(n)
  }
}
