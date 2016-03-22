enablePlugins(SimpleFXPlugin)
scalaVersion := "2.11.7"
organization := "SANDEC"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_")
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
fork := true