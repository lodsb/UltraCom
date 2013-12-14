package org.mt4j.util.animation
import org.mt4j.types.Vec3d
import org.mt4j.util.Color
import org.mt4j.util.math.{Vertex, Vector3D}

/*
  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013-12-11 :: 20:06
    >>  Origin: org.mt4j.util
    >>
  +3>>
    >>  Copyright (c) 2013:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */


object Interpolation {
  def apply(v: Float, src: Float, dst: Float) : Float = {
    ((1-v)*src)+(v*dst)
  }

  def apply(v: Float, src:Color, dst: Color) = {
    src.rgb.interpolate(v, dst.rgb)
  }

  def apply(v: Float, src: Vector3D, dst: Vector3D) = {
    src.getScaled(1-v).getAdded(dst.getScaled(v))
  }

  def apply(v: Float, src: Vertex, dst: Vertex) : Vertex = {
    val v3d = this.apply(v, src.asInstanceOf[Vector3D], dst.asInstanceOf[Vector3D]);
    val c = this.apply(v, Color.fromMtColor(src.getColor), Color.fromMtColor(dst.getColor))
    val tx = this.apply(v, src.getTexCoordU, dst.getTexCoordU)
    val ty = this.apply(v, src.getTexCoordV, dst.getTexCoordV)

    new org.mt4j.util.math.Vertex(v3d,tx,ty,c)

  }

  def apply(v: Float, src: Array[Vertex], dst: Array[Vertex]) : Array[Vertex] = {
    def eucl(vertex: Vertex) : Float = {
      (vertex.x*vertex.x)+(vertex.y*vertex.y)+(vertex.z*vertex.z)
    }

    def appendVerticesToEqualSize(vv: Float, srcI: Seq[(Vertex, Int)], dstI: Seq[(Vertex, Int)]) : (Float, Seq[(Vertex, Int)],Seq[(Vertex, Int)])  = {
      if(dstI.size > srcI.size) {
        var srcNI = srcI
        var done = false

        while(!done){
          // repeat last element till sizes are the same
          val last = srcNI.last
          srcNI = srcNI :+ (last._1, last._2+1)

          if(srcNI.size == dstI.size) {
            done = true
          }
        }
        (vv, srcNI, dstI)

      } else if(dstI.size < srcI.size) {
        //swap
        appendVerticesToEqualSize(1-vv, dstI, srcI)
      } else {
        (vv, srcI, dstI)
      }
    }

    val idxSrc = src.zipWithIndex
    val idxDst = dst.zipWithIndex

    val idxSrcSorted = idxSrc.sortBy(x => eucl(x._1))
    val idxDstSorted = idxDst.sortBy(x => eucl(x._1))

    // also changes v, src and dst if size of src < dst
    val (va, idxSrcA, idxDstA) = appendVerticesToEqualSize(v, idxSrcSorted, idxDstSorted)

    println(idxSrcA.size + " | "+ idxDstA.size + " ||| "+ va)

    // interpolate each vertex
    val resultIdx = idxSrcA.zip(idxDstA).map({x =>
      (x._1._2, this.apply(va, x._1._1, x._2._1))
    })

    println(resultIdx)
    // rebuild into original order
    resultIdx.sortBy(x => x._1).map(_._2).toArray
  }
}
