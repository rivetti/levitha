import AssemblyKeys._ // put this at the top of the file

assemblySettings

name	:= "DA"

version	:= "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

jarName in assembly := "DA.jar"

mainClass in assembly := Some("org.opensplice.mobile.dev.main.Main")

mainClass := Some("org.opensplice.mobile.dev.main.Main")

mainClass in (Compile, run) := Some("org.opensplice.mobile.dev.main.Main")

scalaSource in Compile <<= baseDirectory(_ / "src")

//unmanagedSourceDirectories in Compile <<= baseDirectory(_ / "target/generated-sources")

// This is used to fetch the jar for the DDS implementation (such as OpenSplice Mobile)
resolvers += "Local Maven Repo" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

//Scala
libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

//libraryDependencies += "org.scala-lang" % "scala-swing" % "2.11.0-M3"
libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.0"

// Mobile
libraryDependencies += "org.opensplice.mobile" % "ospl-mobile" % "1.1.1-SNAPSHOT"

// Logback
libraryDependencies += "ch.qos.logback" % "logback-core" % "0.9.30"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "0.9.30"

//libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.2"

//Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.0"
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11.0-M4" % "2.2.0"
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2-M2"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

//Validator
//libraryDependencies += "org.helrohir.uni" % "davalidatorclient_2.9.2" % "0.1.0-SNAPSHOT"

//Esper
//libraryDependencies += "com.espertech" % "esperio-socket" % "4.9.0"

//Log4J x Esper
//libraryDependencies += "log4j" % "log4j" % "1.2.17"

//Tuple Space
libraryDependencies += "io.nuvo" % "nuvo-spaces_2.10" % "0.1.2"

autoCompilerPlugins := true

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

scalacOptions += "-optimise"

scalacOptions ++= Seq("-Xelide-below", "5000")

scalacOptions += "-feature"

scalacOptions += "-language:postfixOps"

//javacOptions ++= Seq("-source", "1.6","-target", "1.6")


