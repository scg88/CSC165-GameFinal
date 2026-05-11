package Chessnt;

import java.util.UUID;
import tage.*;
import tage.*;
import org.joml.*;
import tage.shapes.*;

// A ghost MUST be connected as a child of the root,
// so that it will be rendered, and for future removal.
// The ObjShape and TextureImage associated with the ghost
// must have already been created during loadShapes() and
// loadTextures(), before the game loop is started.

public class GhostAvatar extends GameObject
{
	UUID uuid;
	private MyGame game;
	private int currentPieceID = 0;

	public GhostAvatar(MyGame g, UUID id, ObjShape s, TextureImage t, Vector3f p) 
	{	
		super(GameObject.root(), s, t);
		uuid = id;
		game = g;
		setPosition(p, 0);
		getRenderStates().disableRendering();
	}
	
	public UUID getID() { return uuid; }
	public void setPosition(Vector3f oldM, int id) 
	{ 
		this.currentPieceID = id;
		Vector3f newM = game.getBoard().moveEnemyPiece(game.getOpponentPiece(id), oldM);
		setLocalLocation(newM);
		game.getOpponentPiece(id).getPhysicsObject().setLocation((new float[] {newM.x(), newM.y()+5f, newM.z()})); 
		game.setRunning(true);
		//System.out.println("Get rotated dumbass");
		//game.toggleTurn();
		//System.out.println("Value is: " + game.getTurn());
	}
	public Vector3f getPosition() { return getWorldLocation(); }
	public int getCurrentPieceID() { return currentPieceID; }
}
