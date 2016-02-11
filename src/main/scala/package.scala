package com.hanhuy.android

import android.annotation.TargetApi
import android.content.{Context, IntentFilter}
import android.os.{Looper, Build}
import android.text.{SpannableStringBuilder, Spanned}

import language.implicitConversions
import language.postfixOps

package object common {
  final val gingerbreadAndNewer = iota.v(Build.VERSION_CODES.GINGERBREAD)
  final val honeycombAndNewer   = iota.v(Build.VERSION_CODES.HONEYCOMB)
  final val icsAndNewer         = iota.v(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  final val jellybeanAndNewer   = iota.v(Build.VERSION_CODES.JELLY_BEAN)
  final val kitkatAndNewer      = iota.v(Build.VERSION_CODES.KITKAT)
  final val lollipopAndNewer    = iota.v(Build.VERSION_CODES.LOLLIPOP)
  final val mAndNewer           = iota.v(Build.VERSION_CODES.M)


  @inline implicit final def toIntentFilter(s: String): IntentFilter = new IntentFilter(s)
  @inline implicit final def toIntentFilter(ss: Seq[String]): IntentFilter = {
    val filter = new IntentFilter
    ss foreach filter.addAction
    filter
  }

  @inline implicit final def fn0ToRunnable[A](f: () => A): Runnable = new Runnable() { def run() = f() }

  private[common] def byNameToRunnable[A](f: => A) = new Runnable() { def run() = f }

  @TargetApi(11)
  private[common] lazy val _threadpool = {
    if (honeycombAndNewer) android.os.AsyncTask.THREAD_POOL_EXECUTOR
    else { // basically how THREAD_POOL_EXECUTOR is defined in api11+
      import java.util.concurrent._
      import java.util.concurrent.atomic._
      // initial, max, keep-alive time
      new ThreadPoolExecutor(5, 128, 1, TimeUnit.SECONDS,
        new LinkedBlockingQueue[Runnable](10),
        new ThreadFactory() {
          val count = new AtomicInteger(1)
          override def newThread(r: Runnable) =
            new Thread(r,
              "AsyncPool #" + count.getAndIncrement)
        })
    }
  }
  @TargetApi(3)
  @inline final def isMainThread = Looper.getMainLooper.getThread == Thread.currentThread

  implicit class ContextWithSystemService(val context: Context) extends AnyVal {
    @inline final def systemService[T : iota.SystemService]: T =
      iota.systemService[T](implicitly[iota.SystemService[T]], context)
  }

  implicit class StringAsSpannedGenerator(val fmt: String) extends AnyVal {
    def formatSpans(items: CharSequence*): Spanned = {
      val builder = new SpannableStringBuilder()
      val idx = fmt indexOf "%"

      SpannedGenerator.formatNext(builder, fmt, 0, idx, items)

      builder
    }
  }

  implicit class AnyAsOptionExtension[T <: Any](val any: T) extends AnyVal {
    @inline final def ? = Option(any)
  }

  implicit class AnyAsIOExtension[T <: Any](val any: T) extends AnyVal {
    @inline final def ! = iota.IO(any)
  }
}
