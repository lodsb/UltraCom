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
import de.sciss.osc.{Message => OSCMessage, UDP => OSCviaUDP, TCP => OSCviaTCP, Transport => OSCTransport, PacketCodec => OSCPacketCodec, Receiver, Channel, Packet, Transmitter}


object OSCCommunication {
	class OSCTransportType;
	case object UDP extends OSCTransportType;
	case object TCP extends OSCTransportType;

	def createOSCReceiver(transport: OSCTransportType, address: InetSocketAddress):  SignalingOSCReceiver = {
		val recv = transport match {
			case UDP => {
        val cfg = OSCviaUDP.Config()
        // FIXME: dont use broadcasts only
        cfg.localPort = address.getPort
				val r: OSCviaUDP.Receiver.Undirected = OSCviaUDP.Receiver(cfg);

				val sr = new SignalingOSCReceiver(r);
				r.action = {case x => sr.receipt.emit(x)};
				r.connect()
				sr
			}
			case TCP => {
        val cfg = OSCviaTCP.Config()
        cfg.localPort = address.getPort
        val r: OSCviaTCP.Receiver = OSCviaTCP.Receiver(address)

        val sr = new SignalingOSCReceiver(r);
        r.action = {case x => println(x);sr.receipt.emit(x)};
				r.connect()
				sr
			}
		}

		recv
	}

	def createOSCTransmitter(transport: OSCTransportType, localAddress: InetSocketAddress,
						  codec: OSCPacketCodec = OSCPacketCodec.default): OSCTransmitter = {
		var trans = transport match {
			case UDP => {
				val cfg = OSCviaUDP.Config();
				cfg.codec = OSCPacketCodec().doublesAsFloats().booleansAsInts();
				val t = OSCviaUDP.Transmitter(localAddress, cfg)

				val st = new OSCTransmitter(t);
				st.sendAction =  { x:Packet => t.!(x); println(x);true}
				t.connect()
				st
			}

			case TCP => {
				val cfg = OSCviaTCP.Config();
				cfg.codec = OSCPacketCodec().doublesAsFloats().booleansAsInts();
				val t = OSCviaTCP.Transmitter(localAddress, cfg)
				val st = new OSCTransmitter(t);

				st.sendAction =  { x=> t.!(x); println(x);true}
				t.connect()
				st
			}
		}

		trans
	}


}
