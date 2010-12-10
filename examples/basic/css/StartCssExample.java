package basic.css;

import org.mt4j.MTApplication;


public class StartCssExample extends MTApplication {
	public static void main(String[] args) {
		initialize();
	}
	
	
	@Override
	public void startUp() {
		// TODO Auto-generated method stub
		addScene(new CssExample(this, "Integration  Test Scene"));
	}
}
