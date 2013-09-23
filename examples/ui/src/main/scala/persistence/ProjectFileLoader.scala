package ui.persistence

import org.mt4j.util.math.Vector3D
import org.mt4j.types.Vec3d

import java.io._

import ui._
import ui.paths._
import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.events._


object ProjectFileLoader {

  def load() = {
   val project = scala.xml.XML.loadFile("project.xml")
    var paths = Set[Path]()
    var nodes = Set[Node]()

    /* ##### nodes ##### */
    val xmlNodes = (project \ "nodes")(0)    
    for (node <- (xmlNodes \\ "node")) {
      val nodeType = (node \ "@type").text
      if (nodeType != "ControlNode" && nodeType != "TimeNode") { //these are added to the ui implicitly
        nodes = nodes + this.xmlToNode(node)
      }
    }       
    
    for (node <- nodes) {
      if (node.nodeType == IsolatedNodeType || node.nodeType == ManipulableNodeType || node.nodeType == SingleNodeType) { //all the other nodes have already been added above with their associated paths  
        Ui += node
      }
    }    
    
    
    /* ##### paths ##### */
    for (path <- (project \\ "path")) {
      paths = paths + this.xmlToPath(path)
    }  
     
       
    for (path <- paths) {
      path.connections.foreach(_.nodes.foreach(node => {
        Ui += node
      }))
      Ui += path
    }
   
   
   /* ##### time connections between paths ##### */
   
   
    
  }

 
  /**
  * Constructs a path from xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */   
  private def xmlToPath(xmlPath: scala.xml.Node): Path = {
    if ((xmlPath \ "@factory").text == "ManipulableBezierConnection") {    
      var connectionList = List[ManipulableBezierConnection]()
      var anchorNodeOption: Option[Node] = None
      for (xmlConnection <- (xmlPath \\ "connection")) {
        val connection = this.xmlToManipulableBezierConnection(xmlConnection, anchorNodeOption)
        connectionList = connectionList :+ connection
        anchorNodeOption = Some(connection.endNode)
      }    
      
      val path = Path(Ui, ManipulableBezierConnection.apply, connectionList)    
      
      val xmlTimeNodes = (xmlPath \ "timeNodes")(0)
      var timeNodeList = List[TimeNode]()
      for (node <- (xmlTimeNodes \\ "node")) {
        path ! TimeNodeAddEvent(this.xmlToTimeNode(node, path))
      } 
      
      path
    }
    else throw new IllegalArgumentException("This xml file is corrupted.")
  }
  
  
  /**
  * Constructs a manipulable bezier connection rom xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */  
  private def xmlToManipulableBezierConnection(xmlConnection: scala.xml.Node, startNodeOption: Option[Node]): ManipulableBezierConnection = {
    if ((xmlConnection \ "@type").text == "ManipulableBezierConnection") {    
      var nodeList = List[Node]()
      for (xmlNode <- (xmlConnection \\ "node")) {
        nodeList = nodeList :+ this.xmlToNode(xmlNode)
      }
      
      val startNode = startNodeOption match {case Some(startNode) => startNode case None => nodeList(0)}
      val connection = ManipulableBezierConnection(Ui, startNode, nodeList(1), nodeList(2))     
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
  
 
  /**
  * Constructs a node from xml (excluding time nodes, which have to be dealt with in a separate method).
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */  
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
    //else if (nodeType == "IsolatedNode") Node(Ui, IsolatedNodeType, None, Vec3d(x,y))     
    else if (nodeType == "SingleNode") {
      val node = SingleNode(Ui, Vec3d(x,y))
      val properties = (xmlNode \\ "property").map {property => this.xmlToSimpleProperty(property, node)}      
      var propertyMap = Map[PropertyType, SimpleProperty]()
      properties.foreach(_ match {
        case volumeProperty: SimpleVolumeProperty => propertyMap += (VolumePropertyType -> volumeProperty)
        case pitchProperty: SimplePitchProperty => propertyMap += (PitchPropertyType -> pitchProperty)
        case somethingElse => throw new IllegalArgumentException("This xml file is corrupted.")
      })
      node.setProperties(propertyMap)    
      node
    }
    else throw new IllegalArgumentException("This xml file is corrupted.")    
  }
  
 
  /**
  * Constructs a time node from xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */
  private def xmlToTimeNode(xmlNode: scala.xml.Node, path: Path): TimeNode = {
    val nodeType = (xmlNode \ "@type").text
    if (nodeType == "TimeNode") {
      val x = (xmlNode \ "@x").text.toFloat
      val y = (xmlNode \ "@y").text.toFloat      
      val (connection, parameter) = path.closestSegment(x, y) //calculate the point on the path; this data could also be stored in the xml file for later retrieval, but for now this approach works just fine
      TimeNode(Ui, (path, connection, parameter))
    }    
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
  
  /**
  * Constructs a simple property from xml.
  * @throws an IllegalArgumentException if the xml is malformed or otherwise corrupted.
  */
  private def xmlToSimpleProperty(xmlProperty: scala.xml.Node, node: ManipulableNode): SimpleProperty = {
    val property = 
      if ((xmlProperty \ "@type").text == "SimpleVolumeProperty") SimpleVolumeProperty(node)
      else if ((xmlProperty \ "@type").text == "SimplePitchProperty") SimplePitchProperty(node)
      else throw new IllegalArgumentException("This xml file is corrupted.")
    
      val bucketValue = (xmlProperty \\ "bucket")(0).child.text.toFloat //obtaining the bucket value
      
      property.update(bucketValue)
      property
  }  
  
}
