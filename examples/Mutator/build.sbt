name := "Mutator"

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


libraryDependencies += "org.lodsb" %% "ultracom" % "0.2-PROC2"

libraryDependencies += "org.lodsb" %% "mutant5000" % "0.1-SNAPSHOT"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

//libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.9.2" % "1.5.1"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

unmanagedBase <<= baseDirectory { base => base / "../../libraries/" }

unmanagedJars in Compile <++= baseDirectory map { base =>
  val baseDirectories = (base / "../../libraries/misc") +++ (base / "../../libraries/processing")
  val customJars = (baseDirectories ** "*.jar")
  customJars.classpath
}

conflictWarning := ConflictWarning.disable
//conflictManager := ConflictManager.all

libraryDependencies += "com.twitter" %% "util-eval" % "[6.2.4,)"

