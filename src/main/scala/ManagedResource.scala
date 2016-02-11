package com.hanhuy.android.common

import ManagedResource._

import scala.annotation.implicitNotFound
import scala.reflect.macros.{Context => MacroContext}
import language.experimental.macros

/**
 * @author pfnguyen
 */
object ManagedResource {
  @implicitNotFound("Unable to generate a ManagedResource.ResourceManager for ${A}, create one manually")
  trait ResourceManager[A] extends Any {
    def dispose(resource: A): Unit
  }

  implicit def materializeResourceManager[A]: ManagedResource.ResourceManager[A] = macro ManagedResourceMacro.materializeResourceManagerImpl[A]

  /** alias for apply() */
  @inline final def using[A : ResourceManager, B](res: => A) = ManagedResource(res)

  def apply[A : ResourceManager](opener: => A): ManagedResource[A] = ManagedResource(() => opener, List.empty)
}

private[common] object ManagedResourceMacro {
  def materializeResourceManagerImpl[A : c.WeakTypeTag](c: MacroContext): c.Expr[ManagedResource.ResourceManager[A]] = {
    import c.universe._
    val tp = c.weakTypeOf[A]
    val checkNoSymbol: Symbol => util.Try[Symbol] =
      s => if (s == NoSymbol) util.Failure(new Exception) else util.Success(s)
    val r = util.Try {
      tp.member(newTermName("close"))
    }.flatMap(checkNoSymbol).recover { case x =>
      tp.member(newTermName("recycle"))
    }.flatMap(checkNoSymbol).recover { case x =>
      tp.member(newTermName("dispose"))
    }.flatMap(checkNoSymbol).getOrElse(
      c.abort(c.enclosingPosition, s"no recycle/dispose/close method in $tp"))

    val expr = c.Expr(Apply(Select(Ident(newTermName("res")), r), Nil))

    reify {
      new ResourceManager[A] {
        override def dispose(res: A) = expr.splice
      }
    }
  }
}

case class ManagedResource[A: ResourceManager](opener: () => A, cleanup: List[() => Unit] = List.empty) {
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
