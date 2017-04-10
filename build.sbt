
name := "twillo"

version := "1.0"

lazy val `twillo` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies += "com.twilio.sdk" % "twilio-java-sdk" % "7.0.0-rc-10"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"