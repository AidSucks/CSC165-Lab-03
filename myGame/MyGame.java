package myGame;

import tage. * ;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.shapes. * ;
import java.util.ArrayList;
import java.util.UUID; 

import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.event. * ;
import java.io.IOException;

import org.joml. * ;

import myGame.client.GameClient;
import myGame.networking.EntityType;
import tage.input. * ;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private PhysicsEngine physicsEngine;
	private IAudioManager audioManager;

	private InputManager im;
	private GameClient gameClient;
	
	private boolean isxyzAxesVisible = true;
	private boolean paused=false;
	private boolean isGameStart=false;
	private boolean isLightOn=false;
	private boolean hasKey=false;
	private boolean spaceshipActivated = false;
	private boolean isBeepPlay = true;
	
	private int counter=0;

	private static int serverPort = 9999;
	private static InetAddress serverAddress;

	private static double lastFrameTime, currFrameTime;

	private double elapsTime;
	
	private float beepTimer = 0.0f;
	private float beepInterval = 2.0f;
	
	private int fluffyClouds, lakeIslands, mars, mars1; // skyboxes 

	private PhysicsObject terrainMesh, caps1P;

	private Sound footstepSound, beepSound;
	
    private String message = "Escape this planet, Find the KEY, Find the spaceship, R/B3 - flashlight, F/B2 - Push, Spacebar/B1 - jump, E/B0 - Take";
    private String infor = "";
	

	// object
	private Player avatar;
	private ArrayList < GameObject > keys = new ArrayList < GameObject > ();

	private GameObject enemy, x, y, z, terr, spaceship, key;
	// shape
	private ObjShape dolS, linxS, linyS, linzS, terrS, spaceshipS, keyS;
	// texture
	private TextureImage doltx, enemyTex, hills, floor, spaceshipTex, keyTex;
	// light
	private Light light1, light2, light3, light4;
	
	// Camera
	private CameraOrbit3D orbitCamera;
	private Camera leftCamera, rightCamera;
	
	// viewport
	private Viewport leftVp;
    private Viewport rightVp;
	private Vector3f overheadView = new Vector3f(0, 18, 0);
	
	// enemies
	private AnimatedShape enemyS;
	
	private int maxEnemies = 100;
	private float spawnTimer = 0.0f;
	private float spawnWait = 2.0f; // spawn every 2 seconds
	private boolean isEnemyHost = true; // testing enemies server game A true, game B false
	private class LocalEnemy {
		UUID id;
		GameObject obj;

		LocalEnemy(UUID id, GameObject obj) {
			this.id = id;
			this.obj = obj;
		}
	}
	private ArrayList<LocalEnemy> enemies = new ArrayList<>();


	public MyGame() { 
		super();
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame();

		if(args.length > 0) {

			try {
				serverAddress = InetAddress.getByName(args[0]);

				serverPort = Integer.parseInt(args[1]);
			} catch(NumberFormatException ex) {
				System.err.println("Invalid server port format. Starting singleplayer");
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}

		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();

	}

	private void setupNetworking()
	{
		try {

			this.gameClient = new GameClient(InetAddress.getByName("localhost"), serverPort, this);

		}catch(UnknownHostException ex) {
			System.err.println(ex.getMessage());
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		if(this.gameClient == null) return;

		this.gameClient.joinServer();
	}

	@Override
	public void loadSounds() {

		AudioResource audioResource, audioResourceBeep;

		this.audioManager = engine.getAudioManager();


		// https://opengameart.org/content/foot-walking-step-sounds-on-stone-water-snow-wood-and-dirt
		audioResource = audioManager.createAudioResource("stepdirt_1.wav", AudioResourceType.AUDIO_SAMPLE);
		// https://opengameart.org/content/3-ping-pong-sounds-8-bit-style
		audioResourceBeep = audioManager.createAudioResource("beep.wav", AudioResourceType.AUDIO_SAMPLE);

		footstepSound = new Sound(audioResource, SoundType.SOUND_EFFECT, 100, false);
		footstepSound.initialize(audioManager);
		footstepSound.setMaxDistance(10.0f);
		footstepSound.setMinDistance(0.5f);
		footstepSound.setRollOff(5.0f);
		
		beepSound = new Sound(audioResourceBeep, SoundType.SOUND_EFFECT, 100, false);
		beepSound.initialize(audioManager);
		beepSound.setMaxDistance(30.0f);
		beepSound.setMinDistance(5f);
		beepSound.setRollOff(5.0f);
	}

	@Override
	public void loadShapes(){
		dolS = new ImportedModel("dolphinHighPoly.obj");
		spaceshipS = new ImportedModel("spaceship.obj");
		keyS = new ImportedModel("key.obj");
		// enemyS = new ImportedModel("enemy.obj");
		enemyS = new AnimatedShape("enemy.rkm", "enemy.rks"); 
		enemyS.loadAnimation("IDLE", "idle.rka"); 
		enemyS.loadAnimation("WALK", "walk.rka"); 
		enemyS.loadAnimation("ATTACK", "attack.rka"); 
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
        linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
        linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));
		terrS = new TerrainPlane(1000);  // pixels per axis = 1000x1000
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("Dolphin_HighPolyUV.jpg");
		enemyTex = new TextureImage("enemyUV.jpg");
		hills = new TextureImage("hills.jpg"); 
		floor = new TextureImage("floor.jpg"); 
		spaceshipTex = new TextureImage("spaceshipTex.jpg"); 
		keyTex = new TextureImage("keyTex.jpg"); 
	}
	
	@Override 
	public void loadSkyBoxes() 
	{
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds"); 
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands"); 
		mars = (engine.getSceneGraph()).loadCubeMap("mars"); 
		mars1 = (engine.getSceneGraph()).loadCubeMap("mars1"); 
		(engine.getSceneGraph()).setActiveSkyBoxTexture(mars); 
		(engine.getSceneGraph()).setSkyBoxEnabled(true); 
	} 

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// X,Y,-Z axes
        x = new GameObject(GameObject.root(), linxS);
        y = new GameObject(GameObject.root(), linyS);
        z = new GameObject(GameObject.root(), linzS);
        x.getRenderStates().isTransparent(true);
        y.getRenderStates().isTransparent(true);
        z.getRenderStates().isTransparent(true);
        (x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
        (y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
        (z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		// build avatar
		avatar = new Player(dolS, doltx);
		
		// build enemy
		enemy = new GameObject(GameObject.root(), enemyS, enemyTex);
		initialTranslation = (new Matrix4f()).translation(0f, 5f, 0.5f);
		enemy.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.05f);
        enemy.setLocalScale(initialScale);
		// initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f));
        // enemy.setLocalRotation(initialRotation);
		enemy.getRenderStates().setModelOrientationCorrection( (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180.0f)));
		enemyS.playAnimation("IDLE", 0.5f, AnimatedShape.EndType.LOOP, 0); 
		
		// build spaceship
		spaceship = new GameObject(GameObject.root(), spaceshipS, spaceshipTex);
		initialTranslation = (new Matrix4f()).translation(0f, 0f, 10f);
		spaceship.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.009f);
        spaceship.setLocalScale(initialScale);
		
		// build key
		key = new GameObject(GameObject.root(), keyS, keyTex);
		initialTranslation = (new Matrix4f()).translation(4f, 0f, 10f);
		key.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.3f);
        key.setLocalScale(initialScale);
		initialRotation = (new Matrix4f()).rotationZ((float)java.lang.Math.toRadians(20.0f));
        key.setLocalRotation(initialRotation);
		
		// build terrain object 
		terr = new GameObject(GameObject.root(), terrS, floor); 

		initialScale = (new Matrix4f()).scaling(200.0f, 8.0f, 200.0f); 
		terr.setLocalScale(initialScale); 
		
		terr.setHeightMap(hills); 
		// set tiling for terrain texture 
		terr.getRenderStates().setTiling(1); 
		terr.getRenderStates().setTileFactor(10); 
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.05f, 0.05f, 0.05f);
	
		light1 = new Light();
		light1.setType(Light.LightType.SPOTLIGHT);
		light1.setAmbient(0.2f, 0.2f, 0.2f);
		light1.setDiffuse(1.0f, 1.0f, 0.8f);
		light1.setSpecular(0.7f, 0.7f, 0.7f);	

		light1.setCutoffAngle(20.0f);
		light1.setOffAxisExponent(10.0f);

		light1.setLocation(new Vector3f(0.0f, 5.0f, 0.0f));
		light1.setDirection(new Vector3f(0.0f, -1.0f, 0.0f));
		(engine.getSceneGraph()).addLight(light1);
		
		light2 = new Light();
		light2.setType(Light.LightType.SPOTLIGHT);

		light2.setAmbient(0.0f, 0.0f, 0.0f);
		light2.setDiffuse(1.0f, 1.0f, 0.8f);
		light2.setSpecular(0.7f, 0.7f, 0.7f);	

		light2.setCutoffAngle(10.0f);
		light2.setOffAxisExponent(10.0f);

		light2.setLocation(new Vector3f(0.0f, 0.0f, 0.0f));
		light2.setDirection(new Vector3f(0.0f, 0.0f, -1.0f));
		light2.disable();
		(engine.getSceneGraph()).addLight(light2);
		
		light3 = new Light();
		light3.setAmbient(0.2f, 0.2f, 0.2f);
		light3.setDiffuse(0.0f, 1.0f, 0.0f);
		light3.setSpecular(0.0f, 1.0f, 0.0f);	
		
		light3.setConstantAttenuation(1.0f);
		light3.setLinearAttenuation(1.0f);
		light3.setQuadraticAttenuation(1.0f);
		
		light3.setLocation(new Vector3f(0.0f, 0.0f, 0.0f));
		(engine.getSceneGraph()).addLight(light3);
		
		light4 = new Light();
		light4.setAmbient(0.0f, 0.0f, 0.0f);
		light4.setDiffuse(1.0f, 0.0f, 0.0f);
		light4.setSpecular(1.0f, 0.0f, 0.0f);	
		
		light4.setConstantAttenuation(1.0f);
		light4.setLinearAttenuation(0.4f);
		light4.setQuadraticAttenuation(0.2f);
		
		light4.setLocation(new Vector3f(0.0f, 0.0f, 0.0f));
		(engine.getSceneGraph()).addLight(light4);
	}
	
	@ Override
    public void createViewports() {

        // ------------- viewport -------------
        (engine.getRenderSystem()).addViewport("LEFT", 0, 0, 1f, 1f);
        (engine.getRenderSystem()).addViewport("RIGHT", .75f, 0, .25f, .25f);

        leftVp = (engine.getRenderSystem()).getViewport("LEFT");
        rightVp = (engine.getRenderSystem()).getViewport("RIGHT");

        leftCamera = leftVp.getCamera();
        rightCamera = rightVp.getCamera();

        rightVp.setHasBorder(true);
        rightVp.setBorderWidth(4);
        rightVp.setBorderColor(0.0f, 1.0f, 0.0f);

        // ------------- positioning the camera -------------
        leftCamera.setU(new Vector3f(1, 0, 0));
        leftCamera.setV(new Vector3f(0, 1, 0));
        leftCamera.setN(new Vector3f(0, 0, -1));

        rightCamera.setU(new Vector3f(1, 0, 0));
        rightCamera.setV(new Vector3f(0, 0, -1));
        rightCamera.setN(new Vector3f(0, -1, 0));
		rightCamera.setLocation(new Vector3f(overheadView.x, overheadView.y, overheadView.z));
    }

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		// Process packets

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
		
		im = engine.getInputManager();
		orbitCamera = new CameraOrbit3D(leftCamera, avatar);

		// Sound
		footstepSound.setLocation(this.avatar.getWorldForwardVector());
		beepSound.setLocation(spaceship.getWorldLocation());
		// beepSound.play();
		
		// ----------------- INPUTS SECTION -----------------------------
		FwdAction fwdAction = new FwdAction(this, footstepSound);
        TurnAction turnAction = new TurnAction(this);
		OrbitAzimuthAction azmAction = new OrbitAzimuthAction(this, orbitCamera);
        OrbitElevationAction altAction = new OrbitElevationAction(this, orbitCamera);
        OrbitRadiusAction zoomAction = new OrbitRadiusAction(this, orbitCamera);
		JumpAction jumpAction = new JumpAction(this);
		PushAction pushAction = new PushAction(this);
		AxisTurnAction axisTurnAction = new AxisTurnAction(this);
		TakeKeyAction takeKeyAction = new TakeKeyAction(this);
		LightAction lightAction = new LightAction(this);
		
		// ----------------- Forward/Backward SECTION -----------------------------
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, jumpAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, pushAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// ----------------- Turn SECTION -----------------------------
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, axisTurnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		
		// ----------------- CameraOrbit3D SECTION -----------------------------
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RY, altAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Z, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, altAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.B, altAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.C, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.V, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Z, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.X, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// ----------------- SECTION -----------------------------
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, takeKeyAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.R, lightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._0, takeKeyAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._1, jumpAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._2, pushAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._3, lightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		this.setupNetworking();
	}
	
	@Override
	public void initializePhysicsObjects()
	{
		float[] gravity = {0f, -5f, 0f};
		
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		
		// --------------------enemy--------------------
		float mass = 50.0f; 
		float radius = 0.20f; 
		float height = 0.2f; 
		Vector3f loc; 
		Quaternionf rot2; 
		
		loc = enemy.getWorldLocation();  
		rot2 = new Quaternionf(); 
		(enemy.getWorldRotation()).getNormalizedRotation(rot2); 
		caps1P = (engine.getSceneGraph()).addPhysicsCapsule(mass, loc, rot2, 1, radius, height); 
		caps1P.getRigidBody().setAngularFactor(new com.jme3.math.Vector3f(0, 0, 0));
		caps1P.setDamping(0.5f,0.5f);
		// caps1P.disableSleeping(); 
		enemy.setPhysicsObject(caps1P);

		// Initialize Physics objects for player
		avatar.initializePhysics();

		terrainMesh = engine.getSceneGraph().addPhysicsStaticTerrainMesh(
			new Vector3f(0, 0, 0),
			new Quaternionf(), 
			hills, 
			200, 
			8f, 
			100
		);

		terrainMesh.setFriction(1f);
		terrainMesh.disableSleeping();
		terr.setPhysicsObject(terrainMesh);
		
		engine.enableGraphicsWorldRender(); 
		// engine.enablePhysicsWorldRender();
	}
	
	
	private void spawnEnemy() {
		
		GameObject e = new GameObject(GameObject.root(), enemyS, enemyTex);

		// get player position
		Vector3f playerPos = avatar.getWorldLocation();

		// random angle (circle around player)
		float angle = (float)(Math.random() * Math.PI * 2);

		// random distance from player
		float distance = 5.0f + (float)(Math.random() * 10.0f);

		float x = playerPos.x() + (float)Math.cos(angle) * distance;
		float z = playerPos.z() + (float)Math.sin(angle) * distance;
		float y = terr.getHeight(x, z);

		e.setLocalTranslation(new Matrix4f().translation(x, y, z));
		e.setLocalScale(new Matrix4f().scaling(0.05f));
	
	    UUID enemyID = UUID.randomUUID();

		enemies.add(new LocalEnemy(enemyID, e));

	
	}

	@Override
	public void update()
	{	
		if(this.gameClient != null) {
			this.gameClient.processPackets();

			Quaternionf rot = new Quaternionf();
			this.avatar.getWorldRotation().getNormalizedRotation(rot);

			this.gameClient.sendMove(null, this.avatar.getPhysicsObject().getLocation(), rot, EntityType.PLAYER, "WALK");

			if(this.gameClient.getIsHost()) {
				
			}
		}
		
		// game time
		// Update delta time
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		float dt = (float) getDeltaTime();
		elapsTime += dt;

		// build and set HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		
		// String disStr0 = "score: %s Message: %s";
        // String dispStr1 = String.format(disStr0, score, infor);
		
		String dispStr1 = "Message: " + message;
		String dispStr2 = "Information: " + infor;
		
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 15, 55);
		
		Vector3f enemyLoc = enemy.getWorldLocation(); 
		float enemyHeight = terr.getHeight(enemyLoc.x(), enemyLoc.z()); 
		enemy.setLocalLocation(new Vector3f(enemyLoc.x(), enemyHeight, enemyLoc.z())); 
		
		if (!spaceshipActivated){
			Vector3f spaceshipLoc = spaceship.getWorldLocation(); 
			float spaceshipHeight = terr.getHeight(spaceshipLoc.x(), spaceshipLoc.z()); 
			spaceship.setLocalLocation(new Vector3f(spaceshipLoc.x(), spaceshipHeight, spaceshipLoc.z())); 
		}
		Vector3f keyLoc = key.getWorldLocation(); 
		float keyHeight = terr.getHeight(keyLoc.x(), keyLoc.z()); 
		key.setLocalLocation(new Vector3f(keyLoc.x(), keyHeight, keyLoc.z())); 
		
		Matrix4f currentRot = key.getLocalRotation();
		key.setLocalRotation(new Matrix4f(currentRot).rotateY(dt));
		
		// input update
		im.update(dt);
		
		// enemy animation update
		enemyS.updateAnimation();
		gameClient.getEnemyManager().updateAnimations();
		
		// camera update
		orbitCamera.updateCameraPosition();
		updateOverHeadView();

		for(GameObject go : engine.getSceneGraph().getGameObjects()) {

			// Don't process terrain
			if(go.isTerrain()) continue;

			PhysicsObject po = go.getPhysicsObject();

			if(po == null ) continue;
			if (go == avatar && spaceshipActivated) continue;
			
			go.setLocalLocation(new Vector3f(po.getLocation()).lerp(go.getLocalLocation(), 0.25f));

			// Check is on ground
			if(go instanceof Player) {

				float yVel = po.getLinearVelocity()[1];

				if(yVel < 0.5f && yVel > -0.5f && po.getFullCollidedSet().contains(terrainMesh))
					((Player)go).setIsOnGround(true);
			}
		}

		physicsEngine.update(dt);
		physicsEngine.detectCollisions();
		
		// light
		updateLight();
		
		updateSpaceship();

		// Sounds
		footstepSound.setLocation(this.avatar.getWorldLocation());
		audioManager.getEar().setLocation(avatar.getWorldLocation());
		audioManager.getEar().setOrientation(this.leftCamera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
		updateBeepSound(dt);

		
		// // --------------- enemy walk to player 
		// if (isEnemyHost) {
			// for (LocalEnemy le : enemies) {
					// GameObject e = le.obj;
					// Vector3f enemiesPos = e.getWorldLocation();
					// Vector3f playerPos = avatar.getWorldLocation();

					// // distance^2 = (x2 - x1)^2 + (y2 - y1)^2
					// float dxPlayer = playerPos.x() - enemiesPos.x();
					// float dzPlayer = playerPos.z() - enemiesPos.z();
					// // distance^2 b/w player and enemy
					// float distToPlayerSq = dxPlayer * dxPlayer + dzPlayer * dzPlayer;
					
					// float enemyAttackRange = 0.3f;

					// // if player and enemy distance greater then enemy attack distance,
					// // enemy walk to player
					// if (distToPlayerSq > enemyAttackRange * enemyAttackRange) {
						// // direction to the player
						// Vector3f dir = new Vector3f(dxPlayer, 0, dzPlayer);
						// // dir positive
						// dir.normalize();

						// // --------------- aviod enemy overlap ---------------
						// Vector3f push = new Vector3f(0, 0, 0);
						// float minDist = 0.3f;

						// for (LocalEnemy otherEnemy : enemies) {
							
							// Vector3f otherEnemiesPos = otherEnemy.obj.getWorldLocation();

							// float dxEnemies = enemiesPos.x() - otherEnemiesPos.x();
							// float dzEnemies = enemiesPos.z() - otherEnemiesPos.z();
							// float distToEnemiesSq = dxEnemies * dxEnemies + dzEnemies * dzEnemies;
							// // System.out.println("distToEnemiesSq:" + distToEnemiesSq);

							// // if enemies distance less then min enemies distance (enemies too close together),
							// // push enemies back
							// if (distToEnemiesSq < minDist * minDist && distToEnemiesSq > 0.0001f) {
								// float dist = (float)Math.sqrt(distToEnemiesSq);
								// push.x += dxEnemies / dist;
								// push.z += dzEnemies / dist;
							// }
						// }
						

						// // System.out.println("push.lengthSquared():" + push.lengthSquared());
						// if (push.lengthSquared() > 0.0001f) {
							
							// push.normalize();
						// }
						// float speed = 2.0f;
						// float newX = enemiesPos.x() + (dir.x() + push.x) * speed * dt;
						// float newZ = enemiesPos.z() + (dir.z() + push.z) * speed * dt;
						// float newY = terr.getHeight(newX, newZ);
						// Vector3f newPos = new Vector3f(newX, newY, newZ);
						
						// e.setLocalLocation(newPos);
						
						// if (gameClient != null) {
							// gameClient.sendEnemyMove(le.id, newPos);
						// }
					// }
			// }
		// }
	}

	@Override
	public void shutdown()
	{
		super.shutdown();

		this.gameClient.leaveServer();
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_0:
				System.out.println("pressed 0");
				isGameStart = true;
				break;
			case KeyEvent.VK_T:
				if (gameClient != null) {
					Vector3f avatarPos = avatar.getWorldLocation();
					gameClient.sendSpawnNPCRequest(avatarPos);
				}
				break;
			case KeyEvent.VK_1:
				paused = !paused;
				break;
			case KeyEvent.VK_2:
				avatar.getRenderStates().setWireframe(true);
				break;
			case KeyEvent.VK_3:
				avatar.getRenderStates().setWireframe(false);
				break;
			case KeyEvent.VK_4:
			    (engine.getSceneGraph()).setActiveSkyBoxTexture(mars); 
				(engine.getSceneGraph()).setSkyBoxEnabled(true); 
				break; 
			case KeyEvent.VK_5:
			    (engine.getSceneGraph()).setActiveSkyBoxTexture(mars1); 
				(engine.getSceneGraph()).setSkyBoxEnabled(true); 
				break; 
			case KeyEvent.VK_9: 
			    (engine.getSceneGraph()).setSkyBoxEnabled(false); 
				break; 
			case KeyEvent.VK_O: 
			    terr.getRenderStates().enableRendering();
				break; 
			case KeyEvent.VK_P: 
			    terr.getRenderStates().disableRendering();
				break; 
			case KeyEvent.VK_6: 
				enemyS.stopAnimation(); 
				enemyS.playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0); 
				break;
			case KeyEvent.VK_7: 
				enemyS.stopAnimation(); 
				enemyS.playAnimation("ATTACK", 0.5f, AnimatedShape.EndType.LOOP, 0); 
				break;				
			case KeyEvent.VK_8:
				enemyS.stopAnimation(); 
				break; 
			// show axes
			case KeyEvent.VK_M:
				System.out.println("pressed M");
				isxyzAxesVisible = !isxyzAxesVisible;
				xyzAxesVisible();
				System.out.println("isxyzAxesVisible is " + isxyzAxesVisible);
				break;
		}
		super.keyPressed(e);
	}
	
	private void xyzAxesVisible() {
        if (isxyzAxesVisible) {
            x.getRenderStates().enableRendering();
            y.getRenderStates().enableRendering();
            z.getRenderStates().enableRendering();

        } else {
            x.getRenderStates().disableRendering();
            y.getRenderStates().disableRendering();
            z.getRenderStates().disableRendering();
        }
    }
	
	private void lightVisible() {
        if (isLightOn) {
            light2.enable();
        } else {
            light2.disable();
        }
    }
	
	public void lightToggle(){
		isLightOn = !isLightOn;
		lightVisible();
	}
	
	private void updateLight(){
		if (light2 == null || avatar == null) return;

		// Player position
		Vector3f playerPos = avatar.getWorldLocation();

		// Player forward direction
		Vector3f forward = new Vector3f(avatar.getWorldForwardVector());
		forward.normalize();

		// Flashlight position 
		Vector3f lightPos = new Vector3f(playerPos);
		lightPos.y += 0.3f; // hand height

		float lightDistance = 5.0f; 
		Vector3f targetPos = new Vector3f(playerPos);
		targetPos.add(new Vector3f(forward).mul(lightDistance));

		// Direction from flashlight position 
		Vector3f lightDir = new Vector3f(targetPos).sub(lightPos);
		lightDir.normalize();

		light2.setLocation(lightPos);
		light2.setDirection(lightDir);
		light3.setLocation(playerPos);
		
		Vector3f spaceshipPos = spaceship.getWorldLocation();
		float[] light1Pos = light1.getLocation();
		light1.setLocation(new Vector3f(spaceshipPos.x(),light1Pos[1],spaceshipPos.z()));
		
		Vector3f keyPos = key.getWorldLocation();
		keyPos.y += 0.3f;
		light4.setLocation(new Vector3f(keyPos.x(),keyPos.y(),keyPos.z()));
	}
	
	private void updateOverHeadView(){
		if (avatar == null) return;

		// Player position
		Vector3f playerPos = avatar.getWorldLocation();
		
		overheadView.x = playerPos.x;
		overheadView.z = playerPos.z;
		
		rightCamera.setLocation(overheadView);
	}
	
	private void updateBeepSound(float dt){
		if (beepSound == null || spaceship == null) return;

		beepTimer += dt;
		
		if (beepTimer >= beepInterval){
			beepSound.setLocation(spaceship.getWorldLocation());
			audioManager.getEar().setLocation(avatar.getWorldLocation());
			audioManager.getEar().setOrientation(this.leftCamera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
			if(isBeepPlay) {
				beepSound.play();
			}else{
				beepSound.stop();
			}
			

			beepTimer = 0.0f;
		}
	}
	
	public boolean checkDistance(GameObject objA, GameObject objB, float amount) {
        Vector3f locA, locB;

        locA = objA.getWorldLocation();
        locB = objB.getWorldLocation();

        if (locA.distance(locB) <= amount) {
            // System.out.println("close enough");
            return true;
        } else {
            // System.out.println("not close enough");
            return false;
        }
    }
	
	public void tryTakeKey(){
		if (checkDistance(avatar, key, 2.0f)) {
			infor = "key taken";
			takeKey(keyTex);
			hasKey = true;
			key.getRenderStates().disableRendering();
			light4.disable();
		} else {
			infor = "not close enough getting key";
		}
	}
	
	private void takeKey(TextureImage ojbTex) {
        Matrix4f initialTranslation, initialScale, initialRotation;

        // Set parent = avatar and Call avatar.addChild(key)
        GameObject key = new GameObject(avatar, keyS, ojbTex);
        key.applyParentRotationToPosition(true);

        // set sacle, rota, transf
        initialScale = (new Matrix4f()).scaling(0.08f);
        key.setLocalScale(initialScale);

        initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
        key.setLocalRotation(initialRotation);

        initialTranslation = (new Matrix4f()).translation(0.2f, 0.2f, 0.0f);
        key.setLocalTranslation(initialTranslation);

        // add photo to arraylist
        keys.add(key);
    }
	
	private void updateSpaceship(){
		Matrix4f initialTranslation,
        initialScale,
        initialRotation;
		
		boolean  closeSpaceship = checkDistance(avatar, spaceship, 3.0f);
		// System.out.println("hasKey: " + hasKey);
		// System.out.println("closeSpaceship: " + closeSpaceship);

		if (!hasKey && closeSpaceship){
			infor = "You do not have a key to activate spaceship!";
			return;
		}
		
		if (hasKey && closeSpaceship){
			infor = "Spaceship activated!";
			
			for (int i = 0; i < keys.size(); i++) {

				GameObject k = keys.get(i);

				k.setParent(spaceship);
				k.applyParentRotationToPosition(false);

				initialTranslation = (new Matrix4f()).translation(0.0f, 0.0f, 0.0f);
				k.setLocalTranslation(initialTranslation);

			}
			keys.clear();
			
			avatar.setParent(spaceship);
			avatar.propagateTranslation(true);
			avatar.propagateRotation(true);
			avatar.propagateScale(false);

			avatar.applyParentRotationToPosition(true);
			avatar.applyParentScaleToPosition(false);
			
			avatar.setLocalLocation(new Vector3f(0.0f, 1.0f, 0.0f));
			avatar.setLocalRotation(new Matrix4f());
			avatar.getPhysicsObject().setLinearVelocity(new float[]{0f, 0f, 0f});
			spaceshipActivated = true;
			isBeepPlay = false;
		}	
		
		if(spaceshipActivated){
			float maxY = 10.0f;
			float dt = (float)getDeltaTime();
			float speed = 2.0f;
			
			Vector3f pos = spaceship.getWorldLocation();
			if (pos.y() < maxY){
				spaceship.setLocalLocation(new Vector3f(
				pos.x(),
				pos.y() + speed * dt,
				pos.z()
			));
			}else{
				infor = "You Escaped";
			}
			
		}
	}
	
	
	public Player getAvatar() {
        return avatar;
    }
	
	public GameObject getEnemy() {
        return enemy;
    }


	public static Engine getEngine() {
		return engine;
	}

	public static double getDeltaTime() {
		return (currFrameTime - lastFrameTime) / 1000;
	}

	public GameClient getGameClient() {
		return this.gameClient;
	}
	
	public ObjShape getEnemyShape() {
		return enemyS;
	}
	
	public AnimatedShape getEnemyAnimatedShape() {
		return enemyS;
	}

	public TextureImage getEnemyTexture() {
		return enemyTex;
	}
	
	public GameObject getTerrain() {
		return terr;
	}

	public float getTerrainHeightAt(float x, float z) {
		return terr.getHeight(x, z);
	}
	
	
}