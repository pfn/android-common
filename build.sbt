android.Plugin.androidBuildJar

scalaVersion in Global := "2.11.6"

crossScalaVersions += "2.10.4"

javacOptions in Global ++= "-target" :: "1.7" :: "-source" :: "1.7" :: Nil

libraryDependencies += "com.hanhuy.android" %% "iota" % "1.0.1"

name := "scala-common"

organization := "com.hanhuy.android"

sonatypeProfileName := "com.hanhuy"

version := "1.3-SNAPSHOT"

platformTarget in Android := "android-23"

// sonatype publishing options follow
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra :=
  <scm>
    <url>git@github.com:pfn/android-common.git</url>
    <connection>scm:git:git@github.com:pfn/android-common.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pfnguyen</id>
      <name>Perry Nguyen</name>
      <url>https://github.com/pfn</url>
    </developer>
  </developers>

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/pfn/android-common"))

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 10)) =>
    "org.scalamacros" %% "quasiquotes" % "2.0.1" ::
    compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full) ::
    Nil
  case _ => Nil
})

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
