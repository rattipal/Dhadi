package com.att.g1;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

public class MainActivity extends BaseGameActivity {
	
	private final int CAMERA_WIDTH = 800;
	private final int CAMERA_HEIGHT = 480;
	private Camera camera;
	private SceneManager sceneManager;
	private Engine engine;
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) 
	{
	    engine = new LimitedFPSEngine(pEngineOptions, 60);
	    return engine;
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(Math.round(camera.getWidth()), Math.round(camera.getHeight())), camera);
		return engineOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		
		sceneManager = new SceneManager(engine, this, getVertexBufferObjectManager() ,camera);
		sceneManager.loadSplashScene();
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {

		sceneManager.createSplashScene(pOnCreateSceneCallback);

	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {

		mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
	    {
	            public void onTimePassed(final TimerHandler pTimerHandler) 
	            {
	                mEngine.unregisterUpdateHandler(pTimerHandler);
	                // load menu resources, create menu scene
	                // set menu scene using scene manager
	                // dispose current scene
	                sceneManager.loadMenuScene();
	                sceneManager.createMenuScene();
	                sceneManager.goToMenuScene();
	                sceneManager.disposeSplashScene();

	            }
	    }));
	    pOnPopulateSceneCallback.onPopulateSceneFinished();

	}

}
