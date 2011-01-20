package org.mt4j.input.inputSources.osc;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.input.inputData.MTComponent3DInputEvent;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.input.inputSources.TuioInputSource;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;
import de.sciss.net.OSCServer;

public class OSCInputSource extends AbstractInputSource<MTOSCControllerInputEvt> implements OSCListener {
	private static final Logger logger = Logger.getLogger(TuioInputSource.class.getName());
	static{
		logger.setLevel(Level.INFO);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	// only supports one listener per url
	private OSCServer server;
	private static final int defaultPort = 59000;
	
	private static OSCInputSource instance = null;
	
	public OSCInputSource(MTApplication app){
		this(app, OSCInputSource.defaultPort);
	} 
	
	public OSCInputSource(MTApplication app, int port) {
		super(app);
		try {										
			server = OSCServer.newUsing(OSCServer.UDP,port);
			server.start();
			server.addOSCListener(this);
			
			logger.info("Initialized OSC input on port "+port);
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to initialize OSCServer @ port "+port);
		}
	}

	@Override
	public void messageReceived(OSCMessage message, SocketAddress arg1, long arg2) {
		this.enqueueInputEvent(new MTOSCControllerInputEvt(this, message));
	}
}
