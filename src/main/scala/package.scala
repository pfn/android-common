package com.hanhuy.android

import android.content.{Context, IntentFilter}
import android.graphics.Typeface
import android.os.{Looper, Build}
import android.text.style.{StyleSpan, ForegroundColorSpan}
import android.text.{SpannableString, SpannableStringBuilder, Spanned}

import scala.annotation.tailrec
import language.implicitConversions
import language.postfixOps

package object common {
  val gingerbreadAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
  val honeycombAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
  val icsAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
  val jellybeanAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
  val kitkatAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
  val lollipopAndNewer =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP


  implicit def toIntentFilter(s: String): IntentFilter = new IntentFilter(s)
  implicit def toIntentFilter(ss: Seq[String]): IntentFilter = {
    val filter = new IntentFilter
    ss foreach filter.addAction
    filter
  }

  implicit def fn0ToRunnable[A](f: () => A): Runnable = new Runnable() { def run() = f() }

  private[common] def byNameToRunnable[A](f: => A) = new Runnable() { def run() = f }

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
  def isMainThread = Looper.getMainLooper.getThread == Thread.currentThread

  private lazy val SERVICE_CONSTANTS = {
    val fields = classOf[Context].getDeclaredFields filter {
      _.getName endsWith "_SERVICE"
    }
    fields map { f =>
      val v = f.get(null).toString
      v.replaceAll("_", "") -> v
    } toSeq
  }

  import language.experimental.macros
  implicit def materializeSystemService[T]: SystemService[T] = macro materializeSystemServiceImpl[T]
  def materializeSystemServiceImpl[T: c.WeakTypeTag](c: reflect.macros.Context): c.Expr[SystemService[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val candidates = SERVICE_CONSTANTS filter (tpe.toString.toLowerCase contains _._1)
    val service = ("" /: candidates) { (a, b) =>
      if (a.length > b._2.length) a else b._2
    }

    c.Expr[SystemService[T]] {
      q"com.hanhuy.android.common.SystemService[$tpe]($service)"
    }
  }

  implicit class ContextWithSystemService(val context: Context) extends AnyVal {
    def systemService[T](implicit s: SystemService[T]): T =
      context.getSystemService(s.name).asInstanceOf[T]
  }

  implicit class SpannedGenerator(val fmt: String) extends AnyVal {
    def formatSpans(items: CharSequence*): Spanned = {
      val builder = new SpannableStringBuilder()
      val idx = fmt indexOf "%"

      SpannedGenerator.formatNext(builder, fmt, 0, idx, items)

      builder
    }
  }
  object SpannedGenerator {
    def span(style: Object, text: CharSequence) = {
      val s = new SpannableString(text)
      s.setSpan(style, 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
      s
    }
    def textColor(color: Int, text: CharSequence) =
      span(new ForegroundColorSpan(color), text)

    def bold(text: CharSequence) = span(new StyleSpan(Typeface.BOLD) , text)

    def italics(text: CharSequence) = span(new StyleSpan(Typeface.ITALIC), text)

    val DIGITS = Set('0','1','2','3','4','5','6','7','8','9')

    @tailrec
    private def formatNext(s: SpannableStringBuilder, fmt: String,
                           cur: Int, next: Int, items: Seq[CharSequence]) {
      if (next == -1) {
        s.append(fmt.substring(cur, fmt.length))
      } else {
        s.append(fmt.substring(cur, next))
        val space = fmt.indexWhere(!SpannedGenerator.DIGITS(_), next + 1)
        val number = fmt.substring(next + 1,
          if (space < 0) fmt.length else space).toInt
        s.append(Option(items(number - 1)) getOrElse "")
        if (space > 0)
          formatNext(s, fmt, space, fmt indexOf ("%", space), items)
      }
    }
  }
}
