package org.mt4j.test.css;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Before;
import org.junit.Test;
import org.mt4j.components.css.parser.CSSHandler;
import org.mt4j.components.css.style.CSSSelector;
import org.mt4j.components.css.style.CSSStyle;



public class CSSHandlerTest extends TestCase{

	Logger logger = Logger.getLogger("MT4J Extensions");
	SimpleLayout l = new SimpleLayout();
	ConsoleAppender ca = new ConsoleAppender(l);
	
		
	StartTestApp app = new StartTestApp();
	List<CSSStyle> styles = new ArrayList<CSSStyle>();
	CSSHandler cssh = new CSSHandler(app, styles);
	
	@Before
	public void setUp() {
		logger.addAppender(ca);
	}
	
	
	@Test 
	public void testProcessElement() {
		CSSSelector test = cssh.processElement("P.c141");
		logger.debug(test);
		test = cssh.processElement("P#c141");
		logger.debug(test);
		test = cssh.processElement("#P.c141");
		logger.debug(test);
		test = cssh.processElement("#P c141");
		logger.debug(test);
		test = cssh.processElement("#P                                .c141");
		logger.debug(test);
		
		
	}

}
