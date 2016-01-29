package com.hanhuy.android.common

import android.content.Context
import android.graphics.Typeface
import android.net.nsd.NsdManager
import android.telephony.TelephonyManager
import android.text.style.{StyleSpan, ForegroundColorSpan}
import android.text.{SpannableStringBuilder, Spanned, SpannableString}
import android.util.Log

import scala.annotation.{tailrec, implicitNotFound}

@implicitNotFound(msg = "Unable to find a system service for $T")
case class SystemService[T](name: String) extends AnyVal
object SystemService {
  import Context._
  implicit val `nsd system service` =
    SystemService[NsdManager](NSD_SERVICE)
  implicit val `telephony system service` =
    SystemService[TelephonyManager](TELEPHONY_SERVICE)
}
case class Logcat(tag: String) extends AnyVal {
  @inline def d(msg: String)               = Log.d(tag, msg)
  @inline def d(msg: String, e: Throwable) = Log.d(tag, msg, e)
  @inline def d(msg: String, args: Any*)   = Log.d(tag, msg format(args:_*))
  @inline def v(msg: String)               = Log.v(tag, msg)
  @inline def v(msg: String, e: Throwable) = Log.v(tag, msg, e)
  @inline def v(msg: String, args: Any*)   = Log.v(tag, msg format(args:_*))
  @inline def i(msg: String)               = Log.i(tag, msg)
  @inline def i(msg: String, e: Throwable) = Log.i(tag, msg, e)
  @inline def i(msg: String, args: Any*)   = Log.i(tag, msg format(args:_*))
  @inline def w(msg: String)               = Log.w(tag, msg)
  @inline def w(msg: String, e: Throwable) = Log.w(tag, msg, e)
  @inline def w(msg: String, args: Any*)   = Log.w(tag, msg format(args:_*))
  @inline def e(msg: String)               = Log.e(tag, msg)
  @inline def e(msg: String, e: Throwable) = Log.e(tag, msg, e)
  @inline def e(msg: String, args: Any*)   = Log.e(tag, msg format(args:_*))
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

  private val DIGITS = Set('0','1','2','3','4','5','6','7','8','9')

  @tailrec
  private[common] def formatNext(s: SpannableStringBuilder, fmt: String,
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
