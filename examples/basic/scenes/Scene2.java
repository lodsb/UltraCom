package basic.scenes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.mt4j.MTApplication;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.Iscene;
import org.mt4j.sceneManagement.transition.FadeTransition;
import org.mt4j.sceneManagement.transition.FlipTransition;
import org.mt4j.sceneManagement.transition.SlideTransition;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.opengl.GLFBO;

import processing.core.PImage;

public class Scene2 extends AbstractScene {

	private MTApplication mtApp;
	protected Iscene scene3;
	
//	private String imagePath = System.getProperty("user.dir") + File.separator + "examples"+  File.separator +"basic"+  File.separator + "scenes" + File.separator + "data" + File.separator;
	private String imagePath =  "basic"+  MTApplication.separator + "scenes" + MTApplication.separator + "data" + MTApplication.separator;

	public Scene2(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.mtApp = mtApplication;
		
		//Set the background color
		this.setClearColor(new MTColor(188, 150, 146, 255));
		
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));
		
		//Create a textfield
		MTTextArea textField = new MTTextArea(mtApplication, FontManager.getInstance().createFont(mtApplication, "arial.ttf", 
				50, new MTColor(255, 255, 255, 255), new MTColor(255, 255, 255, 255))); 
		textField.setNoFill(true);
		textField.setNoStroke(true);
		textField.setText("Scene 2");
		this.getCanvas().addChild(textField);
		textField.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height/2f));
		
		//Button to change to the previous scene on the scene stack
		PImage arrow = mtApplication.loadImage(imagePath + "arrowRight.png");
		MTImageButton previousSceneButton = new MTImageButton(mtApplication, arrow);
		previousSceneButton.setNoStroke(true);
		if (MT4jSettings.getInstance().isOpenGlMode())
			previousSceneButton.setUseDirectGL(true);
		previousSceneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getID()) {
				case TapEvent.BUTTON_CLICKED:
					mtApp.popScene();
					break;
				default:
					break;
				}
			}
		});
		getCanvas().addChild(previousSceneButton);
		previousSceneButton.scale(-1, 1, 1, previousSceneButton.getCenterPointLocal(), TransformSpace.LOCAL);
		previousSceneButton.setPositionGlobal(new Vector3D(previousSceneButton.getWidthXY(TransformSpace.GLOBAL) + 5, mtApp.height - previousSceneButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));
		
		//Button to get to the next scene
		MTImageButton nextSceneButton = new MTImageButton(mtApplication, arrow);
		nextSceneButton.setNoStroke(true);
		if (MT4jSettings.getInstance().isOpenGlMode())
			nextSceneButton.setUseDirectGL(true);
		nextSceneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getID()) {
				case TapEvent.BUTTON_CLICKED:
					//Save the current scene on the scene stack before changing
					mtApp.pushScene();
					if (scene3 == null){
						scene3 = new Scene3(mtApp, "Scene 3");
						mtApp.addScene(scene3);
					}
					//Do the scene change
					mtApp.changeScene(scene3);
					break;
				default:
					break;
				}
			}
		});
		getCanvas().addChild(nextSceneButton);
		nextSceneButton.setPositionGlobal(new Vector3D(mtApp.width - nextSceneButton.getWidthXY(TransformSpace.GLOBAL) - 5, mtApp.height - nextSceneButton.getHeightXY(TransformSpace.GLOBAL) - 5, 0));

		//Set a scene transition - Flip transition only available using opengl supporting the FBO extenstion
		if (MT4jSettings.getInstance().isOpenGlMode() && GLFBO.isSupported(mtApp))
			this.setTransition(new SlideTransition(mtApp, 700)); 
		else{
			this.setTransition(new FadeTransition(mtApp));
		}
	}

	@Override
	public void init() {
		System.out.println("Entered scene: " +  this.getName());
	}

	@Override
	public void shutDown() {
		System.out.println("Left scene: " +  this.getName());
	}

}
