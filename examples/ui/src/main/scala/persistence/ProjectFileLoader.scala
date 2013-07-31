package ui.persistence

import org.mt4j.util.math.Vector3D
import org.mt4j.types.Vec3d

import java.io._

import ui._
import ui.paths._
import ui.paths.types._
import ui.properties._
import ui.properties.types._


object ProjectFileLoader {

  def load() = {
   val project = scala.xml.XML.loadFile("project.xml")
    var paths = Set[Path]()
    var nodes = Set[Node]()
 
    for (path <- (project \\ "path")) {    
      paths = paths + this.xmlToPath(path)
    }  
    
    val xmlNodes = (project \ "nodes")(0)    
    for (node <- (xmlNodes \\ "node")) {    
      nodes = nodes + this.xmlToNode(node)
    }      
       
    for (path <- paths) {
      Ui += path
    }
    
    for (node <- nodes) {
      Ui += node
    }
    
  }

 
  /**
  * Constructs a path from xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */   
  private def xmlToPath(xmlPath: scala.xml.Node): Path = {
    if ((xmlPath \ "@factory").text == "ManipulableBezierConnection") {    
      var connectionList = List[ManipulableBezierConnection]()
      for (connection <- (xmlPath \\ "connection")) {
        connectionList = connectionList :+ this.xmlToManipulableBezierConnection(connection)
      }    
      Path(Ui, ManipulableBezierConnection.apply, connectionList)    
    }
    else throw new IllegalArgumentException("This xml file is corrupted.")
  }
  
  
  /**
  * Constructs a manipulable bezier connection rom xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */  
  private def xmlToManipulableBezierConnection(xmlConnection: scala.xml.Node): ManipulableBezierConnection = {
    if ((xmlConnection \ "@type").text == "ManipulableBezierConnection") {    
      var nodeList = List[Node]()
      for (node <- (xmlConnection \\ "node")) {
        nodeList = nodeList :+ this.xmlToNode(node)
      }
      
      val connection = ManipulableBezierConnection(Ui, nodeList(0), nodeList(1), nodeList(2))     
      val properties = (xmlConnection \\ "property").map {property => this.xmlToComplexProperty(property, connection)}
      var propertyMap = Map[PropertyType, ComplexProperty]()
      properties.foreach(_ match {
        case volumeProperty: ComplexVolumeProperty => propertyMap += (VolumePropertyType -> volumeProperty)
        case pitchProperty: ComplexPitchProperty => propertyMap += (PitchPropertyType -> pitchProperty)
        case speedProperty: ComplexSpeedProperty => propertyMap += (SpeedPropertyType -> speedProperty)
        case somethingElse => throw new IllegalArgumentException("This xml file is corrupted.")
      })
      connection.setProperties(propertyMap)
      connection
    }
    else throw new IllegalArgumentException("This xml file is corrupted.")
  }
  
  
  private def xmlToNode(xmlNode: scala.xml.Node): Node = {
    val nodeType = (xmlNode \ "@type").text
    val x = (xmlNode \ "@x").text.toFloat
    val y = (xmlNode \ "@y").text.toFloat
    if (nodeType == "PlayNode") Node(Ui, PlayNodeType, None, Vec3d(x,y))
    else if (nodeType == "StopNode") Node(Ui, StopNodeType, None, Vec3d(x,y))
    else if (nodeType == "ReverseNode") Node(Ui, ReverseNodeType, None, Vec3d(x,y))   
    else if (nodeType == "RepeatNode") Node(Ui, RepeatNodeType, None, Vec3d(x,y))        
    else if (nodeType == "AnchorNode") Node(Ui, AnchorNodeType, None, Vec3d(x,y))
    else if (nodeType == "ControlNode") Node(Ui, ControlNodeType, None, Vec3d(x,y))        
    else if (nodeType == "IsolatedNode") Node(Ui, IsolatedNodeType, None, Vec3d(x,y))          
    else throw new IllegalArgumentException("This xml file is corrupted.")    
  }
  
  
  /**
  * Constructs a complex property from xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */
  private def xmlToComplexProperty(xmlProperty: scala.xml.Node, connection: ManipulableBezierConnection): ComplexProperty = {
    val property = 
      if ((xmlProperty \ "@type").text == "ComplexVolumeProperty") ComplexVolumeProperty(connection)
      else if ((xmlProperty \ "@type").text == "ComplexPitchProperty") ComplexPitchProperty(connection)
      else if ((xmlProperty \ "@type").text == "ComplexSpeedProperty") ComplexSpeedProperty(connection)
      else throw new IllegalArgumentException("This xml file is corrupted.")
    
      var numberOfBuckets = 0
      val xmlBuckets = (xmlProperty \ "buckets")(0)
      numberOfBuckets = (xmlBuckets \ "@number").text.toInt //obtaining the number of buckets
    
      val bucketArray = new Array[Float](numberOfBuckets)
      for (bucket <- (xmlBuckets \\ "bucket")) {    
        bucketArray((bucket \ "@index").text.toInt) = bucket.child.text.toFloat
      }
      
      property.update(index => bucketArray(index))
      property
  }
  
}
