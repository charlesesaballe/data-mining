lazy val commonSettings = Seq(
  organization := "org.isorokoumov.pdiscovery",
  version := "1.0",
  scalaVersion := "2.11.5"
)

lazy val commons = (project in file("commons")).
  settings(commonSettings: _*).
  settings(
    name := "commons"
  )

lazy val apriori = (project in file("apriori")).
  settings(commonSettings: _*).
  settings(
    name := "apriori"
  ).dependsOn(commons)

lazy val patternDiscovery = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "pattern-discovery"
  ).dependsOn(commons, apriori).aggregate(commons, apriori)
