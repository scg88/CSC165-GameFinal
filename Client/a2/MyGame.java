package a2;

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

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private GhostManager gm;
	ChessPiece[] playerPieces = new ChessPiece[6];
	ChessPiece[] opponentPieces = new ChessPiece[6];
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
	private GameObject dol, avatar, x, y, z, home;
	private ObjShape dolS, linxS, linyS, linzS, homeS, ghostS;
	private TextureImage doltx, bricktx, ghostT;
	private Light light1, light2, light3, light4;
	
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// **** Chess like Game Pieces
	// **** MILESTONE 1
	private GameObject rookBlue, rookRed, kingRed, kingBlue, queenRed, queenBlue, 
		knightRed, knightBlue, pawnRed, pawnBlue, bishopRed, bishopBlue;
	private TextureImage rooktxRed, rooktxBlue, kingtxRed, kingtxBlue, queentxRed, 
		queentxBlue, knighttxRed, knighttxBlue, pawntxRed, pawntxBlue, bishoptxRed, bishoptxBlue;
	private ObjShape rookS, kingS, queenS, knightS, pawnS, bishopS;

	// **** Skybox
	// **** MILESTONE 1
	private int gradientSky; // Blue gradient skybox made on GIMP

	// **** Terrain map
	// **** MILESTONE 1
	private GameObject terr;
	private ObjShape terrS;
	private TextureImage hills, grass;

	private CameraOrbit3D orbitController;
	private RotationController pyramidRotController;
	private BobbingController pyramidBobController;
	private BobbingController homeBobController;
	
	private ChessPiece redRook, redPawn, redQueen, redKing, redBishop, redKnight;
	private ChessPiece blueRook, bluePawn, blueQueen, blueKing, blueBishop, blueKnight;

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
		dolS = new ImportedModel("dolphinHighPoly.obj");
		homeS = new ManualHome();   // Custom shape for the home base
		ghostS = new Sphere();

		// Coordinate axes shapes (X, Y, Z)
		linxS = new Line(new Vector3f(0f,.1f,0f), new Vector3f(3f,.1f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,.1f,0f), new Vector3f(0f,.1f,-3f));

		// Terrain shape
		terrS = new TerrainPlane(512);

		// Game Piece Shapes
		rookS = new ImportedModel("RookPiece.obj");
		kingS = new ImportedModel("KingPiece.obj");
		queenS = new ImportedModel("QueenPiece.obj");
		knightS = new ImportedModel("KnightPiece.obj");
		pawnS = new ImportedModel("PawnPiece.obj");
		bishopS = new ImportedModel("BishopPiece.obj");

	}

	@Override
	public void loadTextures()
	{	
		doltx = new TextureImage("Dolphin_HighPolyUV.jpg");
		bricktx = new TextureImage("brick1.jpg");
		ghostT = new TextureImage("redDolphin.jpg");

		// Game Textures
		hills = new TextureImage("hills.jpg"); // This is your grayscale height map

		// Desert texture from Polyhaven - https://polyhaven.com/a/mud_cracked_dry_03
    	//grass = new TextureImage("mud_cracked_dry_03.jpg"); // This is what the ground actually looks like
		grass = new TextureImage("GroundTxt.jpg");
		
		rooktxRed = new TextureImage("RedRookTxt.jpg"); // Texture for the rook piece
		rooktxBlue = new TextureImage("BlueRookTxt.jpg");
		kingtxRed = new TextureImage("RedKingTxt.jpg"); // Texture for the king piece
		kingtxBlue = new TextureImage("BlueKingTxt.jpg");
		queentxRed = new TextureImage("RedQueenTxt.jpg"); // Texture for the queen piece
		queentxBlue = new TextureImage("BlueQueenTxt.jpg");
		knighttxRed = new TextureImage("RedKnightTxt.jpg"); // Texture for the knight piece
		knighttxBlue = new TextureImage("BlueKnightTxt.jpg");
		pawntxRed = new TextureImage("RedPawnTxt.jpg"); // Texture for the pawn piece
		pawntxBlue = new TextureImage("BluePawnTxt.jpg");
		bishoptxRed = new TextureImage("RedBishopTxt.jpg"); // Texture for the bishop piece
		bishoptxBlue = new TextureImage("BlueBishopTxt.jpg");
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

		// Build Dolphin at origin
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,0.8f,0f);
		avatar.setLocalTranslation(initialTranslation); // Start at the origin (0,0,0)
		// Rotate Dolphin to face game objects
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f)); // Rotate 135 to bisect the X and Z axes
		avatar.setLocalRotation(initialRotation);
		// Scale Dolphin
		initialScale = (new Matrix4f()).scaling(3.0f); 
		avatar.setLocalScale(initialScale);
		dol = avatar; // Alias from previous examples
		
		
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

		redRook = new ChessPiece(0, "Rook", rookS, rooktxRed);
		playerPieces[0] = redRook;
		redRook.setLocalTranslation((new Matrix4f()).translation(-2.5f, 1.1f, -12.5f));
    	redRook.setLocalScale((new Matrix4f()).scaling(1.0f));
    	redRook.getRenderStates().hasLighting(true);
		avatar = redRook;
		
		// Build Red the Rook Piece
    	/*
		rookRed = new GameObject(GameObject.root(), rookS, rooktxRed);
		rookRed.setLocalTranslation((new Matrix4f()).translation(-2.5f, 1.1f, -12.5f));
    	rookRed.setLocalScale((new Matrix4f()).scaling(1.0f));
    	rookRed.getRenderStates().hasLighting(true);
		*/
		
		blueRook = new ChessPiece(0, "Rook", rookS, rooktxBlue);
		opponentPieces[0] = blueRook;
		blueRook.setLocalTranslation((new Matrix4f()).translation(-2.5f, 1.1f, 12.5f));
    	blueRook.setLocalScale((new Matrix4f()).scaling(1.0f));
    	blueRook.getRenderStates().hasLighting(true);
		
		/*
		// Build Blue Rook Piece
    	rookBlue = new GameObject(GameObject.root(), rookS, rooktxBlue);
		rookBlue.setLocalTranslation((new Matrix4f()).translation(-2.5f, 1.1f, 12.5f));
    	rookBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
    	rookBlue.getRenderStates().hasLighting(true);
		*/
		
		// Build Red King Piece
		/*
		kingRed = new GameObject(GameObject.root(), kingS, kingtxRed);
		kingRed.setLocalTranslation((new Matrix4f()).translation(2.5f, 1.1f, -12.5f));
		kingRed.setLocalScale((new Matrix4f()).scaling(1.0f));
		kingRed.getRenderStates().hasLighting(true);
		*/
		
		redKing = new ChessPiece(1, "King", kingS, kingtxRed);
		playerPieces[1] = redKing;
		redKing.setLocalTranslation((new Matrix4f()).translation(2.5f, 1.1f, -12.5f));
		redKing.setLocalScale((new Matrix4f()).scaling(1.0f));
		redKing.getRenderStates().hasLighting(true);
		
		blueKing = new ChessPiece(1, "King", kingS, kingtxBlue);
		opponentPieces[1] = blueKing;
		blueKing.setLocalTranslation((new Matrix4f()).translation(2.5f, 1.1f, 12.5f));
		blueKing.setLocalScale((new Matrix4f()).scaling(1.0f));
		blueKing.getRenderStates().hasLighting(true);
		
		// Build Blue King Piece
		/*
		kingBlue = new GameObject(GameObject.root(), kingS, kingtxBlue);
		kingBlue.setLocalTranslation((new Matrix4f()).translation(2.5f, 1.1f, 12.5f));
		kingBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
		kingBlue.getRenderStates().hasLighting(true);
		*/
		
		redQueen = new ChessPiece(2, "Queen", queenS, queentxRed);
		playerPieces[2] = redQueen;
		redQueen.setLocalTranslation((new Matrix4f()).translation(7.5f, 1.1f, -12.5f));
		redQueen.setLocalScale((new Matrix4f()).scaling(1.0f));
		redQueen.getRenderStates().hasLighting(true);

		// Build Red Queen Piece
		/*
		queenRed = new GameObject(GameObject.root(), queenS, queentxRed);
		queenRed.setLocalTranslation((new Matrix4f()).translation(7.5f, 1.1f, -12.5f));
		queenRed.setLocalScale((new Matrix4f()).scaling(1.0f));
		queenRed.getRenderStates().hasLighting(true);
		*/
		
		blueQueen = new ChessPiece(2, "Queen", queenS, queentxBlue);
		opponentPieces[2] = blueQueen;
		blueQueen.setLocalTranslation((new Matrix4f()).translation(7.5f, 1.1f, 12.5f));
		blueQueen.setLocalScale((new Matrix4f()).scaling(1.0f));
		blueQueen.getRenderStates().hasLighting(true);
		
		// Build Blue Queen Piece
		/*
		queenBlue = new GameObject(GameObject.root(), queenS, queentxBlue);
		queenBlue.setLocalTranslation((new Matrix4f()).translation(7.5f, 1.1f, 12.5f));
		queenBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
		queenBlue.getRenderStates().hasLighting(true);
		*/
		
		redKnight = new ChessPiece(3, "Knight", knightS, knighttxRed);
		playerPieces[3] = redKnight;
		redKnight.setLocalTranslation((new Matrix4f()).translation(12.5f, 1.1f, -12.5f));
		redKnight.setLocalScale((new Matrix4f()).scaling(1.0f));
		redKnight.getRenderStates().hasLighting(true);

		// Build Red Knight Piece
		/*
		knightRed = new GameObject(GameObject.root(), knightS, knighttxRed);
		knightRed.setLocalTranslation((new Matrix4f()).translation(12.5f, 1.1f, -12.5f));
		knightRed.setLocalScale((new Matrix4f()).scaling(1.0f));
		knightRed.getRenderStates().hasLighting(true);
		*/
		
		blueKnight = new ChessPiece(3, "Knight", knightS, knighttxBlue);
		opponentPieces[3] = blueKnight;
		blueKnight.setLocalTranslation((new Matrix4f()).translation(12.5f, 1.1f, 12.5f));
		blueKnight.setLocalScale((new Matrix4f()).scaling(1.0f));
		blueKnight.getRenderStates().hasLighting(true);
		
		// Build Blue Knight Piece
		/*
		knightBlue = new GameObject(GameObject.root(), knightS, knighttxBlue);
		knightBlue.setLocalTranslation((new Matrix4f()).translation(12.5f, 1.1f, 12.5f));
		knightBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
		knightBlue.getRenderStates().hasLighting(true);
		*/
		
		redPawn = new ChessPiece(4, "Pawn", pawnS, pawntxRed);
		playerPieces[4] = redPawn;
		redPawn.setLocalTranslation((new Matrix4f()).translation(-12.5f, 0.6f, -12.5f));
		redPawn.setLocalScale((new Matrix4f()).scaling(1.0f));
		redPawn.getRenderStates().hasLighting(true);

		// Build Red Pawn Piece
		/*
		pawnRed = new GameObject(GameObject.root(), pawnS, pawntxRed);
		pawnRed.setLocalTranslation((new Matrix4f()).translation(-12.5f, 0.6f, -12.5f));
		pawnRed.setLocalScale((new Matrix4f()).scaling(1.0f));
		pawnRed.getRenderStates().hasLighting(true);
		*/
		
		bluePawn = new ChessPiece(4, "Pawn", pawnS, pawntxBlue);
		opponentPieces[4] = bluePawn;
		bluePawn.setLocalTranslation((new Matrix4f()).translation(-12.5f, 0.6f, 12.5f));
		bluePawn.setLocalScale((new Matrix4f()).scaling(1.0f));
		bluePawn.getRenderStates().hasLighting(true);
		
		// Build Blue Pawn Piece
		/*
		pawnBlue = new GameObject(GameObject.root(), pawnS, pawntxBlue);
		pawnBlue.setLocalTranslation((new Matrix4f()).translation(-12.5f, 0.6f, 12.5f));
		pawnBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
		pawnBlue.getRenderStates().hasLighting(true);
		*/
		
		redBishop = new ChessPiece(5, "Bishop", bishopS, bishoptxRed);
		playerPieces[5] = redBishop;
		redBishop.setLocalTranslation((new Matrix4f()).translation(-7.5f, 1.1f, -12.5f));
		redBishop.setLocalScale((new Matrix4f()).scaling(1.0f));
		redBishop.getRenderStates().hasLighting(true);

		// Build Red Bishop Piece
		/*
		bishopRed = new GameObject(GameObject.root(), bishopS, bishoptxRed);
		bishopRed.setLocalTranslation((new Matrix4f()).translation(-7.5f, 1.1f, -12.5f));
		bishopRed.setLocalScale((new Matrix4f()).scaling(1.0f));
		bishopRed.getRenderStates().hasLighting(true);
		*/
		
		blueBishop = new ChessPiece(5, "Bishop", bishopS, bishoptxBlue);
		opponentPieces[5] = blueBishop;
		blueBishop.setLocalTranslation((new Matrix4f()).translation(-7.55f, 1.1f, 12.5f));
		blueBishop.setLocalScale((new Matrix4f()).scaling(1.0f));
		blueBishop.getRenderStates().hasLighting(true);
		
		// Build Blue Bishop Piece
		/*
		bishopBlue = new GameObject(GameObject.root(), bishopS, bishoptxBlue);
		bishopBlue.setLocalTranslation((new Matrix4f()).translation(-7.55f, 1.1f, 12.5f));
		bishopBlue.setLocalScale((new Matrix4f()).scaling(1.0f));
		bishopBlue.getRenderStates().hasLighting(true);
		*/
		
		// Build ManualHome (now at edge of map)
    	home = new GameObject(GameObject.root(), homeS, bricktx);
    	Matrix4f homeTrans = (new Matrix4f()).translation(-50f, -5f, 50f);
    	home.setLocalTranslation(homeTrans); // Position it behind the origin
		Matrix4f homeRot = (new Matrix4f()).rotationY((float)Math.toRadians(135.0f));
		home.setLocalRotation(homeRot);// Rotate it to house the dolphin (open side facing forward)
    	Matrix4f homeScale = (new Matrix4f()).scaling(4.5f); // HOME SCALE
    	home.setLocalScale(homeScale); 
    	home.getRenderStates().hasLighting(true);

		// Build the Coordinate Axes so you can see X, Y, and Z
		x = new GameObject(GameObject.root(), linxS);
		x.getRenderStates().setColor(new Vector3f(1f, 0f, 0f));
		y = new GameObject(GameObject.root(), linyS);
		y.getRenderStates().setColor(new Vector3f(0f, 1f, 0f));
		z = new GameObject(GameObject.root(), linzS);
		z.getRenderStates().setColor(new Vector3f(0f, 0f, 1f));

		// 1. The custom Bobbing Controller (Applied to Home immediately)
		homeBobController = new BobbingController(engine, 1.0f, 0.1f);
		homeBobController.addTarget(home);
		homeBobController.enable();
		engine.getSceneGraph().addNodeController(homeBobController);

		// 2. Node Controllers from a2 - reuse as needed
		pyramidRotController = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		engine.getSceneGraph().addNodeController(pyramidRotController);
		// Bobbing controller
		pyramidBobController = new BobbingController(engine, 3.5f, 0.2f);
		engine.getSceneGraph().addNodeController(pyramidBobController);
		// Do NOT .enable() yet!

	}

	@Override
	public void initializeLights() {   
    	// Global Ambient: Providing a base level of visibility
    	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);

		// *** Lights leftover from A2.
		// *** REUSE AS NEEDED
    	// Light 1 (origin)
    	light1 = new Light();
    	light1.setDiffuse(2.0f, 2.0f, 2.0f);
    	light1.setLocation(new Vector3f(-3.0f, 5.0f, 3.0f));
		light1.setConstantAttenuation(1.0f);
		light1.setLinearAttenuation(0.05f);
		light1.setQuadraticAttenuation(0.005f); 
    	(engine.getSceneGraph()).addLight(light1);
    	// Light 2
    	light2 = new Light();
    	light2.setDiffuse(0.0f, 2.0f, 4.0f);
    	light2.setLocation(new Vector3f(19.0f, 5.0f, -21.0f));
		light2.setConstantAttenuation(1.0f);
		light2.setLinearAttenuation(0.05f);
		light2.setQuadraticAttenuation(0.005f);
    	(engine.getSceneGraph()).addLight(light2);
    	// Light 3
    	light3 = new Light();
    	light3.setDiffuse(2.0f, 0.0f, 2.0f); // Purple
    	light3.setLocation(new Vector3f(32.0f, 5.0f, 4.0f));
		light3.setConstantAttenuation(1.0f);
		light3.setLinearAttenuation(0.05f);
		light3.setQuadraticAttenuation(0.005f);
    	(engine.getSceneGraph()).addLight(light3);
    	// Light 4
    	light4 = new Light();
    	light4.setDiffuse(4.0f, 0.0f, 0.0f); // Pure Yellow (Full Red + Full Green)
    	light4.setLocation(new Vector3f(2.0f, 5.0f, -26.0f));
		light4.setConstantAttenuation(1.0f);
		light4.setLinearAttenuation(0.05f);
		light4.setQuadraticAttenuation(0.005f);
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
   	 	leftCamera.setLocation(new Vector3f(-2, 0, 2));
   		leftCamera.setU(new Vector3f(1, 0, 0));
   	 	leftCamera.setV(new Vector3f(0, 1, 0));
    	leftCamera.setN(new Vector3f(0, 0, -1));
		
    	// Overhead Camera setup - pointing straight down (Y-axis)
    	rightCamera.setLocation(new Vector3f(0, 35, 0)); // Start 35 units above to see whole map
    	rightCamera.setU(new Vector3f(1, 0, 0));
    	rightCamera.setV(new Vector3f(0, 0, -1));
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
		orbitController = new CameraOrbit3D(cam, avatar, gpName, engine);
		
		// Instantiate the actions
    	ZoomOverheadAction zoomOverhead = new ZoomOverheadAction();
    	PanOverheadAction panOverhead = new PanOverheadAction();
		FwdAction fwdAction = new FwdAction(this, protClient);
		TurnAction turnAction = new TurnAction(this);
		PitchAction pitchAction = new PitchAction(this);
		TakePhotoAction takePhotoAction = new TakePhotoAction(this);
		SpaceBarAction spaceAction = new SpaceBarAction(this);
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
			fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S,
			fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, 
    		turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, 
    		turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

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

	// ----- GETTERS for use in Action classes -----------
	public GameObject getAvatar() { return avatar; }
	public Engine getEngine() { return engine; }
	public RotationController getPyramidRotController() { return pyramidRotController;}
	public BobbingController getPyramidBobController() { return pyramidBobController; }
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

	@Override
	public void update()
	{	
		// Update the elapsed time (this is always first)
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		float deltaTime = (float) (currFrameTime - lastFrameTime);
    	if (!paused) elapsTime += deltaTime / 1000.0;

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
			return; 
    	}

		// GAME WIN LOGIC - stop updating game logic and show win message
		if (isGameWon) {

		// MAIN GAME LOGIC - updates only happen if the game isn't won or lost yet
		} else {
			Vector3f loc = avatar.getWorldLocation();

			// TERRAIN SNAP LOGIC
			// Get the actual height of the terrain at this specific (x, z)
    		float groundHeight = terr.getHeight(loc.x, loc.z);
			float dolphinOffset = 0.8f; // Offset to keep dolphin above ground.
			float adjustedHeight = groundHeight + dolphinOffset;
			avatar.setLocalLocation(new Vector3f(loc.x, loc.y + vertVel, loc.z));

            // JUMP LOGIC - Apply current vertical velocity to the avatar's position
            if (avatar.getWorldLocation().y > adjustedHeight) {
                // If in the air, gravity pulls the velocity down
                vertVel -= 0.0002f * deltaTime; 
            } else {
                // If we hit the ground, stop falling and snap to the floor
                vertVel = 0.0f;
                avatar.setLocalLocation(new Vector3f(loc.x, adjustedHeight, loc.z));
            }

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
        	Vector3f pos = dol.getWorldLocation();
        	String coordStr = String.format("Dolphin x: %.1f y: %.1f z: %.1f", pos.x(), pos.y(), pos.z());

			(engine.getHUDmanager()).setHUD2(coordStr, new Vector3f(1,1,0), hud2X, hud2Y);
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
			case KeyEvent.VK_2: dol.getRenderStates().setWireframe(true); break;
			case KeyEvent.VK_3: dol.getRenderStates().setWireframe(false);break;
			
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
			case KeyEvent.VK_0:
				if (id == 5) {id = 0;}
				else {id++;}
				avatar = playerPieces[id];
		}
		super.keyPressed(e);
	}
	
	
	private void setupNetworking()
	{	
		isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
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

