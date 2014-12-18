name := "twiiface"

version := "0.0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val akkaVersion  = "2.3.7"
  val sprayVersion = "1.3.2"
  Seq(
    "com.typesafe.akka"  %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion     % "test",
    "com.typesafe.akka"  %% "akka-slf4j"           % akkaVersion,
    "io.spray"           %% "spray-client"         % sprayVersion      withSources(),
    "io.spray"           %% "spray-json"           % "1.3.1",
    "commons-codec"      %  "commons-codec"        % "1.10",
    "ch.qos.logback"     %  "logback-classic"      % "1.1.2",
    "org.scalatest"      %% "scalatest"            % "2.2.1"         % "test"
  )
}

resolvers += "spray repo" at "http://repo.spray.io"


scalacOptions ++= Seq("-deprecation",
                      "-encoding", "UTF-8",
                      "-feature",
                      "-unchecked",
                      "-Xfatal-warnings",
                      "-Xlint",
                      "-Yno-adapted-args",
                      "-Ywarn-dead-code",
                      "-Ywarn-numeric-widen",
                      "-Ywarn-value-discard",
                      "-Xfuture"
)

Revolver.settings
