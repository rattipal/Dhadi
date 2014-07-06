package com.mihu.dhadi.frontend;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import com.mihu.dhadi.frontend.SceneManager.SceneType;

public class SplashScene extends BaseScene
{
    public SplashScene(Engine engine, BaseGameActivity activity,
			VertexBufferObjectManager vbom, Camera camera, SceneManager sceneManager) {
		super(engine, activity, vbom, camera, sceneManager);
	}
    
    // Variables
    private Sprite splash;
	private BitmapTextureAtlas splashTextureAtlas;
	private TextureRegion splashTextureRegion;

	@Override
    public void createScene()
    {
		
		this.setBackground(new Background(0,0,0));
	    splash = new Sprite(0, 0, splashTextureRegion, activity.getVertexBufferObjectManager())
		{
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera) 
			{
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		};
		splash.setScale(1.5f);
		splash.setPosition((camera.getWidth() - splash.getWidth()) * 0.5f, (camera.getHeight() - splash.getHeight()) * 0.5f);
		this.attachChild(splash);

    }

    @Override
    public void onBackKeyPressed()
    {
    	System.exit(0);
    }

    @Override
    public SceneType getSceneType()
    {
    	return SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene()
    {
    	if(splash != null){
    	    splash.detachSelf();
    	    splash.dispose();
    	}
    	    this.detachSelf();
    	    this.dispose();
    }

	@Override
	public void loadScene() {
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		splashTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), Math.round(camera.getWidth()),Math.round(camera.getHeight()), TextureOptions.DEFAULT);
		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, activity, "splash.png", 0, 0);
		splashTextureAtlas.load();
		
	}
}