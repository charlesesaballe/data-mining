lazy val commonSettings = Seq(
  organization := "org.isorokoumov.pdiscovery",
  version := "1.0",
  scalaVersion := "2.11.5",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
)

// module with NLP-related scripts
lazy val nlp = (project in file("nlp")).
  settings(commonSettings: _*).
  settings(
    name := "nlp",
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "english" % "2015.2.19"
    )
  )

// module with itemset pattern mining experiments
lazy val itemset = (project in file("itemset")).
  settings(commonSettings: _*).
  settings(
    name := "itemset"
  )

// module with sequential pattern mining experiments
lazy val sequential = (project in file("sequential")).
  settings(commonSettings: _*).
  settings(
    name := "sequential"
  )

// root module
lazy val patternDiscovery = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "pattern-discovery"
  ).dependsOn(nlp, itemset, sequential).aggregate(nlp, itemset, sequential)
