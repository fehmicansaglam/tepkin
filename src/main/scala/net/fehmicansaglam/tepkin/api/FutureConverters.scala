package net.fehmicansaglam.tepkin.api

import java.util.concurrent.CompletableFuture

import scala.concurrent.{ExecutionContext, Future}

object FutureConverters {

  def toJava[T](f: Future[T])(ec: ExecutionContext): CompletableFuture[T] = {
    val cf = new ExtendedCompletableFuture[T]
    f.onComplete(cf)(ec)
    cf
  }

}
