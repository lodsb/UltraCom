name := "TreeQuencer"

organization := "org.lodsb"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation") //, "-Xprint:typer")

scalacOptions <++= scalaVersion map { version =>
  val Version = """(\d+)\.(\d+)\..*"""r
  val Version(major0, minor0) = version map identity
  val (major, minor) = (major0.toInt, minor0.toInt)
  if (major < 2 || (major == 2 && minor < 10)) 
  	Seq("-Ydependent-method-types")
 	else Nil
}

unmanagedBase <<= baseDirectory { base => base / "../../libraries/" }

unmanagedJars in Compile <++= baseDirectory map { base =>
    val baseDirectories = (base / "../../libraries/misc") +++ (base / "../../libraries/processing")
    val customJars = (baseDirectories ** "*.jar")
    customJars.classpath
}

libraryDependencies += "org.lodsb" %% "ultracom" % "0.2-PROC2"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))

unmanagedBase <<= baseDirectory { base => base / "../../libraries/misc" }
 
libraryDependencies ++= Seq(
    "com.twitter" % "util-eval" % "1.12.13"
)

