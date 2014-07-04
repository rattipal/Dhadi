package com.att.g1;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
import org.andengine.ui.activity.BaseGameActivity;

import android.app.Activity;

public class SceneManager {
	
	//---------------------------------------------
    // SCENES
    //---------------------------------------------
    
    private BaseScene splashScene;
    private BaseScene menuScene;
    private BaseScene gameScene;
    private BaseScene loadingScene;
    
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------
    
    private Engine engine;
    private BaseGameActivity activity;
	private VertexBufferObjectManager vbom;
	private Camera camera;
     
    private SceneType currentSceneType;
    private BaseScene currentScene;
    
    public SceneManager(Engine engine, BaseGameActivity activity,
			VertexBufferObjectManager vbom, Camera camera){
    	this.engine = engine;
    	this.activity = activity;
    	this.vbom = vbom;
    	this.camera = camera;
    	this.currentSceneType = SceneType.SCENE_SPLASH;
    	this.currentScene = new SplashScene(engine, activity, vbom, camera, this);
    }
    
    public enum SceneType
    {
        SCENE_SPLASH,
        SCENE_MENU,
        SCENE_GAME,
        SCENE_LOADING,
    }
    
    //---------------------------------------------
    // CLASS LOGIC
    //---------------------------------------------
    
    public void setScene(BaseScene scene)
    {
        engine.setScene(scene);
        currentScene = scene;
        currentSceneType = scene.getSceneType();
    }
    
    public void setScene(SceneType sceneType)
    {
        switch (sceneType)
        {
            case SCENE_MENU:
                setScene(menuScene);
                break;
            case SCENE_GAME:
                setScene(gameScene);
                break;
            case SCENE_SPLASH:
                setScene(splashScene);
                break;
            case SCENE_LOADING:
                setScene(loadingScene);
                break;
            default:
                break;
        }
    }
    
    //---------------------------------------------
    // GETTERS AND SETTERS
    //---------------------------------------------
    
    public SceneType getCurrentSceneType()
    {
        return currentSceneType;
    }
    
    public BaseScene getCurrentScene()
    {
        return currentScene;
    }
    
    public void createSplashScene(OnCreateSceneCallback pOnCreateSceneCallback)
    {
    	if(splashScene == null){
    		loadSplashScene();
    	}
    	splashScene.createScene();
    	setScene(splashScene);
    	pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
    }
    
    public void loadSplashScene(){
        splashScene = new SplashScene(engine, activity, vbom, camera, this);
        splashScene.loadScene();
    }
    
    public void disposeSplashScene()
    {
    	if(splashScene != null){
        splashScene.disposeScene();
        splashScene = null;
    	}
    }
    
    public void loadMenuScene()
    {
    	menuScene = new MenuScene(engine, activity, vbom, camera, this);
        menuScene.loadScene();
    }
    
    public void createMenuScene()
    {
    	if(menuScene == null){
    		loadMenuScene();
    	}
    	menuScene.createScene();
    }
    
    public void goToMenuScene(){
    	setScene(menuScene);
    }
    
    public void disposeMenuScene(){
    	
    }
    
    public void loadGameScene(){
    	gameScene = new GameScene(engine, activity, vbom, camera, this);
    	gameScene.loadScene();
    }
    
    public void createGameScene(){
    	if(gameScene == null){
    		loadGameScene();
    	}
    	gameScene.createScene();
    }
    
    public void goToGameScene(){
    	setScene(gameScene);
    }
    
    public void disposeGameScene(){
    	
    }
}
