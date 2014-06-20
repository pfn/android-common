import android.Keys._

android.Plugin.androidBuild

crossScalaVersions += "2.9.2"

name := "android-common"

organization := "com.hanhuy"

version := "0.2"

platformTarget in Android := "android-19"

debugIncludesTests in Android := false

publishArtifact in (Compile,packageBin) := true

publishArtifact in (Compile,packageSrc) := true

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
    <url>git@github.com:jsuereth/scala-arm.git</url>
    <connection>scm:git:git@github.com:jsuereth/scala-arm.git</connection>
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
