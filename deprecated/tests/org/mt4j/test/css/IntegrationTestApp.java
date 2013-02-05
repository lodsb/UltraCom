package org.mt4j.test.css;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.components.MTCanvas;
import org.mt4j.components.MTComponent;
import org.mt4j.components.css.parser.CSSParserConnection;
import org.mt4j.components.css.style.CSSStyle;
import org.mt4j.components.css.util.*;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTBackgroundImage;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.test.testUtil.DummyScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vertex;
import processing.core.PImage;

public class IntegrationTestApp extends AbstractScene{
	List<CSSStyle> CSSStyleManager = new ArrayList<CSSStyle>();
	
	private MTComponent parent;
	private MTApplication app;

	CSSStyleManager cssm;
	Logger logger = Logger.getLogger("MT4J Extensions");
	SimpleLayout l = new SimpleLayout();
	ConsoleAppender ca = new ConsoleAppender(l);
	CSSParserConnection pc;
	List<CSSStyle> styles;
	MTColor w = new MTColor(255,255,255,255);
	
	public IntegrationTestApp(MTApplication mtApplication, String name) {
		super(mtApplication, name);

		this.app = mtApplication;
			//this.getCanvas().addChild(new MTBackgroundImage(app, app.loadImage("256x256.jpg"), true));
			
			
			//Set up components
			parent = new MTComponent(app);
			this.getCanvas().addChild(parent);
			
			logger.addAppender(ca);
			
			pc = new CSSParserConnection("junit/integrationtest.css", app);
			styles= pc.getCssh().getStyles();
			cssm = new CSSStyleManager(styles, app);
		
			MTRectangle r = new MTRectangle(app, 500, 500, 500, 500);
			r.enableCSS();
			this.getCanvas().addChild(r);

			
			Vertex[] vtcs = {new Vertex(100,100), new Vertex(200, 20), new Vertex(300, 200) ,new Vertex(100,100)};
			MTPolygon p = new MTPolygon(app, vtcs);
			this.getCanvas().addChild(p);
			p.enableCSS();
			
	}


	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
