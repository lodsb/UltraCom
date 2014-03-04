package mutator

/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 16 :: 10 : 16
    >>  Origin: mt4j (project) / prototaip (module)
    >>
  +3>>
    >>  Copyright (c) 2012:
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

//package Mutator

import de.sciss.osc.Message
import java.net.InetSocketAddress
import org.mt4j.input.osc.OSCCommunication
import org.mt4j.input.osc.OSCCommunication.UDP
import org.mt4j.{Scene, Application}
import org.mt4j.util.Color
import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.types.{Vec3d, Rotation}
import org.lodsb.reakt.Implicits._
import scala.actors.Actor._
import org.mt4j.components.visibleComponents.shapes.{Line, MTLine}
import java.util.Random
import org.mt4j.components.MTLight
import javax.media.opengl.GL2
import processing.core.PApplet
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DProcessor
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import scala._
import mutant5000.{Chromosome, SimpleChromosomeScore, Population}


object Mutator extends Application {

  val noVoters = 3

  val oscTransmit = OSCCommunication.createOSCTransmitter(UDP, new InetSocketAddress("127.0.0.1", 1338))

  // relative coordinates, the absolute ones are taken from the bg file
  val widgetCoordinates = List(
    (1783f/1920f, 539f/1080f),
    (1655f/1920f, 274f/1080f),
    (1391f/1920f, 146f/1080f),
    (1072f/1920f , 108f/1080f),
    (732f/1920f , 116f/1080f),
    (417f/1920f , 203f/1080f),
    (153f/1920f  , 380f/1080f),
    (153f/1920f  , 698f/1080f),
    (397f/1920f , 868f/1080f),
    (676f/1920f , 962f/1080f),
    (989f/1920f , 979f/1080f),
    (1288f/1920f, 952f/1080f),
    (1575f/1920f, 852f/1080f)
  // convert to absolute coordinates, flip y first
  ).map(x => ( x._1 , (1.0f - x._2) ) )

  // mapping definition
  val mappings = List(
    // encoding osc name, value range, deviation range (for mutation)
    (List("/shuffle","/tempo"), List((0,1),(50,300)), List(1, 10)),
    (List("/rootnote","/scale"), List((40,52),(1,8)), List(3, 1)),
    (List("/drum_kick_bar1","/drum_snare_bar1","/drum_hh_bar1"), List((1,4),(1,4),(1,8)), List(1,1,2)),
    (List("/drum_kick_bar2","/drum_snare_bar2","/drum_hh_bar2"), List((1,4),(1,4),(1,8)), List(1,1,2)),
    (List("/drum_kick_bar3","/drum_snare_bar3","/drum_hh_bar3"), List((1,4),(1,4),(1,8)), List(1,1,2)),
    (List("/drum_kick_bar4","/drum_snare_bar4","/drum_hh_bar4"), List((1,4),(1,4),(1,8)), List(1,1,2)),
    (List("/ch_rhyt1","/ch_root1","/ch_voic1"), List((1,4), (0,6), (1,8)), List(1,1,2)),
    (List("/ch_rhyt2","/ch_root2","/ch_voic2"), List((1,4), (0,6), (1,8)), List(1,1,2)),
    (List("/ch_rhyt3","/ch_root3","/ch_voic3"), List((1,4), (0,6), (1,8)), List(1,1,2)),
    (List("/ch_rhyt4","/ch_root4","/ch_voic4"), List((1,4), (0,6), (1,8)), List(1,1,2)),
    (List("/ch_indices"), List((1,7)), List(1)),
    (List("/mel_bar1","/mel_bar2","/mel_bar3","/mel_bar4"),List((1,16),(1,16),(1,16),(1,16)), List(3,3,3,3)),
    (List("/bass_bar1","/bass_bar2","/bass_bar3","/bass_bar4"),List((1,16),(1,16),(1,16),(1,16)), List(3,3,3,3))
  );

  var controllerGlue : List[ControlGlue] = List();
  var voters : List[VotingPanel] = List()

  var votes : List[Float] = List.empty

