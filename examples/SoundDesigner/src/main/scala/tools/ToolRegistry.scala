package ui.tools

import ui.paths._


/**
* This trait realizes a registry for tools. It is used by paths and manipulable nodes to keep
* track of nearby tools and - optionally - their parameterized location, 
* which in turn allows for manipulation areas to be highlighted.
*/
trait ToolRegistry {

  protected var registry = Map[Tool, Option[(ManipulableBezierConnection, Float, Float)]]()	

  def registerTool(tool: Tool) = {
    this.registry += (tool -> None)
  }
  
  def registerTool(tool: Tool, connection: ManipulableBezierConnection, connectionParameter: Float, manipulationRadius: Float) = {
    this.registry += (tool -> Some((connection, connectionParameter, manipulationRadius)))
  }
  
  def unregisterTool(tool: Tool) = {
    this.registry -= tool
  }
  
  def registeredTools = {
    this.registry.keys
  }
  
  def toolRegistryEntries = {
    this.registry
  }
  
  
}
