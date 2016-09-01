enablePlugins(SimpleFXPlugin)
scalaVersion := "2.11.8"
organization := "SANDEC"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_")
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
fork := true