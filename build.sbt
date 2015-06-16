android.Plugin.androidBuild

buildConfigGenerator in Android := Nil

scalaVersion in Global := "2.11.6"

crossScalaVersions += "2.10.4"

javacOptions in Global ++= "-target" :: "1.7" :: "-source" :: "1.7" :: Nil

manifest in Android := <manifest package="com.hanhuy.android.common">
  <application/>
</manifest>

rGenerator in Android := Nil

name := "scala-common"

organization := "com.hanhuy.android"

version := "1.0"

platformTarget in Android := "android-22"

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
  case Some((2, 10)) ⇒
    Seq("org.scalamacros" %% "quasiquotes" % "2.0.1")
  case _ ⇒
    Seq()
})

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
)
