package org.lodsb.VPDSynth

import de.sciss.synth._
import de.sciss.synth.ugen._
import org.mt4j.output.audio.AudioServer


object VPDBW1 {
  val pi: GE = scala.math.Pi;

  def anti(x: GE, psxK: GE, psyK: GE, pxK: GE, pyK: GE): GE = {

    /*
    var psx = K2A.ar(psxK);
    var psy = K2A.ar(psyK);


    var px = K2A.ar(pxK);
    var py = K2A.ar(pyK);   */

    // NAN @ DIV 0?
    var dist1:GE = (pxK - psxK) + 0.00001;
    var coeff1:GE = (pyK - psyK) / dist1;

    var phase: GE = psyK + ((x - psxK) * coeff1);
    phase = Select.ar(CheckBadValues.ar(phase, 0, 0), Seq(phase, DC.ar(0), DC.ar(0), phase));

    var main:GE = -1.0 * SinOsc.ar(0, ((phase * 2 * pi) + (pi / 2)) % (2 * pi));

    main;

  };



  def ar(freq:GE, psxK:GE, psyK:GE, pxK:GE, pyK:GE, pexK:GE, peyK:GE) : GE = {
    var ttrig: GE = Impulse.ar(freq)
    var sr:GE     = SampleRate.ir
    var x:GE      = Phasor.ar(ttrig, freq/sr)

    val larger : GE = (x > pxK)

    var psx = K2A.ar(psxK);
    var psy = K2A.ar(psyK);

    var px = K2A.ar(pxK);
    var py = K2A.ar(pyK)

    var pex = K2A.ar(pexK);
    var pey = K2A.ar(peyK)


    val sa1 = anti(x, psx, psy, px, py)
    val sa2 = anti(x, pex, pey, px, py)

    var siganti: GE = Select.ar(larger, Seq(sa1,sa2))
    var out = Select.ar(CheckBadValues.ar(siganti, 0, 0), Seq(siganti, DC.ar(0), DC.ar(0), siganti))

    //var out = siganti;
    out

  }
  import org.mt4j.output.audio.AudioServer._

}
