name := "VPDSynth"

organization := "org.lodsb"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-unchecked", "-deprecation") //, "-Xprint:typer")

scalacOptions <++= scalaVersion map { version =>
  val Version = """(\d+)\.(\d+)\..*"""r
  val Version(major0, minor0) = version map identity
  val (major, minor) = (major0.toInt, minor0.toInt)
  if (major < 2 || (major == 2 && minor < 10)) 
  	Seq("-Ydependent-method-types")
 	else Nil
}


libraryDependencies += "org.lodsb" %% "ultracom" % "0.2-PROC2"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "com.thesamet" %% "kdtree" % "1.0.1"


unmanagedBase <<= baseDirectory { base => base / "../../libraries/" }

unmanagedJars in Compile <++= baseDirectory map { base =>
  val baseDirectories = (base / "../../libraries/misc") +++ (base / "../../libraries/processing")
  val customJars = (baseDirectories ** "*.jar")
  customJars.classpath
}

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))


