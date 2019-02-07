name := "cloud-simulation-basics"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq("com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe" % "config" % "1.3.3",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)