package org.mt4j.input.inputSources.osc;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;

import org.mt4j.MTApplication;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputSources.AbstractInputSource;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;
import de.sciss.net.OSCServer;

public class OSCInputSource extends AbstractInputSource implements OSCListener {
	// only supports one listener per url
	private OSCServer server;
	private static final int defaultPort = 59000;
	
	private static OSCInputSource instance = null;
	
	public OSCInputSource(MTApplication app, int port) {
		super(app);
		try {										
			server = OSCServer.newUsing(OSCServer.UDP,port);
			
			server.start();
			server.addOSCListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to initialize OSCServer @ port "+port);
		}
	}

	@Override
	public void messageReceived(OSCMessage message, SocketAddress arg1, long arg2) {
		this.enqueueInputEvent(new MTOSCControllerInputEvt(this, message));
	}
}
