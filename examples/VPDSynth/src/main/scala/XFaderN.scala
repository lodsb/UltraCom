package org.lodsb.VPDSynth
import de.sciss.synth._
import de.sciss.synth.ugen._


object XFaderN {
  def ar(inputs: Seq[GE], bipolar: GE, width: GE = 2.0): GE = {

    var whiches:PanAz = PanAz.ar(inputs.size, SinOsc.ar(0.0) + 1.0, bipolar, width)

    var idx = 0;

    val out = (0 to inputs.size-1).map({i => whiches.\(i)*inputs(i)})

    /*val out = MapExpanded(whiches)( {x =>
      x.map { pz => pz*inputs(idx) }
      idx = idx + 1
      x
    }
    );*/

    Mix.mono(out)
  }

}
