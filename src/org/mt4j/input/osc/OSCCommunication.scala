package org.mt4j.input.osc

/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 4 :: 2 : 43
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

import java.nio.channels.DatagramChannel
import java.net.{SocketAddress, InetSocketAddress}
import de.sciss.osc.{	UDP,TCP, Message => OSCMessage,
						Transport => OSCTransport,
						Transmitter=>OSCTransmitter,
						PacketCodec=>OSCPacketCodec}


object OSCCommunication {
	def createOSCReceiver(transport: OSCTransport, address: InetSocketAddress):  SignalingOSCReceiver = {
		val recv = transport match {
			case UDP => {
				new SignalingOSCReceiver(UDP.Receiver(address));
			}
			case TCP => {
				new SignalingOSCReceiver(TCP.Receiver(address));
			}
		}


		val actionFunction = (x:Tuple3[OSCMessage, SocketAddress, Long]) => recv.receipt.emit(x)

		recv.receiver.action = {(msg,addr,time) => recv.receipt.emit((msg,addr,time))}

		recv
	}

	def createOSCTransmitter(transport: OSCTransport, localAddress: InetSocketAddress,
						  codec: OSCPacketCodec = OSCPacketCodec.default): OSCTransmitter = {
		var trans = transport match {
			case UDP => {
				val cfg = UDP.Config();
				cfg.codec = OSCPacketCodec().doublesAsFloats().booleansAsInts();

				new Transmitter(UDP.Transmitter(localAddress, cfg));
			}

			case TCP => {
				val cfg = TCP.Config();
				cfg.codec = OSCPacketCodec().doublesAsFloats().booleansAsInts();

				new Transmitter(TCP.Transmitter(localAddress, cfg));
			}
		}

		trans.sendAction = { x=> trans ! x; println(x);true}

		trans
	}


}
