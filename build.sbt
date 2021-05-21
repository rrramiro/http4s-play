
lazy val playVersion = "2.8.18"
lazy val fs2Version  = "2.4.2"
lazy val http4sVersion = "0.21.7"
lazy val circeVersion  = "0.13.0"
lazy val akkaVersion    = "2.6.10"
lazy val scalaTestPlayVersion  = "5.1.0"
lazy val scalaTestVersion  = "3.2.9"
lazy val monixVersion = "3.4.0"

ThisBuild / scalaVersion := "2.13.10"

lazy val `examples-monix` = project.in(file("example-monix"))
  .enablePlugins(PlayScala)
  .settings(
    description := "Example of http4s on Play with Monix",
    Compile / scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "io.monix"   %% "monix"               % monixVersion,
      "io.circe"   %% "circe-generic"       % circeVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "org.http4s" %% "http4s-twirl"        % http4sVersion,
      "org.http4s" %% "http4s-scala-xml"    % http4sVersion,
      "com.softwaremill.macwire" %% "macros" % "2.3.7" % "provided",
      // "javax.xml.bind" % "jaxb-api" % "2.3.0",
    )
  )
  .dependsOn(`play-route`)

lazy val `examples-play` = project.in(file("example"))
 .enablePlugins(PlayScala)
  .settings(
    description := "Example of http4s on Play",
    Compile / scalacOptions  -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "io.circe"   %% "circe-generic"       % circeVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "org.http4s" %% "http4s-twirl"        % http4sVersion,
      "org.http4s" %% "http4s-scala-xml"    % http4sVersion,
      "com.softwaremill.macwire" %% "macros" % "2.3.7" % "provided"
    )
  )
  .dependsOn(`play-route`)

lazy val `play-route` = project.in(file("."))
  .settings(
    description := "Play wrapper of http4s services",
    libraryDependencies ++= Seq(
      "co.fs2"                 %% "fs2-io"                % fs2Version,
      "co.fs2"                 %% "fs2-reactive-streams"  % fs2Version,
      "com.typesafe.akka"      %% "akka-stream"           % akkaVersion,
      "com.typesafe.play"      %% "play"                  % playVersion,
      "com.typesafe.play"      %% "play-akka-http-server" % playVersion % Test,
      "org.http4s"             %% "http4s-server"         % http4sVersion,
      "org.http4s"             %% "http4s-core"           % http4sVersion,
      "org.http4s"             %% "http4s-dsl"            % http4sVersion % Test,
      "org.scalatestplus.play" %% "scalatestplus-play"    % scalaTestPlayVersion % Test,
      "org.scalatest"          %% "scalatest"             % scalaTestVersion % Test
    ),
    Compile / compile / wartremoverErrors := Seq.empty,
    Test / compile / wartremoverErrors := Seq.empty
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
