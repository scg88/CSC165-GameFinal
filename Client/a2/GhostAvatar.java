package a2;

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

	public GhostAvatar(MyGame g, UUID id, ObjShape s, TextureImage t, Vector3f p) 
	{	
		super(GameObject.root(), s, t);
		uuid = id;
		game = g;
		setPosition(p, 0);
	}
	
	public UUID getID() { return uuid; }
	public void setPosition(Vector3f m, int id) { setLocalLocation(m); game.getOpponentPiece(id).setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }
}
