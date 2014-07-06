package com.mihu.dhadi.frontend;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.graphics.Typeface;

import com.mihu.dhadi.backend.AttackMeGame;
import com.mihu.dhadi.backend.EventType;
import com.mihu.dhadi.backend.MoveOutcome;
import com.mihu.dhadi.frontend.SceneManager.SceneType;

public class GameScene extends BaseScene
{

	public ITextureRegion game_background_region;
	public ITextureRegion unloadedPawnRegion;
	public ITextureRegion player1Region;
	public ITextureRegion player2Region;
	public Game game;
	public ITextureRegion playerHeadingRegion;
	public Font font;
	public AttackMeGame backendGame;


	private BuildableBitmapTextureAtlas gameTextureAtlas;


	public GameScene(Engine engine, BaseGameActivity activity,
			VertexBufferObjectManager vbom, Camera camera, SceneManager sceneManager) {
		super(engine, activity, vbom, camera, sceneManager);
	}

	@Override
	public void createScene()
	{
		createBackground();
		game = new Game(GameMode.DROID, 9, 0);
	}

	@Override
	public void onBackKeyPressed()
	{

	}

	@Override
	public SceneType getSceneType()
	{
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene()
	{

	}

	@Override
	public void loadScene() {
		gameTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1024,1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		game_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "game_background4.png");
		unloadedPawnRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "unselected_pawn5.png");
		player1Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "player1_pawn3.png");
		player2Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "player2_pawn3.png");
		playerHeadingRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "single_heading.png");

		this.font = FontFactory.create(engine.getFontManager(), engine.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
		this.font.load();

		try {
			this.gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));

		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		this.gameTextureAtlas.load();
	}

	private void createBackground()
	{
		attachChild(new Sprite(0, 0, game_background_region, vbom)
		{
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera) 
			{
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		});
		setTouchAreaBindingOnActionDownEnabled(true);
		setTouchAreaBindingOnActionMoveEnabled(true);

	}
	enum GameMode{
		DROID, MULTIPLAYER;
	}

	class Game{
		public int pawnsPerPlayer;
		public boolean isMyTurn = false;
		public boolean attackOccurred = false;
		public Player whoAmI;
		public Player otherPlayer;
		public GameMode gameMode = GameMode.DROID;


		class UIPosition{
			int physicalX;
			int physicalY;
			int logicalNumber;
			public UIPosition(int physicalX, int physicalY, int logicalNumber) {
				super();
				this.physicalX = physicalX;
				this.physicalY = physicalY;
				this.logicalNumber = logicalNumber;
			}
			@Override
			public String toString(){
				return "x="+physicalX+", y="+physicalY;
			}
		}

		List<UIPosition> allUIPositions = new ArrayList<UIPosition>();
		List<UIPosition> allStandbyUIPositionsPlayer1 = new ArrayList<UIPosition>();
		List<UIPosition> allStandbyUIPositionsPlayer2 = new ArrayList<UIPosition>();


		class PawnPosition extends Sprite{
			UIPosition uiPosition;
			boolean isEmpty = true;
			Player player;

			public PawnPosition(UIPosition uiPosition, Player player, ITextureRegion region, VertexBufferObjectManager vbom) {
				super(uiPosition.physicalX, uiPosition.physicalY, region, vbom);
				this.uiPosition = uiPosition;
				this.player = player;
				if(player != null){
					isEmpty = false;
				}
			}

			@Override
			public boolean equals(Object other){
				if(other != null){
					if(other instanceof PawnPosition){
						PawnPosition otherPawn = (PawnPosition)other;
						if(otherPawn.uiPosition.logicalNumber == this.uiPosition.logicalNumber){
							return true;
						}
					}
				}
				return false;
			}
			public void reInitiatePosition(){
				this.setPosition(uiPosition.physicalX, uiPosition.physicalY);
			}
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(attackOccurred){
					if(pSceneTouchEvent.isActionUp()){
						PawnPosition pawnBeingRemoved;
						
						try {
							pawnBeingRemoved = getPawnPositionForUIPosition(getUIPosition(pSceneTouchEvent.getX(), pSceneTouchEvent.getY()));

							MoveOutcome outcome = backendGame.removePawn(pawnBeingRemoved.uiPosition.logicalNumber);
							if(outcome.getEventType() == EventType.SUCCESSFUL){

								nullifyPawn(pawnBeingRemoved);
								disablePlayerPawns(otherPlayer);
								attackOccurred = false;
								passTurnToOtherPlayer();

							}else if(outcome.getEventType() == EventType.GAME_OVER){
								//TODO: handle game over scenario
							}
						}
						catch(Exception e) {
							this.reInitiatePosition();
						}
					}
				} else{

					if(pSceneTouchEvent.isActionMove()){
						this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
					}else if(pSceneTouchEvent.isActionUp()){
						UIPosition uiPostionTemp = getUIPosition(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
						if(uiPostionTemp != null){
							MoveOutcome outcome = backendGame.movePawn(this.uiPosition.logicalNumber, uiPostionTemp.logicalNumber);
							Debug.i(outcome.getEventType().toString());
							
							if(outcome.getEventType() == EventType.SUCCESSFUL){
								PawnPosition pawnPosition = getPawnPositionForUIPosition(uiPostionTemp);
								replacePawn(pawnPosition, this);
								passTurnToOtherPlayer();
							} else if(outcome.getEventType() == EventType.ATTACK_DETECTED){
								PawnPosition pawnPosition = getPawnPositionForUIPosition(uiPostionTemp);
								replacePawn(pawnPosition, this);
								//passTurnToOtherPlayer();
								attackOccurred = true;
								disablePlayerPawns(whoAmI);
								enablePlayerPawns(otherPlayer);
							} else if(outcome.getEventType() == EventType.GAME_OVER){
								//TODO handle game over
							} else{
								this.reInitiatePosition();
							}
						} else{
							this.reInitiatePosition();
						}
					}
				}
				return true;
			}
		}

		List<PawnPosition> allPawnPositions = new ArrayList<PawnPosition>();

		class Player{
			int playerNumber;
			String name;
			ITextureRegion pawnRegion;
			public Player(int playerNumber, String name, ITextureRegion pawnRegion) {
				super();
				this.playerNumber = playerNumber;
				this.pawnRegion = pawnRegion;
				this.name = name;

			}
		}
		Player player1 = new Player(1, "player1", player1Region);
		Player player2 = new Player(2, "player2", player2Region);

		class SpriteButton extends Sprite {

			Text player1Text;
			Text player2Text;

			public SpriteButton(float x, float y, ITextureRegion pTextureRegion, VertexBufferObjectManager vbom){
				super(x, y, pTextureRegion, vbom);
				Text player1Text = new Text(x, y, font, player1.name, vbom);
				player1Text.setColor(Color.BLACK);
				player1Text.setTag(0);
				player1Text.setPosition(this.getX() + 40, this.getY());
				this.attachChild(player1Text);

				Text player2Text = new Text(x, y, font, player2.name, vbom);
				player2Text.setColor(Color.BLACK);
				player2Text.setTag(1);
				player2Text.setPosition(this.getWidth() -130, this.getY());
				this.attachChild(player2Text);
			}
		}
		SpriteButton playerHeading;

		public Game(GameMode gameMode, int pawnsPerPlayer, int playerNum){

			backendGame = AttackMeGame.startNewGame(pawnsPerPlayer, false);
			this.gameMode = gameMode;
			this.pawnsPerPlayer = pawnsPerPlayer;
			if(playerNum == 0){
				whoAmI = player1;
				isMyTurn = true;
				otherPlayer = player2;
			}else if(playerNum == 1){
				whoAmI = player2;
				isMyTurn = false;
				otherPlayer = player1;
			}
			whoAmI.name = "You";
			if(GameMode.DROID == gameMode){
				otherPlayer.name = "Droid";
			}else{
				otherPlayer.name = "Opponent";
			}

			allUIPositions = getAllUIPositionsForGame();
			allStandbyUIPositionsPlayer1 = getAllStandbyUIPositionsPlayer1();
			allStandbyUIPositionsPlayer2 = getAllStandbyUIPositionsPlayer2();

			allPawnPositions = buildAllPawnPositions();
			allPawnPositions.addAll(buildStandbyPawnPositions(allStandbyUIPositionsPlayer1, player1));
			allPawnPositions.addAll(buildStandbyPawnPositions(allStandbyUIPositionsPlayer2, player2));

			playerHeading = new SpriteButton(0, 10, playerHeadingRegion, vbom);
			attachChild(playerHeading);

			if(isMyTurn){
				enablePlayerPawns(whoAmI);
			}else{
				//Let droid start the game.
				letDroidMakeMove(this);
			}

		}

		private List<UIPosition> getAllUIPositionsForGame(){
			//Create all UI positions for the game
			List<UIPosition> uiPositions = new ArrayList<UIPosition>();
			int box1X = 220;
			int box1Y = 80;
			int box1Width = 160;
			uiPositions.addAll(getUIPositionForBox(box1X+box1Width*2/3, box1Y+box1Width*2/3, box1Width/3, 0));
			uiPositions.addAll(getUIPositionForBox(box1X+box1Width/3, box1Y+box1Width/3, box1Width*2/3, 8));
			uiPositions.addAll(getUIPositionForBox(box1X, box1Y, box1Width, 16));

			return uiPositions;

		}

		private List<UIPosition> getUIPositionForBox(int xstart, int ystart, int delta, int index){
			List<UIPosition> uiPositions = new ArrayList<UIPosition>();

			uiPositions.add(new UIPosition(xstart+2*delta, ystart+delta, index++));
			uiPositions.add(new UIPosition(xstart+2*delta, ystart+2*delta, index++));
			uiPositions.add(new UIPosition(xstart+delta, ystart+2*delta, index++));
			uiPositions.add(new UIPosition(xstart, ystart+2*delta, index++));
			uiPositions.add(new UIPosition(xstart, ystart+delta, index++));
			uiPositions.add(new UIPosition(xstart, ystart, index++));
			uiPositions.add(new UIPosition(xstart+delta, ystart, index++));
			uiPositions.add(new UIPosition(xstart+2*delta, ystart, index++));
			return uiPositions;

		}

		private List<UIPosition> getAllStandbyUIPositionsPlayer1(){
			List<UIPosition> uiPositions = new ArrayList<UIPosition>();
			for(int i=0; i< pawnsPerPlayer;i++){
				uiPositions.add(new UIPosition(50,50+i*40, 24+i));
			}
			return uiPositions;
		}

		private List<UIPosition> getAllStandbyUIPositionsPlayer2(){
			List<UIPosition> uiPositions = new ArrayList<UIPosition>();
			for(int i=0; i< pawnsPerPlayer;i++){
				uiPositions.add(new UIPosition(700,50+i*40, 36+i));
			}
			return uiPositions;
		}

		private List<PawnPosition> buildAllPawnPositions(){
			List<PawnPosition> pawnPositions = new ArrayList<PawnPosition>();
			for(UIPosition uiPosition : allUIPositions){
				PawnPosition pawnPosition = new PawnPosition(uiPosition, null, unloadedPawnRegion, vbom);
				attachChild(pawnPosition);
				pawnPositions.add(pawnPosition);
			}
			return pawnPositions;
		}

		private List<PawnPosition> buildStandbyPawnPositions(List<UIPosition> uiPositions, Player player){
			List<PawnPosition> pawnPositions = new ArrayList<PawnPosition>();

			for(UIPosition uiPosition : uiPositions){
				PawnPosition pawnPosition = new PawnPosition(uiPosition, player, player.pawnRegion, vbom);
				attachChild(pawnPosition);
				pawnPositions.add(pawnPosition);
			}
			return pawnPositions;
		}


		private UIPosition getUIPosition(float x, float y){
			for(UIPosition uiPosition : allUIPositions){
				if(x > (uiPosition.physicalX -10) && x < (uiPosition.physicalX + 50) && y > (uiPosition.physicalY - 10) && y < (uiPosition.physicalY + 50) ){
					return uiPosition;
				}
			}
			return null;
		}

		private PawnPosition getPawnPositionForUIPosition(UIPosition uiPosition){
			if(uiPosition == null){
				return null;
			}
			for(PawnPosition pawnPosition: allPawnPositions){
				if(pawnPosition.uiPosition.logicalNumber == uiPosition.logicalNumber){
					return pawnPosition;
				}
			}
			return null;
		}

		private PawnPosition getPawnById(int i){
			for(PawnPosition pawnPosition: allPawnPositions){
				if(pawnPosition.uiPosition.logicalNumber == i){
					return pawnPosition;
				}
			}
			return null;
		}

		private void passTurnToOtherPlayer(){
			Debug.i("Passing turn to other player");
			disablePlayerPawns(whoAmI);
			isMyTurn = false;
			if(gameMode == GameMode.DROID){
				letDroidMakeMove(this);
			}
		}

		private void disablePlayerPawns(Player player){
			for(PawnPosition pawnPosition: allPawnPositions){
				if(pawnPosition.player != null && pawnPosition.player.playerNumber == player.playerNumber){
					GameScene.this.unregisterTouchArea(pawnPosition);
				}
			}
			if(player1.playerNumber == player.playerNumber){
				Text t = (Text)playerHeading.getChildByTag(0);
				t.setColor(Color.TRANSPARENT);
			}else{
				Text t = (Text)playerHeading.getChildByTag(1);
				t.setColor(Color.TRANSPARENT);
			}
		}

		private void enablePlayerPawns(Player player){
			for(PawnPosition pawnPosition: allPawnPositions){
				if(pawnPosition.player != null && pawnPosition.player.playerNumber == player.playerNumber){
					GameScene.this.registerTouchArea(pawnPosition);
				}
			}
			if(player1.playerNumber == player.playerNumber){
				Text t = (Text)playerHeading.getChildByTag(0);
				t.setColor(Color.BLACK);
			}else{
				Text t = (Text)playerHeading.getChildByTag(1);
				t.setColor(Color.BLACK);
			}
		}

		private void nullifyPawn(PawnPosition pawnBeingNullified){
			PawnPosition newPawn = new PawnPosition(pawnBeingNullified.uiPosition, null, unloadedPawnRegion, vbom);
			attachChild(newPawn);
			detachChild(pawnBeingNullified);
			pawnBeingNullified.dispose();
			allPawnPositions.remove(pawnBeingNullified);
			allPawnPositions.add(newPawn);
		}

		private void replacePawn(PawnPosition pawnBeingOverridden, PawnPosition pawnBeingMoved){
			pawnBeingOverridden.isEmpty = true;
			pawnBeingOverridden.player = null;
			UIPosition uiPositionOfPawnBeingReplaced = pawnBeingOverridden.uiPosition;
			pawnBeingOverridden.uiPosition = pawnBeingMoved.uiPosition;
			pawnBeingOverridden.reInitiatePosition();
			pawnBeingMoved.uiPosition = uiPositionOfPawnBeingReplaced;
			unregisterTouchArea(pawnBeingOverridden);
			pawnBeingMoved.reInitiatePosition();
			// do not show the standy pawn
			if(pawnBeingOverridden.uiPosition.logicalNumber >= 24){
				detachChild(pawnBeingOverridden);
				pawnBeingOverridden.dispose();
			}
		}


		private void letDroidMakeMove(Game game){
			// For now implement droid playing here...
			// In multiplayer game, we should interact with cloud.
			GameScene.this.registerUpdateHandler(new TimerHandler(1f, new ITimerCallback() 
			{
				public void onTimePassed(final TimerHandler pTimerHandler) 
				{
					GameScene.this.unregisterUpdateHandler(pTimerHandler);
					//make move and pass the turn to player
					//randomly make a move

					PawnPosition sourcePawn = null;
					PawnPosition targetPawn = null;
					List<PawnPosition> tempSource = new ArrayList<PawnPosition>();
					for(PawnPosition pawn:allPawnPositions){
						if(pawn.player == otherPlayer && pawn.uiPosition.logicalNumber >=24){
							tempSource.add(pawn);
						}
					}
					
					if(tempSource.isEmpty()){
						for(PawnPosition pawn:allPawnPositions){
							if(pawn.player == otherPlayer){
								tempSource.add(pawn);
							}
						}
					}
					
					while(true) {
						if(tempSource.size() == 0) {
							// TODO: Droid stuck. GAME OVER
							return;
						}
						int randomChoice = (int)(Math.random()*100 % tempSource.size());
						sourcePawn = tempSource.get(randomChoice);
						int dest = backendGame.suggestMove(sourcePawn.uiPosition.logicalNumber);
						
						if(dest > 0) {
							targetPawn = getPawnById(dest);
							break;
						}
						else {
							tempSource.remove(randomChoice);
						}
					}
					
					//TODO invoke game api to make move. and if the result is attack, then write code to pick one of other players pawn
					Debug.i("Droid playing now");
					MoveOutcome moveOutcome = backendGame.movePawn(sourcePawn.uiPosition.logicalNumber, targetPawn.uiPosition.logicalNumber);
					if(moveOutcome.getEventType() == EventType.ATTACK_DETECTED){
						replacePawn(targetPawn, sourcePawn);
						List<PawnPosition> pawnsThatCanBePicked = new ArrayList<PawnPosition>();
						for(PawnPosition pawn: allPawnPositions){
							if(pawn.player == whoAmI && pawn.uiPosition.logicalNumber < 24){
								int temp = pawn.uiPosition.logicalNumber;
								if(backendGame.isPickable(otherPlayer.playerNumber, temp/8, temp%8)) {
									Debug.i("Adding to pickable: " + pawn.uiPosition.logicalNumber);
									pawnsThatCanBePicked.add(pawn);
								}
							}
						}
						for(int i=0; i< pawnsThatCanBePicked.size();i++){
							PawnPosition pawn = pawnsThatCanBePicked.get(i);
							MoveOutcome removeOutcome = backendGame.removePawn(pawn.uiPosition.logicalNumber);
							if(removeOutcome.getEventType() == EventType.SUCCESSFUL){
								nullifyPawn(pawn);
								enablePlayerPawns(whoAmI);
								isMyTurn = true;
								break;
							}else if(removeOutcome.getEventType() == EventType.GAME_OVER){
								//TODO handle game over
							}

						}
					} else if(moveOutcome.getEventType() == EventType.SUCCESSFUL){
						replacePawn(targetPawn, sourcePawn);
						enablePlayerPawns(whoAmI);
						isMyTurn = true;
					} else if(moveOutcome.getEventType() == EventType.GAME_OVER) {
						replacePawn(targetPawn, sourcePawn);
					}

				}
			}));
		}
	}

}