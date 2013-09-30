package org.lodsb.VPDSynth
import de.sciss.synth._
import de.sciss.synth.ugen._
import org.lodsb.VPDSynth._

object VPDSynth {
  val r = new scala.util.Random
  def rrand(l: Double, h: Double) = (r.nextFloat()*(h-l))+l


  def ar(gate: GE = 1.0, cleanFmRingmod : GE= 0,
         modFreqMult: GE = 1,
         frequency: GE=440,
         ampEnvType:GE=0,
         carrierVPSYType:GE=0,
         modulatorVPSYType:GE = 0,
         carrierVPSXType:GE=0,
         modulatorVPSXType:GE = 0,
         carrierVPSYWeight:GE=0,
         modulatorVPSYWeight:GE = 0,
         carrierVPSXWeight:GE=0,
         modulatorVPSXWeight:GE = 0,
         fmModType:GE = 0,
         fmModIdx:GE = 0,
         noiseAmount:GE=0,
         fxRouteType:GE=0) : GE = {



   	var modFreqArr = Seq(1.0/9, 1.0/8, 1.0/7, 1.0/5, 1.0/4, 1.0/3,
                            1.0/2, 1.001, 1.0+1/9, 1.0+1/8, 1.0+1/7,
                            1.0+1/5, 1.0+1/4, 1.0+1/3, 1.0+1/2, 2.001);
    //var modFreqArrSize = 16;

    val local: GE = LocalIn.ar(1)


    var e00: GE = 0.0;
    var e01: GE = 1.0;
    var e1 = EnvGen.kr(Env.perc(0.001, 0.3, 1, Curve.parametric(-4)), gate);
   	var e2 = EnvGen.kr(Env.perc(0.01, 0.8, 1, Curve.parametric(-4)), gate);
   	var e3 = EnvGen.kr(Env.perc(0.2, 1.2, 1,  Curve.parametric(-4)), gate);
   	var e4 = EnvGen.kr(Env.perc(0.5, 1.5, 1, Curve.parametric(-4)), gate);
   	var e5 = EnvGen.kr(Env.perc(0.8, 2.5, 1, Curve.parametric(-2)), gate);
   	var e6 = EnvGen.kr(Env.perc(1.6, 2.0, 1, Curve.parametric(0)), gate);
   	var e7 = EnvGen.kr(Env.perc(2, 2.0, 1, Curve.parametric(2)), gate);
   	var e8 = EnvGen.kr(Env.perc(2,2, 1 ,Curve.parametric(4)), gate);
   	var e9 = EnvGen.kr(Env.perc(2,2, 1 ,Curve.parametric(8)), gate);


    var envs = Seq(e00, e01, e1, e2, e3, e4, e5, e6, e7,
                  e8, e9, 1.0-e1, 1.0-e2, 1.0-e3, 1.0-e4, 1.0-e5,
                  1.0-e6, 1.0-e7, 1.0-e8, 1.0-e9)


    var ampEnv = Select.kr(envs.size*ampEnvType, envs);
    var carrierVPSYEnv = Select.kr(envs.size*carrierVPSYType, envs);

    var	modulatorVPSYEnv = Select.kr(envs.size*modulatorVPSYType, envs);
    var fmModEnv = Select.kr(envs.size*fmModType, envs);

    var	carrierVPSXEnv = Select.kr(envs.size*carrierVPSXType, envs);



    var	modulatorVPSXEnv = Select.kr(envs.size*modulatorVPSXType, envs);
    	//frequency = frequency*pitchEnv;


    var modFreq = Select.kr(modFreqArr.size*modFreqMult, modFreqArr)*frequency;

    	// fm offset is missing!

    var modulator = VPDBW1.ar(modFreq, 0.0, 0.0, modulatorVPSXWeight*modulatorVPSXEnv,
                    modulatorVPSYWeight*modulatorVPSYEnv, 0.8,3.0);


    var carrierFM = VPDBW1.ar(frequency+( fmModEnv*modulator*fmModIdx ), 0.0, 0.0, carrierVPSXWeight*carrierVPSXEnv,
                    carrierVPSYWeight*carrierVPSYEnv, 1.0,1.0);

    var carrier = VPDBW1.ar(frequency, 0.0, 0.0, carrierVPSXWeight*carrierVPSXEnv, carrierVPSYWeight*carrierVPSYEnv,
                  1.0,1.0);

    var oscMix = XFaderN.ar(Seq(carrier, 0.5*(carrier+modulator), (carrier*modulator), carrierFM), cleanFmRingmod, 1) + (1.0*noiseAmount*WhiteNoise.ar());

    var ampOut = 0.5*oscMix*ampEnv;


    	// flanger
    var flangeSig = AllpassC.ar(ampOut + (local*0.00), 0.3,
                    ((LFPar.kr(0.6, 0.0)*0.008) + 0.003*0.04).abs, 0.001);

    var flangeMix = ampOut+flangeSig;
    LocalOut.ar(flangeMix);



    // first sig -> flangerWet -> reverb -> chorus
    val numChorusDelays = 36;
    var reverbSig = LeakDC.ar(FreeVerb.ar(flangeSig, 3, 0.95, 20)*24 + 8);
    val chorusIn = reverbSig* 1.0/numChorusDelays;

    val chorusModulators = (0 to numChorusDelays).map {i => LFPar.kr(0.5* rrand(0.64, 1.06), 0.5 * i)* 0.5 + 0.08}

    val	chorusSig = Mix.mono(DelayC.ar(chorusIn, chorusModulators));
    val fx2:GE = 0.125*chorusSig;

    // second sig -> flangerMix -> chorus -> reverb
    val chorusIn2 = flangeMix*numChorusDelays.reciprocal;
    val chorusSig2 = Mix.mono(DelayC.ar(chorusIn2, chorusModulators));
    reverbSig = LeakDC.ar(FreeVerb.ar(chorusSig2, 3, 0.95, 20) * 24 + 8);
    val fx3:GE = 0.125*reverbSig;


    val outChannels:GE = Seq(0.5*ampOut, 0.5*flangeMix, fx2, fx3);
    val outChannelsSize = 4;

    var out:GE = Select.ar(outChannelsSize*fxRouteType, outChannels);



    out = Limiter.ar(out);

    //var out = Limiter.ar(ampOut);


    out
  }

}
