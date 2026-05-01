package a2;

import tage.*;
import tage.shapes.AnimatedShape;

import org.joml.*;

public class ChessPiece extends GameObject
{
	int pieceId;
	String pieceType;
	private AnimatedShape animatedShape; // Store the animation reference
	
	public ChessPiece(int id, String type, ObjShape s, TextureImage t)
	{
		super(GameObject.root(), s, t);
		pieceId = id;
		pieceType = type;

		// If the shape passed in is actually an AnimatedShape, store it
        if (s instanceof AnimatedShape) {
            animatedShape = (AnimatedShape) s;
        }
	}
	
	public ChessPiece(int id, String type, ObjShape s, TextureImage t, Vector3f p)
	{
		this(id, type, s, t); // Calls the constructor above to avoid repeating code
        setPosition(p);
	}
	
	// Helper method to trigger animations easily
    public void playAction(String name, float speed, AnimatedShape.EndType endType) {
        if (animatedShape != null) {
            animatedShape.stopAnimation();
            animatedShape.playAnimation(name, speed, endType, 0);
        }
    }

	// Helper method to stop animations
    public void stopAction() {
        if (animatedShape != null) {
            animatedShape.stopAnimation();
        }
    }

	public int getID() { return pieceId; }
	public String getType() { return pieceType; }
	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }

	// Check if this piece is even capable of animating
    public boolean isAnimated() { return animatedShape != null; }
}