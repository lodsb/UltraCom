package main.scala.TreeQuencer

import org.mt4j.util.opengl.GLMaterial
import org.mt4j.util.opengl.GLMaterial._
import javax.media.opengl.GL2
import java.nio.FloatBuffer

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  06.06.13 :: 09:10
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 *  ^   ^
 *  (o o)
 * {  |  }                  (Wong)
 *    "
 *
 * Don't eat the pills!
 */


class NodeMaterial(val gl: GL2) extends GLMaterial(gl) {

  private var _diffuse: Array[Float] = null
  private var _ambient: Array[Float] = null
  private var _specular: Array[Float] = null
  private var _emission: Array[Float] = null
  private var _shininess: Float = null.asInstanceOf[Float]

  override def setDiffuse(color: Array[Float]) {
    _diffuse = color
    super.setDiffuse(color)
  }
  override def setAmbient(color: Array[Float]) {
    _ambient = color
    super.setAmbient(color)
  }
  override def setSpecular(color: Array[Float]) {
    _specular = color
    super.setSpecular(color)
  }
  override def setEmission(color: Array[Float]) {
    _emission = color
    super.setEmission(color)
  }
  override def setShininess(howShiny: Float) {
    if (howShiny >= minShine && howShiny <= maxShine) {
      _shininess = howShiny
    }
    super.setShininess(howShiny)
  }

  def getDiffuse: Array[Float] = {
    _diffuse
  }
  def getAmbient: Array[Float] = {
    _ambient
  }
  def getSpecular: Array[Float] = {
    _specular
  }
  def getEmission: Array[Float] = {
    _emission
  }
  def getShininess: Float = {
    _shininess
  }

  def copy: NodeMaterial = {
    val newMaterial = new NodeMaterial(gl)
    newMaterial.setDiffuse(_diffuse)
    newMaterial.setAmbient(_ambient)
    newMaterial.setSpecular(_specular)
    newMaterial.setEmission(_emission)
    newMaterial.setShininess(_shininess)
    newMaterial
  }

}
