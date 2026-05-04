package a2;

import tage.*;
import org.joml.*;

public class ChessPiece extends GameObject
{
	int pieceId;
	String pieceType;
	String CNpos;
	
	public ChessPiece(int id, String type, String startPos, ObjShape s, TextureImage t)
	{
		super(GameObject.root(), s, t);
		pieceId = id;
		pieceType = type;
		CNpos = startPos;
	}
	
	public ChessPiece(int id, String type, ObjShape s, TextureImage t, Vector3f p)
	{
		super(GameObject.root(), s, t);
		pieceId = id;
		pieceType = type;
		setPosition(p);
	}
	
	public int getID() { return pieceId; }
	public String getType() { return pieceType; }
	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }
	
	public String getCNPos(){return CNpos;}
	public void setCNPos(String newPos){CNpos = newPos;}
}