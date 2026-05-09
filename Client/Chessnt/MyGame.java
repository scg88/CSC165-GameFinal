package Chessnt;

import tage.*;
import tage.input.action.*;
import tage.nodeControllers.BobbingController;
import tage.nodeControllers.RotationController;
import tage.shapes.*;
import tage.input.*;

// Controller-specific library (JInput)
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.lang.Math;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;

import java.util.*;
import java.util.UUID;
import java.net.InetAddress;
import tage.networking.IGameConnection.ProtocolType;
import java.net.UnknownHostException;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import java.util.HashSet;

import tage.audio.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private GhostManager gm;
	ChessPiece[] playerPieces = new ChessPiece[16];
	ChessPiece[] opponentPieces = new ChessPiece[16];
	int id = 0;

	private boolean paused=false;
	private boolean isRiding = true; // Start the game on the dolphin
	private String hudMessage = "Welcome to the CSC165 Final Project! Press the 'Y Button' to jump!";
	private boolean axesVisible = true;
	
	private boolean isGameOver = false; // Game over condition
	private boolean isGameWon = false;  // Game win condition (if you want to implement a win state)

	private double lastFrameTime, currFrameTime, elapsTime;
	private float vertVel = 0.0f; // Vertical velocity for jumping and gravity

	private InputManager im;
	private GameObject x, y, z, home, selector;
	private ObjShape linxS, linyS, linzS, homeS, ghostS, selectorS;
	private TextureImage bricktx, ghostT, selectorT;
	private Light light1, light2, light3, light4;
	
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// **** Chess like Game Pieces
	// **** MILESTONE 1
	private TextureImage rooktxRed, rooktxBlue, kingtxRed, kingtxBlue, queentxRed, 
		queentxBlue, knighttxRed, knighttxBlue, pawntxRed, pawntxBlue, bishoptxRed, bishoptxBlue;
	private ObjShape rookS, queenS, knightS, pawnS, bishopS;

	// **** Removed for MILESTONE 2 - now using AnimatedShape for the king piece
	// private ObjShape kingS; 

	// **** Skybox
	// **** MILESTONE 1
	private int gradientSky; // Blue gradient skybox made on GIMP

	// **** Terrain map
	// **** MILESTONE 1
	private GameObject terr;
	private ObjShape terrS;
	private TextureImage hills, grass;

	// **** MILESTONE 2 - NPC
	private GameObject npc;
	private TextureImage npcTx;
	private ObjShape npcS;

	public ObjShape getNPCshape() { return npcS; }
	public TextureImage getNPCtexture() { return npcTx; }
	public GameObject getNPC() { return npc; }

	// **** MILESTONE 2 - Networked Ghost NPCs
	public void createGhostNPC(UUID ghostID, Vector3f position) {
    	if (gm != null) {
        	gm.createGhostNPC(ghostID, position);
    	}
	}

	// **** MILESTONE 2 Animation
	private AnimatedShape kingSRed, kingSBlue;

	private CameraOrbit3D orbitController;
	private RotationController pyramidRotController;
	private BobbingController pyramidBobController;
	private BobbingController homeBobController;
	
	private ChessPiece redRook, redPawn, redQueen, redKing, redBishop, redKnight;
	private ChessPiece blueRook, bluePawn, blueQueen, blueKing, blueBishop, blueKnight;
	
	// **** Chess gameplay
	private ChessPiece avatar;
	private GameObject boardO;
	private ObjShape boardS;
	private TextureImage boardT;
	private Board boardL;
	private TextureImage tilesT;
	private boolean chessMovement = true;
	private boolean myTurn;
	private boolean done = false;
	
	// **** PHYSICS
	
	private PhysicsEngine physicsEngine;
	private PhysicsObject planeP;
	private int floorUID;
	private boolean running = false;
	boolean physicsRenderingOn = false;
	
	// **** SOUND
	private IAudioManager audioMgr;
	private Sound screamSound, welcomeSound, gameLoopSound;

	public MyGame(String serverAddress, int serverPort, String protocol) 
	{ 
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();
	}

	@Override
	public void loadShapes()
	{	
		ghostS = new Sphere();

		// Coordinate axes shapes (X, Y, Z)
		linxS = new Line(new Vector3f(0f,.1f,0f), new Vector3f(3f,.1f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,.1f,0f), new Vector3f(0f,.1f,-3f));

		// Terrain shape
		terrS = new TerrainPlane(512);

		// Game Piece Shapes
		rookS = new ImportedModel("Rook_v2.obj");
		//kingS = new ImportedModel("King_v2.obj");
		queenS = new ImportedModel("Queen_v2.obj");
		knightS = new ImportedModel("Knight_v2.obj");
		pawnS = new ImportedModel("Pawn_v2.obj");
		bishopS = new ImportedModel("Bishop_v2.obj");
		
		// Board
		boardS = new Plane();

		// MILESTONE 2 - NPC Shape
		npcS = new ImportedModel("NPC.obj");

		// MILESTONE 2 - Animated King Piece Shape
		kingSRed = new AnimatedShape("King_v2.rkm", "King_v2.rks");
		kingSRed.loadAnimation("waveSword", "King_v2_waveSword.rka");
		kingSRed.loadAnimation("waveHand", "King_v2_waveHand.rka");

		kingSBlue = new AnimatedShape("King_v2.rkm", "King_v2.rks");
		kingSBlue.loadAnimation("waveSword", "King_v2_waveSword.rka");
		kingSBlue.loadAnimation("waveHand", "King_v2_waveHand.rka");
		
		selectorS = new ImportedModel("selector.obj");
	}

	@Override
	public void loadTextures()
	{	
		ghostT = new TextureImage("redDolphin.jpg");

		// Game Textures
		hills = new TextureImage("hills2.jpg"); // This is your grayscale height map

		// Desert texture from Polyhaven - https://polyhaven.com/a/mud_cracked_dry_03
    	grass = new TextureImage("mud_cracked_dry_03.jpg"); // This is what the ground actually looks like
		boardT = new TextureImage("GroundTxt.jpg");
		
		//Tiles texture
		tilesT = new TextureImage("GrassText.jpg");
		
		rooktxRed = new TextureImage("RedRook_v2.jpg"); // Texture for the rook piece
		rooktxBlue = new TextureImage("BlueRook_v2.jpg");
		kingtxRed = new TextureImage("RedKing_v2.jpg"); // Texture for the king piece
		kingtxBlue = new TextureImage("BlueKing_v2.jpg");
		queentxRed = new TextureImage("RedQueen_v2.jpg"); // Texture for the queen piece
		queentxBlue = new TextureImage("BlueQueen_v2.jpg");
		knighttxRed = new TextureImage("RedKnight_v2.jpg"); // Texture for the knight piece
		knighttxBlue = new TextureImage("BlueKnight_v2.jpg");
		pawntxRed = new TextureImage("RedPawn_v2.jpg"); // Texture for the pawn piece
		pawntxBlue = new TextureImage("BluePawn_v2.jpg");
		bishoptxRed = new TextureImage("RedBishop_v2.jpg"); // Texture for the bishop piece
		bishoptxBlue = new TextureImage("BlueBishop_v2.jpg");

		// MILESTONE 2 - NPC Texture
		npcTx = new TextureImage("NPCtx.jpg");
		
		selectorT = new TextureImage("selector.png");
	}

	@Override
	public void loadSkyBoxes()
	{
    	// "gradientSky" built in GIMP - gradient blue sky texture
    	// inside assets/skyboxes/
    	gradientSky = (engine.getSceneGraph()).loadCubeMap("gradientSky");
    	(engine.getSceneGraph()).setActiveSkyBoxTexture(gradientSky);
    	(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialScale, initialRotation;
		
		// 45 deg rotation
		float angle = (float) Math.toRadians(45.0); 
		
		// Build the Terrain
		terr = new GameObject(GameObject.root(), terrS, grass);
    	terr.setLocalTranslation((new Matrix4f()).translation(0f, 0f, 0f));
    	// The Y-scale (10.0f here) determines how TALL your mountains are.
    	terr.setLocalScale((new Matrix4f()).scaling(50.0f, 10.0f, 50.0f));
    	terr.setHeightMap(hills);
    	// Tiling makes the grass texture repeat so it doesn't look blurry
    	terr.getRenderStates().setTiling(1);
    	terr.getRenderStates().setTileFactor(20);
		terr.getRenderStates().hasLighting(true);
		
		selector = new GameObject(GameObject.root(), selectorS, selectorT);
		selector.setLocalScale((new Matrix4f()).scaling(2.5f));

		
		//----------BUILD THE PIECES----------
		
		//Build the red pawns
		for (int i = 0; i < 8; i++)
		{
			opponentPieces[i] = new ChessPiece(i, "Pawn", (char)(104-i) + "7", pawnS, pawntxRed);
			opponentPieces[i].setLocalTranslation((new Matrix4f()).translation((float)-17.5+5*i, 0f, 12.5f));
			opponentPieces[i].setLocalScale((new Matrix4f()).scaling(1.0f));
			opponentPieces[i].getRenderStates().hasLighting(true);
			opponentPieces[i].setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		}
		
		//Build the red rook pieces
		for (int i = 0; i < 2; i++)
		{
			opponentPieces[i+8] = new ChessPiece(i+8, "Rook", (char)(104-(i*7)) + "8", rookS, rooktxRed);
			opponentPieces[i+8].setLocalTranslation((new Matrix4f()).translation((float)-17.5+35*i, 0f, 17.5f));
			opponentPieces[i+8].setLocalScale((new Matrix4f()).scaling(1.0f));
			opponentPieces[i+8].getRenderStates().hasLighting(true);
			opponentPieces[i+8].setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		}
		
		//Build the red knight pieces 
		for (int i = 0; i < 2; i++)
		{
			opponentPieces[i+10] = new ChessPiece(i+10, "Knight", (char)(103-(i*5)) + "8", knightS, knighttxRed);
			opponentPieces[i+10].setLocalTranslation((new Matrix4f()).translation((float)-12.5+25*i, 0f, 17.5f));
			opponentPieces[i+10].setLocalScale((new Matrix4f()).scaling(1.0f));
			opponentPieces[i+10].getRenderStates().hasLighting(true);
			opponentPieces[i+10].setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		}
		
		//Build the red bishop pieces 
		for (int i = 0; i < 2; i++)
		{
			opponentPieces[i+12] = new ChessPiece(i+12, "Bishop", (char)(102-(i*3)) + "8", bishopS, bishoptxRed);
			opponentPieces[i+12].setLocalTranslation((new Matrix4f()).translation((float)-7.5+15*i, 0f, 17.5f));
			opponentPieces[i+12].setLocalScale((new Matrix4f()).scaling(1.0f));
			opponentPieces[i+12].getRenderStates().hasLighting(true);
			opponentPieces[i+12].setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		}
		
		//Build the red king piece
		redKing = new ChessPiece(14, "King", "e8", kingSRed, kingtxRed);
		redKing.setLocalTranslation((new Matrix4f()).translation(-2.5f, 0f, 17.5f));
		redKing.setLocalScale((new Matrix4f()).scaling(1.0f));
		redKing.getRenderStates().hasLighting(true);
		redKing.setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		opponentPieces[14] = redKing;
		
		//Build the red queen piece
		opponentPieces[15] = new ChessPiece(15, "Queen", "d8", queenS, queentxRed);
		opponentPieces[15].setLocalTranslation((new Matrix4f()).translation(2.5f, 0f, 17.5f));
		opponentPieces[15].setLocalScale((new Matrix4f()).scaling(1.0f));
		opponentPieces[15].getRenderStates().hasLighting(true);
		opponentPieces[15].setLocalRotation((new Matrix4f()).rotationY((float)Math.toRadians(180f)));
		
		
		//Build the blue pawns
		for (int i = 0; i < 8; i++)
		{
			playerPieces[i] = new ChessPiece(i, "Pawn", (char)(97+i) + "2", pawnS, pawntxBlue);
			playerPieces[i].setLocalTranslation((new Matrix4f()).translation((float)17.5-5*i, 0f, -12.5f));
			playerPieces[i].setLocalScale((new Matrix4f()).scaling(1.0f));
			playerPieces[i].getRenderStates().hasLighting(true);
		}
		avatar = playerPieces[0];
		
		//Build the blue rook pieces
		for (int i = 0; i < 2; i++)
		{
			playerPieces[i+8] = new ChessPiece(i+8, "Rook", (char)(97+(i*7)) + "1", rookS, rooktxBlue);
			playerPieces[i+8].setLocalTranslation((new Matrix4f()).translation((float)17.5-35*i, 0f, -17.5f));
			playerPieces[i+8].setLocalScale((new Matrix4f()).scaling(1.0f));
			playerPieces[i+8].getRenderStates().hasLighting(true);
		}
		
		//Build the blue knight pieces 
		for (int i = 0; i < 2; i++)
		{
			playerPieces[i+10] = new ChessPiece(i+10, "Knight", (char)(98+(i*5)) + "1", knightS, knighttxBlue);
			playerPieces[i+10].setLocalTranslation((new Matrix4f()).translation((float)12.5-25*i, 0f, -17.5f));
			playerPieces[i+10].setLocalScale((new Matrix4f()).scaling(1.0f));
			playerPieces[i+10].getRenderStates().hasLighting(true);
		}
		
		//Build the blue bishop pieces 
		for (int i = 0; i < 2; i++)
		{
			playerPieces[i+12] = new ChessPiece(i+12, "Bishop", (char)(99+(i*3)) + "1", bishopS, bishoptxBlue);
			playerPieces[i+12].setLocalTranslation((new Matrix4f()).translation((float)7.5-15*i, 0f, -17.5f));
			playerPieces[i+12].setLocalScale((new Matrix4f()).scaling(1.0f));
			playerPieces[i+12].getRenderStates().hasLighting(true);
		}
		
		//Build the blue king piece
		blueKing = new ChessPiece(14, "King", "e1", kingSBlue, kingtxBlue);
		blueKing.setLocalTranslation((new Matrix4f()).translation(-2.5f, 0f, -17.5f));
		blueKing.setLocalScale((new Matrix4f()).scaling(1.0f));
		blueKing.getRenderStates().hasLighting(true);
		playerPieces[14] = blueKing;
		
		//Build the blue queen piece
		playerPieces[15] = new ChessPiece(15, "Queen", "d1", queenS, queentxBlue);
		playerPieces[15].setLocalTranslation((new Matrix4f()).translation(2.5f, 0f, -17.5f));
		playerPieces[15].setLocalScale((new Matrix4f()).scaling(1.0f));
		playerPieces[15].getRenderStates().hasLighting(true);

		// MILESTONE 2 - Build NPC
		npc = new GameObject(GameObject.root(), npcS, npcTx);
		npc.setLocalTranslation((new Matrix4f()).translation(0f, 0f, 0f));
		npc.setLocalScale((new Matrix4f()).scaling(1.0f));
		npc.setLocalRotation((new Matrix4f()).rotationY((float) Math.toRadians(90.0f)));
		npc.getRenderStates().hasLighting(true);

		// Build the Coordinate Axes so you can see X, Y, and Z
		x = new GameObject(GameObject.root(), linxS);
		x.getRenderStates().setColor(new Vector3f(1f, 0f, 0f));
		y = new GameObject(GameObject.root(), linyS);
		y.getRenderStates().setColor(new Vector3f(0f, 1f, 0f));
		z = new GameObject(GameObject.root(), linzS);
		z.getRenderStates().setColor(new Vector3f(0f, 0f, 1f));
		
		//Build the (physical) board
		boardO = new GameObject(GameObject.root(), boardS, boardT);
		boardO.getRenderStates().setTiling(1);
    	boardO.getRenderStates().setTileFactor(4);
		boardO.setLocalScale(new Matrix4f().scaling(20f));
		boardO.setLocalTranslation((new Matrix4f()).translation(0f, 0.01f, 0f));
		
		//Build the (logic) board
		boardL = new Board(boardS, tilesT);
		
	}

	@Override
	public void initializeLights() {   
    	// Global Ambient: Providing a base level of visibility
    	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);

		// *** LIGHTS FOR : Avatar, NPC, and the Ghost
    	// Light 1 - Avatar (White Light)
    	light1 = new Light();
    	light1.setDiffuse(1.0f, 1.0f, 1.0f);
		light1.setSpecular(0.0f, 0.0f, 1.0f);
    	//light1.setLocation(new Vector3f(-3.0f, 5.0f, 3.0f));
		light1.setConstantAttenuation(1.0f);
		light1.setLinearAttenuation(0.05f);
		light1.setQuadraticAttenuation(0.005f); 
    	(engine.getSceneGraph()).addLight(light1);

    	// Light 2 - NPC (Green / Cyan)
    	light2 = new Light();
    	light2.setDiffuse(0.0f, 1.0f, 0.0f);
		light2.setSpecular(1.0f, 1.0f, 0.0f);
    	//light2.setLocation(new Vector3f(19.0f, 5.0f, -21.0f));
		light2.setConstantAttenuation(1.0f);
		light2.setLinearAttenuation(0.01f);
		light2.setQuadraticAttenuation(0.001f);
    	(engine.getSceneGraph()).addLight(light2);

    	// Light 3 - Ghost (Yellow / Gold)
    	light3 = new Light();
    	light3.setDiffuse(1.0f, 1.0f, 0.0f); 
		light3.setSpecular(1.0f, 0.84f, 0.0f);
    	//light3.setLocation(new Vector3f(32.0f, 5.0f, 4.0f));
		light3.setConstantAttenuation(1.0f);
		light3.setLinearAttenuation(0.05f);
		light3.setQuadraticAttenuation(0.005f);
    	(engine.getSceneGraph()).addLight(light3);

    	// Light 4 (origin) - 
    	light4 = new Light();
    	light4.setDiffuse(1.0f, 1.0f, 1.0f); // Pure Yellow
		light4.setSpecular(1.0f, 1.0f, 1.0f);
    	light4.setLocation(new Vector3f(0.0f, 5.0f, -0.0f));
		light4.setConstantAttenuation(1.0f);
		light4.setLinearAttenuation(0.01f);
		light4.setQuadraticAttenuation(0.001f);
    	(engine.getSceneGraph()).addLight(light4);
}

	@Override
	public void createViewports() 
	{
    	// Main Viewport (Full Screen background)
    	(engine.getRenderSystem()).addViewport("LEFT", 0.0f, 0.0f, 1.0f, 1.0f);
    
    	// Overhead Viewport (Bottom Right corner, 25% size)
    	(engine.getRenderSystem()).addViewport("RIGHT", 0.75f, 0.0f, 0.25f, 0.25f);
		
    	Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
   	 	Viewport rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
    
   		Camera leftCamera = leftVp.getCamera();
   		Camera rightCamera = rightVp.getCamera();
    
 		// Style the overhead viewport
   		rightVp.setHasBorder(true);
   		rightVp.setBorderWidth(4);
  		rightVp.setBorderColor(0.0f, 1.0f, 0.0f); // Green border
    
   	 	// Initial Main Camera setup
   	 	leftCamera.setLocation(new Vector3f(-2, 1, 3));
   		leftCamera.setU(new Vector3f(1, 0, 0));
   	 	leftCamera.setV(new Vector3f(0, 1, 0));
    	leftCamera.setN(new Vector3f(0, 0, -1));
		
    	// Overhead Camera setup - pointing straight down (Y-axis)
    	rightCamera.setLocation(new Vector3f(0, 35, 0)); // Start 35 units above to see whole map
    	rightCamera.setU(new Vector3f(-1, 0, 0));
    	rightCamera.setV(new Vector3f(0, 0, 1));
    	rightCamera.setN(new Vector3f(0, -1, 0));
	}

	@Override
	public void initializeGame()
	{	
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
		
		setupNetworking();
		
		// --------------INPUTS & CAMERA SECTION----------------------
		im = engine.getInputManager();
		String gpName = im.getFirstGamepadName();
		Camera cam = engine.getRenderSystem().getViewport("LEFT").getCamera();
		
		// ---A2 REQUIREMENT: Create the CameraOrbit3D controller and associate it with the avatar and camera ---
		orbitController = new CameraOrbit3D(cam, avatar, gpName, engine, 0f, 20f, 6.5f);
		
		// Instantiate the actions
    	ZoomOverheadAction zoomOverhead = new ZoomOverheadAction();
    	PanOverheadAction panOverhead = new PanOverheadAction();
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
		PitchAction pitchAction = new PitchAction(this);
		TakePhotoAction takePhotoAction = new TakePhotoAction(this);
		SpaceBarAction spaceAction = new SpaceBarAction(this, protClient);
		JumpAction jumpAction = new JumpAction(this);
		ToggleAxesAction toggleAxesAction = new ToggleAxesAction(this);

		// Controller mappings (using JInput identifiers for an 8BitDo SN30 pro+ controller)
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._1,
			fwdAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, 
			turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, 
			fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._0, 
			takePhotoAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._3, 
    		jumpAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		

		// KEYBOARD MAPPINGS
		// Register Photo Key (P)
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.P, 
			takePhotoAction,InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Register Viewport Zoom Keys (Q and E)
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, 
        	zoomOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, 
        	zoomOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// Register Viewport Pan Keys (I, K, J, L)
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.I, 
        	panOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.K, 
        	panOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.J, 
        	panOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.L, 
        	panOverhead, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// Register WASD for movement and turning
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W,
			fwdAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S,
			fwdAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, 
    		turnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, 
    		turnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Register Keyboard UP and DOWN arrow
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, 
    		pitchAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, 
    		pitchAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// Register Space Bar to complete the game
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, 
    		spaceAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Register G key to toggle axes visibility
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, 
			toggleAxesAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		//Room for more mappings...
		//keeping keyPressed class for WASD as of now, but can easily move to Action classes if desired
		
	}
	
	@Override
	public void initializePhysicsObjects()
	{
		float[] gravity = {0f, -9.8f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		
		// -- create physics world --
		float mass = 10.0f;
		float up[] = {0,1,0};
		float radius = 1.0f;
		float height = 1.25f;
		Vector3f loc;
		Quaternionf rot;
		
		
		for(int i = 0; i < playerPieces.length; i++)
		{
			
			rot = new Quaternionf();
			playerPieces[i].setPhysicsObject((engine.getSceneGraph()).addPhysicsCylinder(mass,
			playerPieces[i].getWorldLocation(), (playerPieces[i].getWorldRotation()).getNormalizedRotation(rot), 1, radius, height));
			playerPieces[i].getPhysicsObject().setLocation((new float[]{playerPieces[i].getPhysicsObject().getLocation().x(), 
			playerPieces[i].getPhysicsObject().getLocation().y() + 1.25f, playerPieces[i].getPhysicsObject().getLocation().z()}));
			playerPieces[i].getPhysicsObject().setBounciness(0.0f);
			playerPieces[i].getPhysicsObject().disableSleeping();
			
			
			rot = new Quaternionf();
			opponentPieces[i].setPhysicsObject((engine.getSceneGraph()).addPhysicsCylinder(mass, 
			opponentPieces[i].getWorldLocation(), (opponentPieces[i].getWorldRotation()).getNormalizedRotation(rot), 1, radius, height));
			opponentPieces[i].getPhysicsObject().setLocation((new float[]{opponentPieces[i].getPhysicsObject().getLocation().x(), 
			opponentPieces[i].getPhysicsObject().getLocation().y() + 1.25f, opponentPieces[i].getPhysicsObject().getLocation().z()}));
			opponentPieces[i].getPhysicsObject().setBounciness(0.0f);
			opponentPieces[i].getPhysicsObject().disableSleeping();
			
		}
		
		loc = terr.getWorldLocation();
		rot = new Quaternionf();
		(terr.getWorldRotation()).getNormalizedRotation(rot);
		planeP = (engine.getSceneGraph()).addPhysicsStaticTerrainMesh(loc, rot, hills, 50.0f, 10f, 100);
		planeP.setBounciness(0.0f);
		terr.setPhysicsObject(planeP);
		
		floorUID = terr.getPhysicsObject().getUID();
		
		engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();
	}
	
	@Override
	public void loadSounds()
	{
		AudioResource resource1, resource2;
		audioMgr = engine.getAudioManager();
		
		
		// Scream Sound Source: https://pixabay.com/sound-effects/people-male-death-scream-horror-352706/
		resource1 = audioMgr.createAudioResource("scream.wav", AudioResourceType.AUDIO_SAMPLE);
		screamSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
		screamSound.initialize(audioMgr);
		screamSound.setMaxDistance(10.0f);
		screamSound.setMinDistance(0.5f);
		screamSound.setRollOff(5.0f);

		// Welcome Message Sound Source: Created in Reason13 by Spencer Green
		resource2 = audioMgr.createAudioResource("WelcomeToChessnt.wav", AudioResourceType.AUDIO_SAMPLE);
    	welcomeSound = new Sound(resource2, SoundType.SOUND_EFFECT, 70, false);
   		welcomeSound.initialize(audioMgr);
		welcomeSound.setMaxDistance(15.0f);
    	welcomeSound.setMinDistance(0.5f);
    	welcomeSound.setRollOff(5.0f);

		AudioResource resLoop = audioMgr.createAudioResource("GameLoop_S.wav", AudioResourceType.AUDIO_SAMPLE);
    	gameLoopSound = new Sound(resLoop, SoundType.SOUND_EFFECT, 50, false); // true = looping
    	gameLoopSound.initialize(audioMgr);
		gameLoopSound.setMaxDistance(15.0f);
		gameLoopSound.setMinDistance(0.5f);
		gameLoopSound.setRollOff(5.0f);
		
	}
	
	public void setEarParameters()
	{
		Camera camera = (engine.getRenderSystem()).getViewport("LEFT").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	// ----- GETTERS for use in Action classes -----------
	public ChessPiece getAvatar() { return avatar; }
	public Engine getEngine() { return engine; }
	public boolean getIsRiding() { return isRiding; }
	public void setHUDMessage(String m) { hudMessage = m; }

	public GameObject getHome() { return home; }
	public void setIsGameWon(boolean b) { isGameWon = b; }
	public void setVertVel(float v) { vertVel = v; }

	public boolean getAxesVisible() { return axesVisible; }
	public void setAxesVisible(boolean b) { axesVisible = b; }
	public GameObject getXAxis() { return x; }
	public GameObject getYAxis() { return y; }
	public GameObject getZAxis() { return z; }
	
	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }

	public ChessPiece getOpponentPiece(int id) {return opponentPieces[id];}
	public int getPieceId() {return id;}
	public boolean getChessM() {return chessMovement;}
	public Board getBoard() {return boardL;}
	public boolean getTurn() {return myTurn;}
	public void setTurn(boolean state) {myTurn = state;}
	public void toggleTurn() {myTurn = !myTurn;}
	
	public boolean getRunning(){return running;}

	public Sound getWelcomeSound() { return welcomeSound; }
	public Sound getGameLoopSound() { return gameLoopSound; }
	
	public boolean getIsGameDone()
	{
		if(isGameOver || isGameWon){return true;}
		else{return false;}
	}

	@Override
	public void update()
	{	
		// Update the elapsed time (this is always first)
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		float deltaTime = (float) (currFrameTime - lastFrameTime);
    	if (!paused) elapsTime += deltaTime / 1000.0;

		setEarParameters(); // Update ear parameters for 3D sound each frame

		// Update the input manager to process any new input events
		im = engine.getInputManager();
    	im.update(deltaTime);

		// ---A2 REQUIREMENT: Update the Orbit Camera ---
    	// This must happen every frame so the camera follows the avatar
    	orbitController.updateCameraPosition(avatar);
		
		processNetworking((float)elapsTime);
	
		// GAME OVER LOGIC - stop updating game logic
    	if (isGameOver) {
			im = engine.getInputManager();
    		im.update(deltaTime);
			System.out.println("Game Over; You Lose.");
			return; 
    	}

		// GAME WIN LOGIC - stop updating game logic and show win message
		if (isGameWon) {
			im = engine.getInputManager();
    		im.update(deltaTime);
			System.out.println("Game Over; You Win.");
			return; 
		// MAIN GAME LOGIC - updates only happen if the game isn't won or lost yet
		} else {
			Vector3f loc = avatar.getWorldLocation();

			// TERRAIN SNAP LOGIC
			// Get the actual height of the terrain at this specific (x, z)
    		float groundHeight = terr.getHeight(loc.x, loc.z);
			//float dolphinOffset = 0.8f; // Offset to keep dolphin above ground.
			float adjustedHeight = groundHeight /*+ dolphinOffset*/;
			avatar.setLocalLocation(new Vector3f(loc.x, loc.y + vertVel, loc.z));

			// DATA CALCULATION FOR HUD
			int elapsTimeSec = Math.round((float)elapsTime);
			// Get current window dimensions for relative positioning
			int windowWidth = engine.getRenderSystem().getWidth();
			int windowHeight = engine.getRenderSystem().getHeight();

			// Calculate X to be at the start of the RIGHT viewport (75% across)
        	int hud2X = (int)(windowWidth * 0.76f);
        	int hud2Y = 15; // Bottom of the overhead viewport

			// --- HUD 1: MAIN VIEWPORT (Bottom Left) ---
			String mainDisp = " Time: " + elapsTimeSec + " | " + hudMessage;
        	(engine.getHUDmanager()).setHUD1(mainDisp, new Vector3f(1,1,1), 15, 15);
			
			// --- HUD 2: OVERHEAD VIEWPORT (Relative to Right Window) ---
        	Vector3f pos = avatar.getWorldLocation();
        	String coordStr = String.format("Avatar x: %.1f y: %.1f z: %.1f", pos.x(), pos.y(), pos.z());

			(engine.getHUDmanager()).setHUD2(coordStr, new Vector3f(1,1,0), hud2X, hud2Y);
			
			// --- Display moveable tiles logic
			boardL.showMoves(boardL.validMoves(avatar));
			
			// Update Physics
			if(running)
			{
				physicsEngine.update((float)elapsTime/1000f);
				for(GameObject go: engine.getSceneGraph().getGameObjects())
				{
					if(go.getPhysicsObject() != null)
					{
						Vector3f oldObjPos, up, newObjPos;
						
						//set initial translation
						loc = go.getPhysicsObject().getLocation();
						Matrix4f locMat = new Matrix4f();
						locMat.set(3, 0, loc.x); locMat.set(3, 1, loc.y); locMat.set(3, 2, loc.z);
						go.setLocalTranslation(locMat);
						
						
						//Offset for chess pieces so the physics object properly aligns with them.
						if(go.getShape() != terrS)
						{
							oldObjPos = go.getWorldLocation();
							up = go.getLocalUpVector();
							up.mul(-1.25f);
							newObjPos = oldObjPos.add(up);
							go.setLocalLocation(newObjPos);
						}
						
						//set rotation
						Quaternionf rot = go.getPhysicsObject().getRotation();
						Matrix4f rotMat = new Matrix4f();
						rot.get(rotMat);
						go.setLocalRotation(rotMat);
					}
				}
				
				physicsEngine.detectCollisions();
				
				if(myTurn || gm.isGhostAvatar())
				{
					//List those physics objects that have collided with the piece
					HashSet<PhysicsObject> newCollisions = avatar.getPhysicsObject().getNewlyCollidedSet();
					if(newCollisions.size()>0)
					{
						System.out.print(avatar.getType() + " Piece collides with ");
						for(PhysicsObject po: newCollisions)
						{
							System.out.print(po + " ");
							if(po.getUID() != floorUID)
							{
								for(int i = 0; i < playerPieces.length; i++)
								{
									if(opponentPieces[i].getPhysicsObject() == po)
									{
										opponentPieces[i].getRenderStates().disableRendering();
										screamSound.setLocation(opponentPieces[i].getWorldLocation());
										setEarParameters();
										screamSound.play();
										if(opponentPieces[i] == opponentPieces[14]){isGameWon = true;}
									}
								}
								(engine.getSceneGraph()).removePhysicsObject(po);
							}
						}
						System.out.println();
					}
				}
				else
				{
					for(ChessPiece piece: playerPieces)
					{
						HashSet<PhysicsObject> newCollisions = piece.getPhysicsObject().getNewlyCollidedSet();
						if(newCollisions.size() > 0)
						{
							for(PhysicsObject po: newCollisions)
							{
								if(po.getUID() != floorUID)
								{
									piece.getRenderStates().disableRendering();
									screamSound.setLocation(piece.getWorldLocation());
									setEarParameters();
									screamSound.play();
									if(piece == playerPieces[14]){isGameOver = true;}
								}
							}
						}
					}
				}
			}
			selector.setLocalTranslation(avatar.getWorldTranslation());
		}

		// Update both king shapes
    	if (kingSRed != null) kingSRed.updateAnimation();
    	if (kingSBlue != null) kingSBlue.updateAnimation();
		
		//Sets up turn-based switching.
		// Doesn't work--Figure out a new way to do it
		/*
		if(!done && elapsTime > 10f)
		{
			if(gm.isGhostAvatar()){myTurn = false;}
			System.out.println("Initial Check! Value is " + myTurn);
			done = true;
		}
		//else if(done && elapsTime > 6f){System.out.println("Further Checks. Value is: " + myTurn);}
		*/
		
		//System.out.println("Value is: " + myTurn);

		// --- LIGHTING SNAP LOGIC ---
		// Update Light 1 to follow the Avatar
    	if (avatar != null && light1 != null) {
        	Vector3f aPos = avatar.getWorldLocation();
        	light1.setLocation(new Vector3f(aPos.x(), aPos.y() + 2.5f, aPos.z()));
    	}

		// Update Light 2 to follow the NPC
		if (npc != null && light2 != null) {
        	Vector3f nPos = npc.getWorldLocation();
        	light2.setLocation(new Vector3f(nPos.x(), nPos.y() + 2.5f, nPos.z()));
    	}

		// Update Light 3 to follow the ghost avatar (if it exists)
		if (gm != null && light3 != null) {
    		Vector<GhostAvatar> ghosts = gm.getGhostAvatars();
    
    		if (ghosts != null && !ghosts.isEmpty()) {
        		GhostAvatar targetGhost = ghosts.get(0); // Grab the first ghost player in the list
				int id = targetGhost.getCurrentPieceID(); // Get the piece ID that the ghost is currently controlling
				Vector3f gPos = getOpponentPiece(id).getWorldLocation(); // Option 1: Get the position of the piece the ghost is controlling (if you want the light to follow the piece rather than the ghost itself)
				
				// Vector3f gPos = targetGhost.getPosition(); // OPTION 2: Get the ghost's actual position (if you want the light to follow the ghost itself rather than the piece it's controlling)
        		
        		light3.setLocation(new Vector3f(gPos.x(), gPos.y() + 2.5f, gPos.z())); // Snap the light to the piece
    		} else {
        		// If no player 2 is connected, hide the light underground
        		light3.setLocation(new Vector3f(0f, -100f, 0f));
    		}
		}
	}

	// VIEWPORT ZOOM - as per prompt
	private class ZoomOverheadAction extends AbstractInputAction {
        public void performAction(float time, net.java.games.input.Event event) {
            Camera cam = (engine.getRenderSystem()).getViewport("RIGHT").getCamera();
            Vector3f loc = cam.getLocation();
            float zoomAmount = 0.05f;
            
            // Zoom Out (Up) with Q, Zoom In (Down) with E
            if (event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.Q) {
                cam.setLocation(new Vector3f(loc.x(), loc.y() + zoomAmount, loc.z()));
            } else if (event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.E) {
                cam.setLocation(new Vector3f(loc.x(), loc.y() - zoomAmount, loc.z()));
            }
        }
    }

	// VIEWPOERT PAN - as per prompt
    private class PanOverheadAction extends AbstractInputAction {
        public void performAction(float time, net.java.games.input.Event event) {
            Camera cam = (engine.getRenderSystem()).getViewport("RIGHT").getCamera();
            Vector3f loc = cam.getLocation();
            float panDist = 0.05f;
            net.java.games.input.Component.Identifier.Key key = 
                (net.java.games.input.Component.Identifier.Key)event.getComponent().getIdentifier();

            if (key == net.java.games.input.Component.Identifier.Key.I) cam.setLocation(new Vector3f(loc.x(), loc.y(), loc.z() - panDist));
            if (key == net.java.games.input.Component.Identifier.Key.K) cam.setLocation(new Vector3f(loc.x(), loc.y(), loc.z() + panDist));
            if (key == net.java.games.input.Component.Identifier.Key.J) cam.setLocation(new Vector3f(loc.x() - panDist, loc.y(), loc.z()));
            if (key == net.java.games.input.Component.Identifier.Key.L) cam.setLocation(new Vector3f(loc.x() + panDist, loc.y(), loc.z()));
        }
    }
 
	@Override
	public void keyPressed(KeyEvent e)
	{	Vector3f loc, fwd, newLocation, up , right;
		Camera cam = engine.getRenderSystem().getViewport("LEFT").getCamera();
		
		float moveSpeed = 0.5f; // units per key press
    	float turnSpeed = 3.0f; // degrees
		float turnAmount = (float)Math.toRadians(5.0f);
		
		
		switch (e.getKeyCode())
		{	
			case KeyEvent.VK_ESCAPE:
            System.exit(0);
            break;

			//case KeyEvent.VK_C: counter++; break;
			case KeyEvent.VK_1: paused = !paused; break;
			case KeyEvent.VK_4:
				(engine.getRenderSystem().getViewport("LEFT").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
			case KeyEvent.VK_5:
				(engine.getRenderSystem().getViewport("LEFT").getCamera()).setLocation(new Vector3f(0,0,5));
				break;
			case KeyEvent.VK_8: // Press 8 to toggle skybox
            	boolean isEnabled = (engine.getSceneGraph()).isSkyboxEnabled();
            	(engine.getSceneGraph()).setSkyBoxEnabled(!isEnabled);
            	break;
			case KeyEvent.VK_9:
				chessMovement = !chessMovement;
				break;
			case KeyEvent.VK_0:
				if (id == 15) {id = 0;}
				else {id++;}
				avatar = playerPieces[id];
				break;
			
			//PHYSICS CONTROLS
			case KeyEvent.VK_2:
				running = !running;
				break;
			case KeyEvent.VK_3:
				if(!physicsRenderingOn){engine.enablePhysicsWorldRender();}
				else{engine.disablePhysicsWorldRender();}
				physicsRenderingOn = !physicsRenderingOn;
				break;

			// ---- MILESTONE 2: ANIMATION TRIGGERS ----
        	case KeyEvent.VK_V: // Red King: Wave Sword
            	if (redKing != null) 
				{
                	redKing.playAction("waveSword", 0.5f, AnimatedShape.EndType.LOOP);
            	}
				break;
        	case KeyEvent.VK_B: // Red King: Wave Hand
            	if (redKing != null) 
                {
					redKing.playAction("waveHand", 0.5f, AnimatedShape.EndType.LOOP);
            	}
				break;
        	case KeyEvent.VK_N: // Blue King: Wave Sword
            	System.out.println("N pressed");
				if (blueKing != null)
				{
					System.out.println("blueKing is NOT null");
                	blueKing.playAction("waveSword", 0.5f, AnimatedShape.EndType.LOOP);
				}
				else{System.out.println("blueKing is null");}
            	break;
        	case KeyEvent.VK_M: // Blue King: Wave Hand
            	if (blueKing != null) 
                {	
					blueKing.playAction("waveHand", 0.5f, AnimatedShape.EndType.LOOP);
            	}
				break;
        	case KeyEvent.VK_Z: // Stop All Animations
            	if (redKing != null) {redKing.stopAction();}
            	if (blueKing != null) {blueKing.stopAction();}
            	break;
		}
		super.keyPressed(e);
	}
	
	
	private void setupNetworking()
	{	
		isClientConnected = false;	
		try 
		{	
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	
		catch (UnknownHostException e) 
		{	
			e.printStackTrace();
		}	
		catch (IOException e) 
		{	
			e.printStackTrace();
		}
		if (protClient == null)
		{	
			System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();

			// --- ADDED FOR NPC INITIALIZATION ---
        	// Request the NPC information from the server right after joining
        	System.out.println("requesting NPC data");
        	protClient.askForNPC();
		}
	}
	
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}
	
	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
}

