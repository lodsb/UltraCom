package basic.css.genericItems;

import org.mt4j.MTApplication;


public class StartGenericExample extends MTApplication {
	public static void main(String[] args) {
		initialize();
	}
	
	
	@Override
	public void startUp() {
		// TODO Auto-generated method stub
		addScene(new GenericExample(this, "Integration  Test Scene"));
	}
}
