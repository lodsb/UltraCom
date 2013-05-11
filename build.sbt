name := "UltraCom"

scalaVersion := "2.9.2"

organization := "org.lodsb"

version := "0.1-SNAPSHOT"



scalacOptions ++= Seq("-unchecked", "-deprecation") //, "-Xprint:typer")

scalacOptions <++= scalaVersion map { version =>
  val Version = """(\d+)\.(\d+)\..*"""r
  val Version(major0, minor0) = version map identity
  val (major, minor) = (major0.toInt, minor0.toInt)
  if (major < 2 || (major == 2 && minor < 10)) 
  	Seq("-Ydependent-method-types")
 	else Nil
}


//libraryDependencies += "de.sciss" %% "scalacollider" % "1.3.+"

//libraryDependencies += "de.sciss" %% "scalaosc" % "1.1.+"

libraryDependencies += "org.lodsb" %% "reakt" % "0.1-SNAPSHOT"

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))

unmanagedBase <<= baseDirectory { base => base / "libraries/misc" }

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

resolvers += "Twitter repo" at "http://maven.twttr.com/"

libraryDependencies ++= Seq(
    "com.twitter" % "util-eval" % "1.12.13"
)
