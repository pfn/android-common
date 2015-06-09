package com.hanhuy.android.common

import android.content.Context
import android.net.nsd.NsdManager
import android.telephony.TelephonyManager
import android.util.Log

import scala.annotation.implicitNotFound

@implicitNotFound(msg = "Unable to find a system service for $T")
case class SystemService[T](name: String)
object SystemService {
  import Context._
  implicit val `nsd system service` =
    SystemService[NsdManager](NSD_SERVICE)
  implicit val `telephony system service` =
    SystemService[TelephonyManager](TELEPHONY_SERVICE)
}
case class Logcat(tag: String) {
  def d(msg: String)               = Log.d(tag, msg)
  def d(msg: String, e: Throwable) = Log.d(tag, msg, e)
  def d(msg: String, args: Any*)   = Log.d(tag, msg format(args:_*))
  def v(msg: String)               = Log.v(tag, msg)
  def v(msg: String, e: Throwable) = Log.v(tag, msg, e)
  def v(msg: String, args: Any*)   = Log.v(tag, msg format(args:_*))
  def i(msg: String)               = Log.i(tag, msg)
  def i(msg: String, e: Throwable) = Log.i(tag, msg, e)
  def i(msg: String, args: Any*)   = Log.i(tag, msg format(args:_*))
  def w(msg: String)               = Log.w(tag, msg)
  def w(msg: String, e: Throwable) = Log.w(tag, msg, e)
  def w(msg: String, args: Any*)   = Log.w(tag, msg format(args:_*))
  def e(msg: String)               = Log.e(tag, msg)
  def e(msg: String, e: Throwable) = Log.e(tag, msg, e)
  def e(msg: String, args: Any*)   = Log.e(tag, msg format(args:_*))
}
