package com.hanhuy.android.common

import android.util.Log
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Try

/**
 * @author pfnguyen
 */
object Futures {
  object CurrentThread extends ExecutionContext {
    override def execute(runnable: Runnable) = runnable.run()
    override def reportFailure(cause: Throwable) = Log.w(
      "Futures", cause.getMessage, cause)
  }
  object MainThread extends ExecutionContext {
    override def execute(runnable: Runnable) = UiBus.run(runnable.run())
    override def reportFailure(cause: Throwable) = throw cause
  }

  implicit object AsyncThread extends ExecutionContext {
    override def execute(runnable: Runnable) =
      _threadpool.execute(runnable)
    override def reportFailure(cause: Throwable) = Log.w(
      "Futures", cause.getMessage, cause)
  }

  implicit class RichFuturesType(val f: Future.type) extends AnyVal {
    /** run on the UI thread immediately if on UI thread, otherwise post to UI */
    @inline final def main[A](b: => A) = f.apply(b)(MainThread)
    // ensure posting at the end of the event queue, rather than
    // running immediately if currently on the main thread
    /** run on the UI thread asynchronously regardless of current thread */
    @inline final def mainEx[A](b: => A) = f.apply(b)(iota.std.MainThreadExecutionContext)
  }

  implicit class RichFutures[T](val f: Future[T]) extends AnyVal {
    type S[U] = PartialFunction[T,U]
    type F[U] = PartialFunction[Throwable,U]
    type C[U] = Try[T] => U
    @inline final def onSuccessHere[U]  = f.onSuccess( _: S[U])(CurrentThread)
    @inline final def onFailureHere[U]  = f.onFailure( _: F[U])(CurrentThread)
    @inline final def onCompleteHere[U] = f.onComplete(_: C[U])(CurrentThread)
    @inline final def onSuccessMain[U]  = f.onSuccess( _: S[U])(MainThread)
    @inline final def onFailureMain[U]  = f.onFailure( _: F[U])(MainThread)
    @inline final def onCompleteMain[U] = f.onComplete(_: C[U])(MainThread)

    @inline final def ~[A >: T](next: => Future[A]): Future[A] = f.flatMap(_ => next)
  }
  def traverseO[A, B](o: Option[A])(f: A => Future[B])(implicit ev: ExecutionContext): Future[Option[B]] =
    (o map f).fold(Future.successful(Option.empty[B]))(_.flatMap(x => Future.successful(Some(x)))(ev))
  def sequenceO[A](o: Option[Future[A]])(implicit ev: ExecutionContext): Future[Option[A]] = traverseO(o)(identity)(ev)
}
