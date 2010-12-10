package basic.css.menus;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.css.parser.CSSParserConnection;
import org.mt4j.components.css.style.CSSSelector;
import org.mt4j.components.css.style.CSSStyle;
import org.mt4j.components.css.util.CSSStyleManager;
import org.mt4j.components.css.util.CSSTemplates;
import org.mt4j.components.css.util.CSSKeywords.CSSSelectorType;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4jx.components.visibleComponents.widgets.menus.MTHUD;
import org.mt4jx.components.visibleComponents.widgets.menus.MTHexagonMenu;
import org.mt4jx.components.visibleComponents.widgets.menus.MTSquareMenu;
import org.mt4jx.components.visibleComponents.widgets.menus.MenuItem;



import processing.core.PImage;


public class MenuExample  extends AbstractScene{
	List<CSSStyle> CSSStyleManager = new ArrayList<CSSStyle>();
	
	private MTComponent parent;
	private MTApplication app;

	CSSStyleManager cssm;
	Logger logger = Logger.getLogger("MT4J Extensions");
	SimpleLayout l = new SimpleLayout();
	ConsoleAppender ca = new ConsoleAppender(l);
	CSSParserConnection pc;
	List<CSSStyle> styles;
	MTColor w = MTColor.WHITE;
	
	
	public MenuExample(MTApplication mtApplication, String name) {
		super(mtApplication, name);

		this.app = mtApplication;
			
			//Set CSS Enabled for all components
			app.getCssStyleManager().setGloballyEnabled(true);

			//Load a different CSS Style for each component
			app.getCssStyleManager().loadStylesAndOverrideSelector(CSSTemplates.MATRIXSTYLE, new CSSSelector("MTHUD", CSSSelectorType.CLASS));
			app.getCssStyleManager().loadStylesAndOverrideSelector(CSSTemplates.BLUESTYLE, new CSSSelector("MTHexagonMenu", CSSSelectorType.CLASS));
			app.getCssStyleManager().loadStylesAndOverrideSelector(CSSTemplates.REDSTYLE, new CSSSelector("MTSquareMenu", CSSSelectorType.CLASS));
			
			logger.addAppender(ca);
			
			PImage p1 = app.loadImage("basic/css/data/p1.jpg");
			PImage p2 = app.loadImage("basic/css/data/p2.jpg");
			PImage p3 = app.loadImage("basic/css/data/p3.jpg");
			
			
			//Create Menu Items
			List<MenuItem> menus = new ArrayList<MenuItem>();
			menus.add(new MenuItem("Start", new gestureListener("Start")));
			menus.add(new MenuItem("Open", new gestureListener("Open")));
			menus.add(new MenuItem("Close", new gestureListener("Close")));
			menus.add(new MenuItem("Exit", new gestureListener("Exit")));
			menus.add(new MenuItem("Save", new gestureListener("Save")));
			menus.add(new MenuItem("Load", new gestureListener("Load")));
			menus.add(new MenuItem("Cancel", new gestureListener("Cancel")));
			menus.add(new MenuItem("Undo", new gestureListener("Undo")));
			menus.add(new MenuItem(p1, new gestureListener("Picture Item 1")));
			menus.add(new MenuItem(p2, new gestureListener("Picture Item 2")));
			menus.add(new MenuItem(p3, new gestureListener("Picture Item 3")));
			
			//Create Square Menu
			MTSquareMenu sm = new MTSquareMenu(app, new Vector3D(25,200),  menus, 75);
			this.getCanvas().addChild(sm);
			
			//Create Hexagon Menu
			MTHexagonMenu hm = new MTHexagonMenu(app, new Vector3D(500,200),  menus, 100);
			this.getCanvas().addChild(hm);
			
			//Create Heads up display (on bottom of the screen)
			MTHUD hud = new MTHUD(app,menus, 64, MTHUD.BOTTOM );
			this.getCanvas().addChild(hud);
			
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	public class gestureListener implements IGestureEventListener {
		String string;
		public gestureListener(String string) {
			super();
			this.string = string;
		}
		
		
		public boolean processGestureEvent(MTGestureEvent ge) {

			if (ge instanceof TapEvent) {
				TapEvent te = (TapEvent) ge;
				if (te.getTapID() == TapEvent.BUTTON_CLICKED) {
					System.out.println(string);
				}
			}
			return true;
		}
		
	}
}
