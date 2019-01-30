import sbt._

// scalastyle:off
object Dependencies {
  import Versions._

  // Libraries
  object Compile {
    val akkaHttp             = "com.typesafe.akka"        %% "akka-http"                  % akkaHttpVersion
    val akkaHttpJson4s       = "de.heikoseeberger"        %% "akka-http-json4s"           % akkaHttpJson4sVersion
    val json4s               = "org.json4s"               %% "json4s-native"              % json4sVersion
    val json4sExt            = "org.json4s"               %% "json4s-ext"                 % json4sVersion
    val faunaDb              = "com.faunadb"              %% "faunadb-scala"              % faunaDbVersion
  }

  object Test {
    val scalaTest            = "org.scalatest"            %% "scalatest"                  % scalaTestVersion   % "test"
    val scalaMock            = "org.scalamock"            %% "scalamock"                  % scalaMockVersion   % "test"
    val akkaHttpTestkit      = "com.typesafe.akka"        %% "akka-http-testkit"          % akkaHttpVersion    % "test"
  }

  val akkaHttp = Seq(Compile.akkaHttp, Compile.akkaHttpJson4s, Compile.json4s, Compile.json4sExt)
  val faunaDb = Seq(Compile.faunaDb)
  val test = Seq(Test.scalaTest, Test.scalaMock, Test.akkaHttpTestkit)

  val faunaDbDemoApp = akkaHttp ++ faunaDb ++ test
}
