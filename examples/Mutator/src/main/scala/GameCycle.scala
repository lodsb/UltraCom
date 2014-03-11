package mutator

import mutant5000._
import scala.util.Random

/**
 * Created by lodsb on 3/3/14.
 */
object GameCycle {

  private val r = new Random

  def evolve(population: Population,
             elitism: Double,
             crossOverProbability: Double,
             mutationProbability: Double,
             keepBest: Int = 6,
             prettyPrint: Boolean = true,
             earlyStop: Boolean = true) : Chromosome = {

    var generation = 0
    var done = false

    var ret : Chromosome = null

    println("population size..."+population.size)

    if(population.size >= 10) {
      population.keepBest(keepBest)
    }

    while (!(done && earlyStop)) {
      println("looking for someone..."+population.size)
      println(population.printChromosomeChck)
      val mates = population.mates(crossOverProbability, elitism)
      println("done...")

      if (mates.isDefined) {

        var offspring: Seq[Chromosome] = mates.get.map {
          couple =>
            couple._1 |+| couple._2
        }

        println("MUTATION PROB "+mutationProbability)

        if (mutationProbability > 0) {
          offspring = offspring.map {
            o => println(o.mutation);o.mutate(mutationProbability)//SimpleChromosomeMutation2.apply(o, mutationProbability)//o.mutate(mutationProbability)
          }
        }

        if (prettyPrint) {
          println("****\nCycle: " + generation + "\nPopulation Size: " + population.size + "\nBest: " + population.best + "\n")
        }


        // if we have some offspring, then we are done, they are added to the pool externally before the next round
        done = true
        ret = offspring(0)//r.nextInt(offspring.size-1))
        println("offspring")
        offspring.foreach({x=> println(x.chckString)})

        println("RETURN: "+ret.chckString)

      }

      generation = generation + 1
    }

    ret
  }
}

object SimpleChromosomeMutation2 extends ChromosomeMutation {
  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
  println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")


  override def apply(v1: Chromosome, prob: Double): Chromosome = {
    // hack to increase probability of mutation
    val probability = prob  // / v1.genes.length
    println("\n\n\n!!!!!!!!!!!!!!!!!!!!! overall prob !!!!!!!!!!!! "+prob)
    println("\n\n\n")
    val smuta = v1.genes.map(x => x.mutate(probability))

    new Chromosome(smuta, mutation = SimpleChromosomeMutation2)
  }
}

class IntegerEncodingMutation2(min: Int, max: Int, mutationDeviation: Int) extends EncodingMutation {
  private val r = scala.util.Random
  def apply(that: Encoding, prob: Double): Encoding = {
    println("called mutation "+prob + " "+ mutationDeviation)
    that match {
      case v1: IntegerEncoding => {

        val intValue: Int = if(r.nextDouble() < prob) {
          v1.toInt
        } else {
          val curDeviation = r.nextInt(mutationDeviation)
          val res = if(r.nextBoolean()) {
            v1.toInt + curDeviation
          } else {
            v1.toInt - curDeviation
          }

          scala.math.min(max, scala.math.max(min, res))
        }

        println("mutated!" + v1.toInt + " -> " + intValue)
        v1.copy(v=intValue)
      }

      case _ => that
    }
  }
}

object MultipleConcatenatingGeneCombination extends GeneCombination {
  private val random = scala.util.Random

  def combine(x: Gene, y: Gene): Gene = {
    val pivot1 = random.nextInt(x.sequence.length)
    val scombination1 = x.sequence.slice(0, pivot1) ++ y.sequence.slice(pivot1, y.sequence.length)

    val pivot2 = random.nextInt(x.sequence.length)
    val scombination2 = x.sequence.slice(0, pivot2) ++ y.sequence.slice(pivot2, y.sequence.length)

    val pivot3 = random.nextInt(x.sequence.length)
    val scombination3 = scombination1.slice(0, pivot3) ++ scombination2.slice(pivot3, scombination2.length)

    x.copy(encodings = scombination3)
  }
}

