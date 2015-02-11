package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.io.{IO, Tcp}
import net.fehmicansaglam.tepkin.protocol.message.Message


class MongoPool(host: String, port: Int) extends Actor with Stash {

  import context.system

  val manager = IO(Tcp)
  val remote = new InetSocketAddress(host, port)
  var idles = Seq.empty[ActorRef]

  (1 to 20) foreach { i =>
    context.actorOf(Props(classOf[MongoConnection], manager, remote), s"connection-$i")
  }

  def receive = {
    case "Idle" =>
      idles :+= sender()
//      println(s"idles: $idles")
      unstashAll()

    case m: Message =>
      if (idles.isEmpty) {
//        context.actorOf(Props(classOf[MongoConnection], manager, remote))
        stash()
      } else {
        idles.head.forward(m)
        idles = idles.tail
      }
  }
}
