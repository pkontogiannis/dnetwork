name := "dNetwork"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion = "2.5.17"
lazy val scalaTestVersion = "3.0.5"
lazy val argonautVersion = "6.2.2"
lazy val slickVersion = "3.2.3"
lazy val flywayVersion = "5.2.0"
lazy val jwtVersion = "0.19.0"
lazy val circeVersion = "0.10.0"
lazy val circeExtra = "1.22.0"
lazy val h2Version = "1.3.148"
lazy val catsVersion = "1.4.0"
lazy val scalaCheck = "1.14.0"
lazy val neoTypesVersion = "0.7.0"
lazy val neo4jVersion = "3.5.5"
lazy val neo4jDriverVersion = "1.7.4"

libraryDependencies ++= {
    Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,

        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "it,test",
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "it,test",
        "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "it,test",

        // Scala Test
        "org.scalatest" %% "scalatest" % scalaTestVersion % "it,test",
        "org.scalacheck" %% "scalacheck" % scalaCheck,

        // JSON Serialization Library
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "de.heikoseeberger" %% "akka-http-circe" % circeExtra,

        // Migration of SQL Databases
        "org.flywaydb" % "flyway-core" % flywayVersion,

        // ORM
        "com.typesafe.slick" %% "slick" % slickVersion,
        "com.h2database" % "h2" % h2Version, // % Test,

        // Logging dependencies
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",

        "com.pauldijou" %% "jwt-core" % jwtVersion,
        "com.pauldijou" %% "jwt-circe" % jwtVersion,

        "org.typelevel" %% "cats-core" % catsVersion,

        "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriverVersion,
        "org.neo4j" % "neo4j-graphdb-api" % neo4jVersion,
        "org.neo4j" % "neo4j-kernel" % neo4jVersion,
        "org.neo4j" % "neo4j-ogm-embedded-driver" % "3.1.9"
    )
}


lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
      resolvers += Resolver.bintrayRepo("unisay", "maven"),
      Defaults.itSettings,
      parallelExecution in ThisBuild := false
  )


enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

mainClass in(Compile, run) := Some("com.klm.Main")
