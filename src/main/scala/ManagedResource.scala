package com.hanhuy.android.common

import java.io.{InputStream, OutputStream}

import ManagedResource._
import android.database.Cursor

/**
 * @author pfnguyen
 */
object ManagedResource {
  trait ResourceManager[-A] {
    def dispose(resource: A): Unit
  }

  // cannot use closeable because Cursor does not implement Closeable in 4.0
  //  implicit val closeableManager = new ResourceManager[java.io.Closeable] {
  //    override def dispose(resource: Closeable) = resource.close()
  //  }
  implicit val inputStreamManager = new ResourceManager[InputStream] {
    override def dispose(resource: InputStream) = resource.close()
  }
  implicit val outputStreamManager = new ResourceManager[OutputStream] {
    override def dispose(resource: OutputStream) = resource.close()
  }

  implicit val cursorCloseManager = new ResourceManager[Cursor] {
    override def dispose(resource: Cursor) = resource.close()
  }

  def using[A : ResourceManager, B](res: => A) = ManagedResource(res)

  def apply[A : ResourceManager](opener: => A): ManagedResource[A] = ManagedResource(() => opener, List.empty)
}

case class ManagedResource[+A: ResourceManager](opener: () => A, cleanup: List[() => Unit] = List.empty) {
  lazy val res = opener()
  def flatMap[B: ResourceManager](f: A => ManagedResource[B]): ManagedResource[B] = try {
    f(res).copy(cleanup = (() => implicitly[ResourceManager[A]].dispose(res)) :: cleanup)
  } catch {
    case e: Throwable =>
      cleanup foreach (_())
      throw e
  }

  def foreach[B](f: A => B): Unit = try {
    f(res)
  } finally {
    cleanup foreach (_())
    implicitly[ResourceManager[A]].dispose(res)
  }
}
