import Settings._

lazy val `faunadb-crud-example-app` =
  project
    .in(file("."))
    .settings(settings)
    .settings(libraryDependencies ++= Dependencies.faunaDbDemoApp)
