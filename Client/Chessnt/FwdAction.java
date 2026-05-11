package Chessnt;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class FwdAction extends AbstractInputAction
{ 
    private MyGame game;
    private Vector3f oldPosition, newPosition;
    private Vector4f fwdDirection;
	private ProtocolClient protClient;

    public FwdAction(MyGame g, ProtocolClient p)
    { 
		game = g;
		protClient = p;
    }

    @Override
    public void performAction(float time, Event e)
    {
        float keyValue = e.getValue();
        String componentName = e.getComponent().getIdentifier().getName();
		
		// Check if the input is from a keyboard key
		boolean isKeyboard = e.getComponent().getIdentifier() instanceof net.java.games.input.Component.Identifier.Key;
		
		// 1. Deadzone check: Only apply to sticks, not keyboard
        if (!isKeyboard && java.lang.Math.abs(keyValue) < 0.2f) return;
		
		// Determine direction and movement type
    	float finalMove;
        if (isKeyboard) {
            // Keyboard Logic: S is back, everything else (W) is forward
            float direction = componentName.equalsIgnoreCase("S") ? -1.0f : 1.0f;
            finalMove = 5.0f * direction;
        } else {
            // Analog Stick Logic (Continuous movement)
            float speed = 2.0f;
            // Note: keyValue usually needs to be negated for forward/back sticks
            float stickValue = -keyValue; 
            finalMove = speed * stickValue * (time / 1000.0f);
        }

		if (!game.getIsGameDone()) {
        	if (game.getChessM()) {
            	Vector3f currentPhysLoc = game.getAvatar().getPhysicsObject().getLocation();
            
            	// Use finalMove instead of moveAmount or 5f
            	float newZ = currentPhysLoc.z() + finalMove;

            	if (game.getRunning()) {
                	game.getAvatar().getPhysicsObject().setLocation(new float[]{currentPhysLoc.x(), 5f, newZ});
            	} else {
                	game.getAvatar().getPhysicsObject().setLocation(new float[]{currentPhysLoc.x(), currentPhysLoc.y(), newZ});
                	game.getAvatar().setLocalTranslation(game.getAvatar().getWorldTranslation().translate(0f, 0f, finalMove));
            	}
        	} else {
            	// Standard camera movement
            	Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
            	Vector3f fwd = new Vector3f(cam.getN()).mul(finalMove);
            	cam.setLocation(cam.getLocation().add(fwd));
        	}

			//protClient.sendMoveMessage(game.getAvatar().getWorldLocation(), game.getPieceId());
		}
    } 
}
