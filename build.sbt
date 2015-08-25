enablePlugins(SimpleFXPlugin)
scalaVersion := "2.11.7"
organization := "SANDEC"
resolvers += Resolver.url("SANDEC", url("http://sandec.de/repo/"))(Resolver.ivyStylePatterns)
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_")
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
fork := true