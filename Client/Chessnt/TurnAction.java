package Chessnt;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class TurnAction extends AbstractInputAction {
	private MyGame game;
	private ProtocolClient protClient;

	public TurnAction(MyGame g, ProtocolClient p) {
		game = g;
		protClient = p;
	}

	@Override
	public void performAction(float time, Event e) {
		float keyValue = e.getValue();
		String componentName = e.getComponent().getIdentifier().getName();
		
		// Check if the input is from a keyboard key
		boolean isKeyboard = e.getComponent().getIdentifier() instanceof net.java.games.input.Component.Identifier.Key;
		
		if (java.lang.Math.abs(keyValue) < 0.2f) return; // Standard deadzone

		// Determine direction and movement type (Discrete vs Continuous)
        float finalMove;
        if (isKeyboard) {
            // 2. Keyboard Logic (A and D keys)
            float direction = componentName.equalsIgnoreCase("D") ? 1.0f : -1.0f;
            finalMove = 5.0f * direction;
        } else {
            // 3. Analog Stick Logic (Continuous movement)
            float strafeSpeed = 2.0f;
			float stickValue = -keyValue; // inversion for strafing
            // Note: Invert keyValue here if your character moves the wrong way
            finalMove = strafeSpeed * stickValue * (time / 1000.0f);
        }

		if (!game.getIsGameDone()) {
            if (game.getChessM()) {
                Vector3f currentPhysLoc = game.getAvatar().getPhysicsObject().getLocation();
                float newX = currentPhysLoc.x() + finalMove;

                if (game.getRunning()) {
                    game.getAvatar().getPhysicsObject().setLocation(new float[] {
                        newX, 5f, currentPhysLoc.z() 
                    });
                } else {
                    game.getAvatar().getPhysicsObject().setLocation(new float[] {
                        newX, currentPhysLoc.y(), currentPhysLoc.z() 
                    });
                    // Sync visual avatar
                    game.getAvatar().setLocalTranslation(
                        (game.getAvatar().getWorldTranslation()).translate(finalMove, 0f, 0f)
                    );
                }
            } else {
                // Non-chess mode: Camera Yaw (rotation)
                Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
                cam.yaw(finalMove);
            }
			// protClient.sendMoveMessage(game.getAvatar().getWorldLocation(),
			// game.getPieceId());
		}
	}
}
