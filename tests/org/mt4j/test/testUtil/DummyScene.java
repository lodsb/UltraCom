package org.mt4j.test.testUtil;

import org.mt4j.MTApplication;
import org.mt4j.sceneManagement.AbstractScene;

public class DummyScene extends AbstractScene {

	public DummyScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
	}

	@Override
	public void init() {

	}

	@Override
	public void shutDown() {

	}

}