  def vote(vote: Float) = {
    votes = votes :+ vote

    if(voters.forall(p => !p.isPanelEnabled)){
      var mean = votes.sum / votes.size

      println("votes" + votes + " -> "+mean)

      runGameCycle(mean)

      voters.foreach{x => x.enablePanel(true)}

      votes = List()
    }
  }

  var population : Population = new Population(Seq.empty, None)

  def updatePopulation(score: Double) = {
    val genes = controllerGlue.map( {x=>
    // also update gene pool fitnessess from voting
      val gene = x.generateGene()
      gene
    })

    val chromosome = new Chromosome(genes, mutation = SimpleChromosomeMutation2)

    println("ADDED NEW CHROMOSOME "+chromosome.chckString + " with score "+score)

    population.add(chromosome, score)
    population.add(chromosome, score*0.00001)
  }

  def initializePopulation = {
    val r = new Random

    (0 to 100).foreach{ x =>
      val score = 1.0 - (r.nextDouble()*0.001) // high scores = bad fitness
      updatePopulation(score)
    }
  }

  def runGameCycle(fitness: Double) = {
    // score is the inverted fitness
    val score = 1.0-fitness

    controllerGlue.foreach(x => x.locked = true)

    // we give a slight preference to new specimen by successively degrading the population
    // this also helps overfitting

    population.degradePopulationScoresBy(0.2)

    updatePopulation(score)
                                                                // no elitism
    val chromosome = GameCycle.evolve(population,0.0,0.99, 0.1) // 10% mutation probability given to each gene!

    println("my new chromosome "+chromosome.chckString + "   " + chromosome.genes.size )

    population.printChromosomeChck

    controllerGlue.foreach(x => x.locked = false)

    chromosome.genes.zip(controllerGlue).foreach{x =>
      x._2.updateFromGene(x._1)
    }
  }







  var scene: MutatorScene = null

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
    scene = new MutatorScene(this, "Mutator");
		this.addScene(scene)
	}

}


class MutatorScene(app: Application, name: String) extends Scene(app,name) {
  // mapping class -> item , coords, map func

  val center = Vec3d(app.width/2f, app.height/2f)
  val width = app.width
  val height= app.height
  val margin = 100.0f

  val image = app.loadImage("drawing.png")
  image.resize(app.width, app.height)
  val backgroundImage = new MTBackgroundImage(app, image, false)
  canvas().addChild(backgroundImage)


  val l  = new MTLight(app, 3, center.getAdded(this.getSceneCam.getPosition))
  MTLight.enableLightningAndAmbient(app, 5, 5, 5, 255)
  //l.enable()

	// Show touches
	showTracer(true)


  Mutator.widgetCoordinates.zip(Mutator.mappings).foreach { mapping =>
    val x = mapping._1
    println(x)
    val form = RandomNodeForm(Vec3d(x._1*width, x._2*height,20))
    form.setLight(l)

    val glue = new ControlGlue(Mutator.oscTransmit,form, mapping._2._1, mapping._2._2, mapping._2._3)
    Mutator.controllerGlue = Mutator.controllerGlue :+ glue


    canvas += form;
    canvas += form.xCircle ++ form.yCircle ++ form.zCircle


  }

  // create an initial population
  Mutator.initializePopulation
  Mutator.runGameCycle(0.0);

  // send triggers to pd
  Mutator.oscTransmit.send() = Message("/start", 1)
  Mutator.oscTransmit.send() = Message("/audio", 1)

  val appCenter = Vec3d(app.width/2, app.height/2)

  (1 to Mutator.noVoters).foreach {  x =>

    val coord = appCenter.subtractLocal(Vec3d((( x+1 ) % 2 )*100,(( x ) % 2 )*100 ) )

    val vPanel = new VotingPanel(Vec3d(0,0,0))
    val vPanelCenter = vPanel.getCenterPointGlobal

    vPanel.rotateZ(vPanelCenter, 90f*x)
    vPanel.setPositionGlobal(coord)


    Mutator.voters = Mutator.voters :+ vPanel

    vPanel.voted.observe({x => Mutator.vote(x); true})

    canvas += vPanel
  }

}
