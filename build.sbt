name := "scala-mqtt-client"

version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.8"

organization := "com.github.hyjay"
homepage := Some(url("https://github.com/hyjay/scala-mqtt-client"))
developers := List(Developer(
  "hyjay",
  "Jay Kim",
  "hyeonjay.kim@gmail.com",
  url("https://github.com/hyjay")
))
licenses += ("MIT", url("https://github.com/hyjay/scala-mqtt-client/blob/master/LICENSE"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/hyjay/scala-mqtt-client"),
    "https://github.com/hyjay/scala-mqtt-client.git"
  )
)
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
publishMavenStyle := true

// Bintray resolver for "moquette-broker"
resolvers +=
  "bintray" at "https://jcenter.bintray.com"

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.1.33.Final",
  "co.fs2" %% "fs2-io" % "1.0.3",
  "org.slf4j" % "slf4j-log4j12" % "1.7.26",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "io.moquette" % "moquette-broker" % "0.12.1" % Test
)
