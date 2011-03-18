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
import de.sciss.osc._
import impl.{TCPTransmitter, UDPTransmitter, UDPReceiver, TCPReceiver}
import java.net.{SocketAddress, InetSocketAddress}
import react._



;
;
;






object OSCCommunication {
	def createOSCReceiver(transport: OSCTransport, address: InetSocketAddress, codec: OSCPacketCodec = OSCPacketCodec.default):  SignalingOSCReceiver = {
		val recv = transport match {
			case UDP => new SignalingUDPOSCReceiver(address, codec)
			case TCP => new SignalingTCPOSCReceiver(address, codec)
		}


		val actionFunction = (x:Tuple3[OSCMessage, SocketAddress, Long]) => recv.receipt.emit(x)

		recv.action = {(msg,addr,time) => recv.receipt.emit((msg,addr,time))}

		recv
	}

	def createOSCTransmitter(transport: OSCTransport, localAddress: InetSocketAddress,
						  codec: OSCPacketCodec = OSCPacketCodec.default): OSCTransmitter = {
		var trans = transport match {
			case UDP => new RichUDPTransmitter(localAddress, codec)
			case TCP => new RichTCPTransmitter(localAddress, codec)
		}

		trans.sendAction = { x=> trans ! x; println(x);true}

		trans
	}


}
