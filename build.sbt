name := """steam-chat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

lazy val akkaVersion = "2.4.0"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.4.play24",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
