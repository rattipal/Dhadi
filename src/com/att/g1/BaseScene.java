package com.att.g1;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import com.att.g1.SceneManager.SceneType;

public abstract class BaseScene extends Scene
{
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------
    
    protected Engine engine;
    protected BaseGameActivity activity;
    protected VertexBufferObjectManager vbom;
    protected Camera camera;
    protected SceneManager sceneManager;
    
    //---------------------------------------------
    // CONSTRUCTOR
    //---------------------------------------------
    
    public BaseScene(Engine engine, BaseGameActivity activity, VertexBufferObjectManager vbom, Camera camera, SceneManager sceneManager)
    {
    	super();
        this.engine = engine;
        this.activity = activity;
        this.vbom = vbom;
        this.camera = camera;
        this.sceneManager = sceneManager;
        
    }
    
    //---------------------------------------------
    // ABSTRACTION
    //---------------------------------------------
    
    public abstract void loadScene();
    
    public abstract void createScene();
    
    public abstract void onBackKeyPressed();
    
    public abstract SceneType getSceneType();
    
    public abstract void disposeScene();
}
