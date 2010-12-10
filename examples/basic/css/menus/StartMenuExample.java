package basic.css.menus;

import org.mt4j.MTApplication;


public class StartMenuExample extends MTApplication {
	public static void main(String[] args) {
		initialize();
	}
	
	
	@Override
	public void startUp() {
		// TODO Auto-generated method stub
		addScene(new MenuExample(this, "Integration  Test Scene"));
	}
}
