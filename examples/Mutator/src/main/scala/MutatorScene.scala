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
import org.mt4j.util.{SessionLogger, Color}
import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.types.{Vec3d, Rotation}
import org.lodsb.reakt.Implicits._
import scala.actors.Actor._
import org.mt4j.components.visibleComponents.shapes.{MTEllipse, Line, MTLine}
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
import java.io.File


object Mutator extends Application {

  val noVoters = 3

  var progressCircle : Option[MTEllipse] = None

  val oscTransmit = OSCCommunication.createOSCTransmitter(UDP, new InetSocketAddress("127.0.0.1", 1338))
  val oscRecv = OSCCommunication.createOSCReceiver(UDP, new InetSocketAddress("127.0.0.1", 1339))

  oscRecv.receipt.observe({ x=>
    val msg = x._1

    if(msg.name.contains("64")) {
      val tick = msg.args(0).asInstanceOf[Int]
      this.updateHighlighting(tick)

      if(progressCircle.isDefined) {
        val circle = progressCircle.get
        val deg = (tick.toFloat/64f)*360f

        circle.setDegrees(deg)
        circle.create()

      }
    }

    true
  })

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
    (1288f/1920f, 952f/1080f)
//    (1575f/1920f, 852f/1080f)
  // convert to absolute coordinates, flip y first
  ).map(x => ( x._1 , (1.0f - x._2) ) )

  // mapping definition
  val mappings = List(
    // encoding osc name, value range, deviation range (for mutation), tick when active in bar
    (List("/tempo"), List((120,300)), List(10), None, 0),
    (List("/rootnote","/scale"), List((40,52),(1,8)), List(3, 1), None , 0),
    (List("/drum_kick_bar1","/drum_snare_bar1","/drum_hh_bar1"), List((1,4),(1,4),(1,8)), List(1,1,2), Some(0,15 ), 2),
    (List("/drum_kick_bar2","/drum_snare_bar2","/drum_hh_bar2"), List((1,4),(1,4),(1,8)), List(1,1,2), Some(16,31), 2),
    (List("/drum_kick_bar3","/drum_snare_bar3","/drum_hh_bar3"), List((1,4),(1,4),(1,8)), List(1,1,2), Some(32,47), 2),
    (List("/drum_kick_bar4","/drum_snare_bar4","/drum_hh_bar4"), List((1,4),(1,4),(1,8)), List(1,1,2), Some(48,64), 2),
    (List("/ch_rhyt1","/ch_root1","/ch_voic1"), List((1,4), (0,6), (1,8)), List(1,1,2), Some(0,15 ), 3),
    (List("/ch_rhyt2","/ch_root2","/ch_voic2"), List((1,4), (0,6), (1,8)), List(1,1,2), Some(16,31), 3),
    (List("/ch_rhyt3","/ch_root3","/ch_voic3"), List((1,4), (0,6), (1,8)), List(1,1,2), Some(32,47), 3),
    (List("/ch_rhyt4","/ch_root4","/ch_voic4"), List((1,4), (0,6), (1,8)), List(1,1,2), Some(48,64), 3),
//    (List("/ch_indices"), List((1,7)), List(1), None, 4),
    (List("/mel_bar1","/mel_bar2","/mel_bar3","/mel_bar4"),List((1,16),(1,16),(1,16),(1,16)), List(3,3,3,3), None , 5),
    (List("/bass_bar1","/bass_bar2","/bass_bar3","/bass_bar4"),List((1,16),(1,16),(1,16),(1,16)), List(3,3,3,3), None, 1)
  );

  var controllerGlue : List[ControlGlue] = List();
  var voters : List[VotingPanel] = List()

  var votes : List[Float] = List.empty

  def vote(vote: Float) = {
    votes = votes :+ vote

    if(voters.forall(p => !p.isPanelEnabled)){
      var mean = votes.sum / votes.size

      println("votes" + votes + " -> "+mean)
      SessionLogger.log("Voting", SessionLogger.SessionEvent.Event, this, this, votes)


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
    //population.add(chromosome, score-0.01)
  }

  def initializePopulation = {
    val r = new Random

    (0 to 10).foreach{ x =>
      val score = 0.8 - (r.nextDouble()*0.1) // high scores = bad fitness
    val genes = controllerGlue.map( {x=>
      // also update gene pool fitnessess from voting
        val gene = x.generateRandomGene()
        gene
      })

      val chromosome = new Chromosome(genes, mutation = SimpleChromosomeMutation2)

      population.add(chromosome, score)
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

    // force sending current values again
    controllerGlue.foreach(x => x.bang)
  }

  def updateHighlighting(currentSubdivision: Int) = {
    controllerGlue.foreach{x => x.updateHighlighting(currentSubdivision)}
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

  // scale factors: forms & ellipse layout
  val ftr = 0.8f
  val sftr = 1.3f

  val mftr= (1.0f-ftr)/2f

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

    val form = SeqNodeForm(Vec3d((width*mftr)+x._1*width*ftr, (height*mftr)+x._2*height*ftr,120f), mapping._2._5)
    //val form = RandomNodeForm(Vec3d((width*mftr)+x._1*width*ftr, (height*mftr)+x._2*height*ftr,120f))
    form.setLight(l)

    val glue = new ControlGlue(Mutator.oscTransmit,form, mapping._2._1, mapping._2._2, mapping._2._3, mapping._2._4)
    Mutator.controllerGlue = Mutator.controllerGlue :+ glue


    canvas += form;
    canvas += form.xCircle ++ form.yCircle ++ form.zCircle


    form.scaleGlobal(sftr, sftr, sftr ,form.getCenterPointGlobal())
    form.xCircle.scaleGlobal(sftr, sftr, sftr ,form.getCenterPointGlobal())
    form.yCircle.scaleGlobal(sftr, sftr, sftr ,form.getCenterPointGlobal())
    form.zCircle.scaleGlobal(sftr, sftr, sftr ,form.getCenterPointGlobal())

  }

  // create an initial population
  Mutator.initializePopulation
  Mutator.runGameCycle(0.0);

  // send triggers to pd
  Mutator.oscTransmit.send() = Message("/start", 1)
  Mutator.oscTransmit.send() = Message("/audio", 1)


  /*

  val foo = new MTSvg(Mutator, "test.svg")
  canvas += foo

  val fff = new Runnable{
    var ff = false
    override def run(): Unit = {
      while(true){
      println("eek "+ff)
      foo.setVisible(ff)
      ff = ff ^ true
      Thread.sleep(500)
      }
    }
  }

  (new Thread(fff)).start()
  */


  val appCenter = Vec3d(Mutator.width/2f, Mutator.height/2f)

  (1 to Mutator.noVoters).foreach {  x =>

    val coord = (appCenter.getSubtracted(Vec3d((( x+1 ) % 3 )*100,(( x ) % 3 )*100) ))
    coord.setZ(300);


    val vPanel = new VotingPanel(Vec3d(0,0,0))
    vPanel.scale(0.7f,0.7f,0.7f, vPanel.getCenterPointGlobal)

    val vPanelCenter = vPanel.getCenterPointGlobal

    vPanel.rotateZ(vPanelCenter, 90f*x)
    vPanel.setPositionGlobal(coord)


    Mutator.voters = Mutator.voters :+ vPanel

    vPanel.voted.observe({x => Mutator.vote(x); true})

    canvas += vPanel
  }

  var ellipse = new WrapperEllipse(Mutator, appCenter, Mutator.height/6, Mutator.height/6)
  //ellipse.setPositionGlobal(appCenter)
  ellipse.setPickable(false)
  ellipse.setFillColor(Color.PURPLE.opacity(0.4f))
  canvas += ellipse

  Mutator.progressCircle = Some(ellipse)



}
