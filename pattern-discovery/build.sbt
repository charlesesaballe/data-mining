lazy val commonSettings = Seq(
  organization := "org.isorokoumov.pdiscovery",
  version := "1.0",
  scalaVersion := "2.11.5"
)

lazy val ner = (project in file("ner")).
  settings(commonSettings: _*).
  settings(
    name := "ner",
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "english"  % "2015.2.19"
    )
  )

lazy val apriori = (project in file("apriori")).
  settings(commonSettings: _*).
  settings(
    name := "apriori"
  )

lazy val patternDiscovery = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "pattern-discovery"
  ).dependsOn(ner, apriori).aggregate(ner, apriori)
