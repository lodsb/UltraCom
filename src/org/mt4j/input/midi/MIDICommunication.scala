/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 9 :: 10 : 35
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
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

package org.mt4j.input.midi

import org.mt4j.input.{TraitOutputSink, TraitInputSource}

case class MidiMsg(channel: Int)
case class MidiCtrlMsg(chan: Int, num: Int, data: Float) extends MidiMsg(chan)

trait TraitMidiInputSource extends TraitInputSource[MidiMsg, Nothing]
trait TraitMidiOutputSink extends TraitOutputSink[MidiMsg]

import javax.sound.midi._

object MidiCommunication {

	class MidiInput extends Receiver with TraitMidiInputSource {
		def close {
			println("Input closed")
		}

		def send(m: MidiMessage, time: Long) {
			m match {
				case sm: ShortMessage if (sm.getCommand() == ShortMessage.CONTROL_CHANGE) =>
					val ch = sm.getChannel()
					val num = sm.getData1()
					val v = (sm.getData2().floatValue)/127.0f

					this.receipt.emit(MidiCtrlMsg(ch,num,v))
			}
		}
	}

	class MidiOutput(private val receiver: Receiver) extends TraitMidiOutputSink {
		def senderAction(message: MidiMsg): Boolean =  {
			message match {
				case MidiCtrlMsg(cha, num, v) => {
					val msg = new ShortMessage
					val value = (v*127.0f).toInt
					msg.setMessage(ShortMessage.CONTROL_CHANGE, cha, num, value);
					this.receiver.send(msg, -1)
				}

				case _ => println("This MidiMessage-Type is NOT YET SUPPORTED")
			}

			true
		}

		this.sendAction = senderAction
	}

	def knownDevices = MidiSystem.getMidiDeviceInfo

	def createMidiInput(deviceName: String): Option[MidiInput] = {
		val DUMP_IN = false
		val DUMP_OUT = false
		val infos = knownDevices

		var ret: Option[MidiInput] = None;

		infos.foreach(x => println(x.getDescription))

		val inDevO = infos.filter(_.getDescription == deviceName).map(MidiSystem.getMidiDevice(_)).find(_.getMaxTransmitters() != 0)
		try {
			inDevO match {
				case Some(inDev) =>
					inDev.open
					val t = inDev.getTransmitter()
					val input = new MidiInput
					t.setReceiver(input)
					ret = Some(input)
				case _ =>
					if (inDevO.isEmpty) println("No input device '" + deviceName + "' found!")
			}
		} catch {
			case e =>
				println("Error initializing MIDI: ")
				e.printStackTrace()
		}

		ret
	}

	def createMidiOutput(deviceName: String): Option[MidiOutput] = {
		val DUMP_IN = false
		val DUMP_OUT = false
		val infos = knownDevices

		var ret: Option[MidiOutput] = None;

		infos.foreach(x => println(x.getDescription))

		val outDevO = infos.filter(_.getDescription == deviceName).map(MidiSystem.getMidiDevice(_)).find(_.getMaxReceivers() != 0)
		try {
			outDevO match {
				case Some(outDev) =>
					outDev.open
					val output = new MidiOutput(outDev.getReceiver)

					ret = Some(output)

				case _ =>
					if (outDevO.isEmpty) println("No output device '" + deviceName + "' found!")
			}
		} catch {
			case e =>
				println("Error initializing MIDI: ")
				e.printStackTrace()
		}

		ret
	}
}
