package net.fehmicansaglam.tepkin.api

import java.util.Optional
import java.util.concurrent.CompletableFuture

import scala.concurrent.{ExecutionContext, Future}

object JavaConverters {

  def toCompletableFuture[T](f: Future[T])(implicit ec: ExecutionContext): CompletableFuture[T] = {
    val cf = new ExtendedCompletableFuture[T]
    f.onComplete(cf)(ec)
    cf
  }

  def toOptional[T]: PartialFunction[Option[T], Optional[T]] = {
    case Some(document) => Optional.of[T](document)
    case None => Optional.empty[T]()
  }

  implicit def toOption[T](optional: Optional[T]): Option[T] = {
    if (optional.isPresent()) Some(optional.get()) else None
  }

}
