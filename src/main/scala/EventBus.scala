package com.hanhuy.android.common

import android.os.{Handler, Looper}

trait BusEvent

object EventBus {

  class Owner {
    var handlers = List.empty[EventBus.Handler]
  }

  trait RefOwner {
    final implicit val __eventBusRefOwner__ = new Owner
  }

  type Handler = PartialFunction[BusEvent,Any]

  /** result value for `Handler`, returning this removes the handler from
    * future processing
    */
  object Remove
}
abstract class EventBus {
  import ref.WeakReference

  private var queue = List.empty[WeakReference[EventBus.Handler]]

  protected def broadcast(e: BusEvent) = queue foreach { r =>
    r.get match {
      case Some(h) => if (h.isDefinedAt(e)) if (h(e) == EventBus.Remove) this -= r
      case None => this -= r
    }
  }

  final def clear() = queue = Nil

  def send(e: BusEvent) = broadcast(e)

  // users of += must have trait EventBus.RefOwner
  final def +=(handler: EventBus.Handler)(implicit owner: EventBus.Owner) {
    // long-lived objects that use EventBus must purge their owner list
    // keep the handler only for as long as the weak reference is valid
    owner.handlers = handler :: owner.handlers
    queue = new WeakReference(handler) :: queue
  }

  final def size = queue.size

  // don't know the owner to remove it from  :-/
  private[this] def -=(e: WeakReference[EventBus.Handler]) =
    queue = queue filterNot (_ == e)
}
/** an `EventBus` that will only handle events on the UI thread */
object UiBus extends EventBus {

  lazy val handler = new Handler(Looper.getMainLooper)

  def post(f: => Unit) = handler.post(byNameToRunnable(f))

  def run(f: => Unit) = if (isMainThread) f else post(f)

  override def send(e: BusEvent) =
    if (isMainThread) broadcast(e) else post { broadcast(e) }
}

/** an `EventBus` that pays no attention to which thread it's on */
object ServiceBus extends EventBus
