name := "UltraCom"

scalaVersion := "2.10.1"

organization := "org.lodsb"

version := "0.2-SNAPSHOT"



scalacOptions ++= Seq("-unchecked", "-deprecation") //, "-Xprint:typer")

scalacOptions <++= scalaVersion map { version =>
  val Version = """(\d+)\.(\d+)\..*"""r
  val Version(major0, minor0) = version map identity
  val (major, minor) = (major0.toInt, minor0.toInt)
  if (major < 2 || (major == 2 && minor < 10)) 
  	Seq("-Ydependent-method-types")
 	else Nil
}


libraryDependencies += "de.sciss" %% "scalacollider" % "1.10.+"

libraryDependencies += "de.sciss" %% "scalaosc" % "1.1.2+"

libraryDependencies += "org.lodsb" %% "reakt" % "0.2-SNAPSHOT"

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))

//unmanagedBase <<= baseDirectory { base => base / "libraries/misc" }

unmanagedBase <<= baseDirectory { base => base / "libraries/" }

unmanagedJars in Compile <++= baseDirectory map { base =>
    val baseDirectories = (base / "libraries/misc") +++ (base / "libraries/processing") 
    val customJars = (baseDirectories ** "*.jar")
    customJars.classpath
}


resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.0-SNAPSHOT")

resolvers += "Twitter repo" at "http://maven.twttr.com/"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "0.4.2"

libraryDependencies ++= Seq(
    "com.twitter" % "util-eval" % "1.12.13"
)

//libraryDependencies ++= Seq(
//    "com.twitter" % "util-logging" % "1.12.13"
//)
