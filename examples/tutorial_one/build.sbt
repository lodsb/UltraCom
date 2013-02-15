name := "tutorial_one"

organization := "org.lodsb"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

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

//libraryDependencies += "org.lodsb" %% "reakt" % "0.1-SNAPSHOT"

libraryDependencies += "org.lodsb" %% "ultracom" % "0.1-SNAPSHOT"

//libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.9.2" % "1.5.1"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))

unmanagedBase <<= baseDirectory { base => base / "../../libraries/misc" }


