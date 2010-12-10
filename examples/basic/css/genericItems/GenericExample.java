package basic.css.genericItems;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4jx.components.visibleComponents.widgets.MTCheckbox;
import org.mt4jx.components.visibleComponents.widgets.MTOptionBox;
import org.mt4jx.components.visibleComponents.widgets.MTSuggestionTextArea;
import org.mt4jx.components.visibleComponents.widgets.OptionGroup;
import org.mt4jx.components.visibleComponents.widgets.menus.MTHUD;
import org.mt4jx.components.visibleComponents.widgets.menus.MenuItem;



import processing.core.PImage;


public class GenericExample  extends AbstractScene{
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
	
	
	public GenericExample(MTApplication mtApplication, String name) {
		super(mtApplication, name);

		this.app = mtApplication;
		
					
			app.getCssStyleManager().setGloballyEnabled(true);
			app.getCssStyleManager().loadStylesAndOverrideSelector(CSSTemplates.MATRIXSTYLE, new CSSSelector("Group A", CSSSelectorType.ID));
			app.getCssStyleManager().loadStylesAndOverrideSelector(CSSTemplates.BLUESTYLE, new CSSSelector("Group B", CSSSelectorType.ID));
			
			logger.addAppender(ca);
			
			//Group A
			//Set CSSID of all elements
			
			//Create Checkbox
			MTCheckbox a_cb = new MTCheckbox(app, 40);
			a_cb.setCSSID("Group A");
			
			this.getCanvas().addChild(a_cb);
			
			
			//Create OptionGroup, add two OptionBoxes
			OptionGroup a_group = new OptionGroup();
			MTOptionBox a_box1 = new MTOptionBox(app,40, a_group);
			a_box1.setCSSID("Group A");
			MTOptionBox a_box2 = new MTOptionBox(app,40, a_group);
			a_box2.setCSSID("Group A");
			this.getCanvas().addChild(a_box1);
			this.getCanvas().addChild(a_box2);
			
			

			//Prepare Suggestions for MTSuggestionTextArea
			String[] su = new String[] {"Nested ", "Class", "Summary", "MTListCell", "visibleComponents" };
			List<String> suggestions = Arrays.asList(su);

			//Create MTsuggestionTextArea
			MTSuggestionTextArea a_sta = new MTSuggestionTextArea(app, 200, suggestions);
			a_sta.setCSSID("Group A");
			this.getCanvas().addChild(a_sta);

			//Position all Elements
			a_cb.setAnchor(PositionAnchor.UPPER_LEFT);
			a_cb.setPositionGlobal(new Vector3D(50,100));

			a_box1.setAnchor(PositionAnchor.UPPER_LEFT);
			a_box1.setPositionGlobal(new Vector3D(50,300));
			
			a_box2.setAnchor(PositionAnchor.UPPER_LEFT);
			a_box2.setPositionGlobal(new Vector3D(150,300));
			
			a_sta.setAnchor(PositionAnchor.UPPER_LEFT);
			a_sta.setPositionGlobal(new Vector3D(50,500));
			
			//Group B
			
			MTCheckbox b_cb = new MTCheckbox(app, 40);
			b_cb.setCSSID("Group B");
			this.getCanvas().addChild(b_cb);
			
			OptionGroup b_group = new OptionGroup();
			MTOptionBox b_box1 = new MTOptionBox(app,40, b_group);
			b_box1.setCSSID("Group B");
			MTOptionBox b_box2 = new MTOptionBox(app,40, b_group);
			b_box2.setCSSID("Group B");
			this.getCanvas().addChild(b_box1);
			this.getCanvas().addChild(b_box2);
			
		
			MTSuggestionTextArea b_sta = new MTSuggestionTextArea(app, 400);
			b_sta.setCSSID("Group B");
			this.getCanvas().addChild(b_sta);
			
			//Position all Elements
			b_cb.setAnchor(PositionAnchor.UPPER_LEFT);
			b_cb.setPositionGlobal(new Vector3D(550,100));

			b_box1.setAnchor(PositionAnchor.UPPER_LEFT);
			b_box1.setPositionGlobal(new Vector3D(550,300));
			
			b_box2.setAnchor(PositionAnchor.UPPER_LEFT);
			b_box2.setPositionGlobal(new Vector3D(650,300));
			
			b_sta.setAnchor(PositionAnchor.UPPER_LEFT);
			b_sta.setPositionGlobal(new Vector3D(550,500));
			
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
