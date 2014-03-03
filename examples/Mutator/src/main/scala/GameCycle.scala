package mutator

import mutant5000.{Chromosome, Population}

/**
 * Created by lodsb on 3/3/14.
 */
object GameCycle {

  def evolve(population: Population,
             elitism: Double,
             crossOverProbability: Double,
             mutationProbability: Double,
             keepBest: Int = 10,
             prettyPrint: Boolean = true,
             earlyStop: Boolean = true) : Chromosome = {

    var generation = 0
    var done = false

    var ret : Chromosome = null

    population.keepBest(keepBest)

    while (!(done && earlyStop)) {
      val mates = population.mates(crossOverProbability, elitism)

      if (mates.isDefined) {

        var offspring: Seq[Chromosome] = mates.get.map {
          couple =>
            couple._1 |+| couple._2
        }

        if (mutationProbability > 0) {
          offspring = offspring.map {
            o =>
              o.mutate(mutationProbability)
          }
        }

        if (prettyPrint) {
          println("****\nCycle: " + generation + "\nPopulation Size: " + population.size + "\nBest: " + population.best + "\n")
        }


        // if we have some offspring, then we are done, they are added to the pool externally before the next round
        done = true
        ret = offspring(0)

      }

      generation = generation + 1
    }

    ret
  }
}
