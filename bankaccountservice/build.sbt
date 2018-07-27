name := """BankAccountService_0"""
organization := "com.xyzbank"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
	.settings(routesGenerator := InjectedRoutesGenerator)
	.enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "junit" % "junit" % "4.10" % Test
libraryDependencies += "org.scoverage" %% "scalac-scoverage-plugin" % "1.3.1" % "provided"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.4"


//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"