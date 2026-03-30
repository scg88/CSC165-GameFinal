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

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;

	private boolean paused=false;
	private boolean isRiding = true; // Start the game on the dolphin
	
	private String hudMessage = "Press the 'B button' to take a photo, and the 'Y Button' to jump!";

	private boolean isGameOver = false; // Game over condition
	private boolean isGameWon = false;  // Game win condition (if you want to implement a win state)
	private boolean axesVisible = true;

	private boolean photoTaken1 = false; // Track which photos have been taken
	private boolean photoTaken2 = false;
	private boolean photoTaken3 = false;

	private double lastFrameTime, currFrameTime, elapsTime;

	private InputManager im;
	private GameObject dol, tor, avatar, x, y, z, home;
	private GameObject pObj1, pObj2, pObj3, ground;
	private GameObject pyramid1, pyramid2, pyramid3;
	private ObjShape dolS, torS, planetS, linxS, linyS, linzS, homeS, rectS;
	private ObjShape groundPlaneS, pyramidS;
	private TextureImage doltx, planettx, planettx2, planettx3, bricktx, grasstx;
	private Light light1, light2, light3, light4;
	
	private CameraOrbit3D orbitController;

	private RotationController pyramidRotController;
	private BobbingController pyramidBobController;
	private BobbingController homeBobController;

	private float vertVel = 0.0f;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();
	}

	@Override
	public void loadShapes()
	{	
		dolS = new ImportedModel("dolphinHighPoly.obj");
		planetS = new Sphere();     // Use a simple sphere shape for planets
		homeS = new ManualHome();   // Custom shape for the home base
		groundPlaneS = new Plane(); // Ground Plane
		rectS = new Plane();        // Plane for photo mechanic
		pyramidS = new ManualPyramid(); // Custom pyramid shape

		torS = new Torus(0.5f, 0.2f, 48); // Shapes for coordinate axes and torus
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	
		doltx = new TextureImage("Dolphin_HighPolyUV.jpg");
		planettx = new TextureImage("Texturelabs_Stone_153M.jpg");  // Generic Stone texture for the first planet (sourced from TextureLabs free textures)
		planettx2 = new TextureImage("PlanetText_05.jpg");          // Purple planet texture
		planettx3 = new TextureImage("PlanetText_04.jpg");          // Red planet texture
		grasstx = new TextureImage("mud_cracked_dry_03.jpg");       // Desert texture sourced from Polyhaven
		bricktx = new TextureImage("brick1.jpg");
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialScale, initialRotation;
		
		// 45 deg rotation
		float angle = (float) Math.toRadians(45.0); 

		// BUILD DOLPHIN in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,0.8f,0f);
		avatar.setLocalTranslation(initialTranslation); // Start at the origin (0,0,0)
		// ROTATE DOLPHIN
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f)); // Rotate 135 to bisect the X and Z axes
		avatar.setLocalRotation(initialRotation);
		// SCALE DOLPHIN
		initialScale = (new Matrix4f()).scaling(3.0f); 
		avatar.setLocalScale(initialScale);
		dol = avatar; // Alias from previous examples

		// Build ManualHome at the origin
    	home = new GameObject(GameObject.root(), homeS, bricktx);
    	Matrix4f homeTrans = (new Matrix4f()).translation(-3f, -5f, 3f);
    	home.setLocalTranslation(homeTrans); // Position it behind the origin
		Matrix4f homeRot = (new Matrix4f()).rotationY((float)Math.toRadians(135.0f));
		home.setLocalRotation(homeRot);// Rotate it to house the dolphin (open side facing forward)
    	Matrix4f homeScale = (new Matrix4f()).scaling(4.5f); // HOME SCALE
    	home.setLocalScale(homeScale); 
    	home.getRenderStates().hasLighting(true);
	
		// Build the Ground Plane
		ground = new GameObject(GameObject.root(), groundPlaneS, grasstx);
		ground.setLocalLocation(new Vector3f(0, 0, 0));
		ground.setLocalScale((new Matrix4f()).scaling(100.0f, 1.0f, 100.0f));
		ground.getRenderStates().setTiling(1); // Set tiling to repeat texture (less blurry)
		ground.getRenderStates().setTileFactor(20);

		// Metalic pyramid
		pyramid1 = new GameObject(GameObject.root(), pyramidS, planettx);
		pyramid1.setLocalLocation(new Vector3f(20f, 1.0f, -20f));
		pyramid1.setLocalScale((new Matrix4f()).scaling(5.0f)); 

		// Purple pyramid
		pyramid2 = new GameObject(GameObject.root(), pyramidS, planettx2);
		pyramid2.setLocalLocation(new Vector3f(30f, 1.0f, 0f));
		pyramid2.setLocalScale((new Matrix4f()).scaling(3.5f));
		pyramid2.setLocalRotation((new Matrix4f()).rotationY(angle));
		
		// Red pyramid
		pyramid3 = new GameObject(GameObject.root(), pyramidS, planettx3);
		pyramid3.setLocalLocation(new Vector3f(0f, 1.0f, -25f));
		pyramid3.setLocalScale((new Matrix4f()).scaling(3.5f));
		pyramid3.setLocalRotation((new Matrix4f()).rotationY(angle));

		// Build the Coordinate Axes so you can see X, Y, and Z
		x = new GameObject(GameObject.root(), linxS);
		x.getRenderStates().setColor(new Vector3f(1f, 0f, 0f));
		y = new GameObject(GameObject.root(), linyS);
		y.getRenderStates().setColor(new Vector3f(0f, 1f, 0f));
		z = new GameObject(GameObject.root(), linzS);
		z.getRenderStates().setColor(new Vector3f(0f, 0f, 1f));

		// Build the Torus
		tor = new GameObject(GameObject.root(), torS);
		tor.getRenderStates().hasLighting(false); // Disable lighting to make the torus stand out
		tor.getRenderStates().setColor(new Vector3f(0.0f, 1.0f, 1.0f));
		initialTranslation = (new Matrix4f()).translation(4, 0, 0);
		tor.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(0.25f);
		tor.setLocalScale(initialScale);

		// 1. The custom Bobbing Controller (Applied to Home immediately)
		homeBobController = new BobbingController(engine, 1.0f, 0.1f);
		homeBobController.addTarget(home);
		homeBobController.enable();
		engine.getSceneGraph().addNodeController(homeBobController);

		// 2. Node Controller for Pyramids
		pyramidRotController = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		engine.getSceneGraph().addNodeController(pyramidRotController);
		pyramidBobController = new BobbingController(engine, 3.5f, 0.2f);
		engine.getSceneGraph().addNodeController(pyramidBobController);
		// Do NOT .enable() yet!

	}

	@Override
	public void initializeLights() {   
    	// Global Ambient: Providing a base level of visibility
    	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);

    	// Light 1: Over Home (Origin) - Kept this as your base/starting light
    	light1 = new Light();
    	light1.setDiffuse(2.0f, 2.0f, 2.0f);
    	light1.setLocation(new Vector3f(-3.0f, 5.0f, 3.0f));
		light1.setConstantAttenuation(1.0f);
		light1.setLinearAttenuation(0.05f);
		light1.setQuadraticAttenuation(0.005f); 
    	(engine.getSceneGraph()).addLight(light1);

    	// Light 2: Over Metalic Pyramid (Pyramid 1)
    	// Location matches pyramid1: (15, 1, -15)
    	light2 = new Light();
    	light2.setDiffuse(0.0f, 2.0f, 4.0f);
    	light2.setLocation(new Vector3f(19.0f, 5.0f, -21.0f));
		light2.setConstantAttenuation(1.0f);
		light2.setLinearAttenuation(0.05f);
		light2.setQuadraticAttenuation(0.005f);
    	(engine.getSceneGraph()).addLight(light2);

    	// Light 3: Over Purple Pyramid (Pyramid 2)
    	// Location matches pyramid2: (30, 1, 0)
    	light3 = new Light();
    	light3.setDiffuse(2.0f, 0.0f, 2.0f); // Purple
    	light3.setLocation(new Vector3f(32.0f, 5.0f, 4.0f));
		light3.setConstantAttenuation(1.0f);
		light3.setLinearAttenuation(0.05f);
		light3.setQuadraticAttenuation(0.005f);
    	(engine.getSceneGraph()).addLight(light3);

    	// Light 4: Over Red Pyramid
    	// Location matches pyramid3: (0, 1, -25) 
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
    	rightCamera.setLocation(new Vector3f(10, 35, -10)); // Start 35 units above to see whole map
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

		// --------------INPUTS & CAMERA SECTION----------------------
		im = engine.getInputManager();
		String gpName = im.getFirstGamepadName();
		Camera cam = engine.getRenderSystem().getViewport("LEFT").getCamera();
		
		// ---A2 REQUIREMENT: Create the CameraOrbit3D controller and associate it with the avatar and camera ---
		orbitController = new CameraOrbit3D(cam, avatar, gpName, engine);
		
		// Instantiate the actions
    	ZoomOverheadAction zoomOverhead = new ZoomOverheadAction();
    	PanOverheadAction panOverhead = new PanOverheadAction();
		FwdAction fwdAction = new FwdAction(this);
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

	public GameObject getPlanet1() { return pyramid1; }
    public GameObject getPlanet2() { return pyramid2; }
    public GameObject getPlanet3() { return pyramid3; }
	public TextureImage getTex1() { return planettx; }
    public TextureImage getTex2() { return planettx2; }
    public TextureImage getTex3() { return planettx3; }

	public ObjShape getRectS() { return rectS; }
	public boolean hasPhoto1() { return photoTaken1; }
	public boolean hasPhoto2() { return photoTaken2; }
	public boolean hasPhoto3() { return photoTaken3; }
	public void setPhoto1(boolean b) { photoTaken1 = b; }
	public void setPhoto2(boolean b) { photoTaken2 = b; }
	public void setPhoto3(boolean b) { photoTaken3 = b; }
	public void setPhotoObj1(GameObject g) { pObj1 = g; }
	public void setPhotoObj2(GameObject g) { pObj2 = g; }
	public void setPhotoObj3(GameObject g) { pObj3 = g; }
	public GameObject getPhotoObj1() { return pObj1; }
	public GameObject getPhotoObj2() { return pObj2; }
	public GameObject getPhotoObj3() { return pObj3; }
	public GameObject getHome() { return home; }
	public void setIsGameWon(boolean b) { isGameWon = b; }
	public void setVertVel(float v) { vertVel = v; }

	public boolean getAxesVisible() { return axesVisible; }
	public void setAxesVisible(boolean b) { axesVisible = b; }
	public GameObject getXAxis() { return x; }
	public GameObject getYAxis() { return y; }
	public GameObject getZAxis() { return z; }

	@Override
	public void update()
	{	
		// Update the elapsed time (this is always first)
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		float deltaTime = (float) (currFrameTime - lastFrameTime);
    	if (!paused) elapsTime += (currFrameTime - lastFrameTime) / 1000.0;

		// Update the input manager to process any new input events
		im = engine.getInputManager();
    	im.update(deltaTime);

		// ---A2 REQUIREMENT: Update the Orbit Camera ---
    	// This must happen every frame so the camera follows the avatar
    	orbitController.updateCameraPosition();

		// GAME OVER LOGIC - stop updating game logic
    	if (isGameOver) {
        	(engine.getHUDmanager()).setHUD1("YOU CRASHED! GAME OVER", new Vector3f(1,0,0), 15, 15);
        	(engine.getHUDmanager()).setHUD2("Press ESC to exit", new Vector3f(1,1,1), 15, 40);
        	
			im = engine.getInputManager();
    		im.update(deltaTime);
			return; 
    	}

		// GAME WIN LOGIC - stop updating game logic and show win message
		if (isGameWon) {
    		(engine.getHUDmanager()).setHUD1("MISSION COMPLETE!", new Vector3f(0,1,0), 15, 15);
    		(engine.getHUDmanager()).setHUD2("Free Roam Mode: Use WASD/Arrows to view photos", new Vector3f(1,1,1), 15, 40);
    		//return;

		// MAIN GAME LOGIC - updates only happen if the game isn't won or lost yet
		} else { 
			Vector3f loc = avatar.getWorldLocation();
            
            // JUMP LOGIC - Apply current vertical velocity to the avatar's position
            avatar.setLocalLocation(new Vector3f(loc.x, loc.y + vertVel, loc.z));
            if (avatar.getWorldLocation().y > 0.8f) {
                // If in the air, gravity pulls the velocity down
                vertVel -= 0.0002f * deltaTime; 
            } else {
                // If we hit the ground, stop falling and snap to the floor
                vertVel = 0.0f;
                avatar.setLocalLocation(new Vector3f(loc.x, 0.8f, loc.z));
            }

			// COLLISION DETECTION
    		checkCrash(pyramid1, 3.0f);  // Adjust based on planet size
    		checkCrash(pyramid2, 2.0f);
    		checkCrash(pyramid3, 5.0f);

			// DATA CALCULATION FOR HUD
			int elapsTimeSec = Math.round((float)elapsTime);
			int score = (photoTaken1 ? 1 : 0) + (photoTaken2 ? 1 : 0) + (photoTaken3 ? 1 : 0);
			
			// Get current window dimensions for relative positioning
			int windowWidth = engine.getRenderSystem().getWidth();
			int windowHeight = engine.getRenderSystem().getHeight();

			// TASK COMPLETE LOGIC - all 3 pictures are taken, promt the user
			if (score == 3 && !isGameWon) {
    			hudMessage = "Return Home and press 'SPACE' to complete the game!";
			}

			// --- HUD 1: MAIN VIEWPORT (Bottom Left) ---
			String mainDisp = "Score: " + score + " | Time: " + elapsTimeSec + " | " + hudMessage;
        	(engine.getHUDmanager()).setHUD1(mainDisp, new Vector3f(1,1,1), 15, 15);
			
			// --- HUD 2: OVERHEAD VIEWPORT (Relative to Right Window) ---
        	Vector3f pos = dol.getWorldLocation();
        	String coordStr = String.format("Dolphin x: %.1f y: %.1f z: %.1f", pos.x(), pos.y(), pos.z());

			// Calculate X to be at the start of the RIGHT viewport (75% across)
        	int hud2X = (int)(windowWidth * 0.76f);
        	int hud2Y = 15; // Bottom of the overhead viewport

			(engine.getHUDmanager()).setHUD2(coordStr, new Vector3f(1,1,0), hud2X, hud2Y);
		}
	}

	// Helper method for crashing
	private void checkCrash(GameObject p, float threshold) {
    	if (dol.getWorldLocation().distance(p.getWorldLocation()) < threshold) {
        isGameOver = true;
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
		}
		super.keyPressed(e);
	}
}

