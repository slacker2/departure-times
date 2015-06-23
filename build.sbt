name := """departure-times"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "se.radley" %% "play-plugins-salat" % "1.5.0", 
  jdbc,
  cache,
  ws
  //specs2 % Test
)

import play.PlayImport.PlayKeys._
routesImport += "se.radley.plugin.salat.Binders._"
TwirlKeys.templateImports += "org.bson.types.ObjectId"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
//resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/ "))(Resolver.ivyStylePatterns),
//resolvers += "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/com/"
//resolvers += "Sedis Repo" at "http://pk11-scratch.googlecode.com/svn/trunk"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator
