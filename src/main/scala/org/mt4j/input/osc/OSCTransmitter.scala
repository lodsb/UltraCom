/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 4 - 18 :: 8 : 32
    >>  Origin: mt4j (project) / mt4j_mod (module)
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

package org.mt4j.input.osc

import de.sciss.osc.{Packet, Transmitter, Channel}
import de.sciss.osc.Channel.Directed.Output


class OSCTransmitter(private val transmitter:  Output) extends TraitTransmitOSCComm {
    def close() = transmitter.close()
    def isConnected() = transmitter.isConnected
}

