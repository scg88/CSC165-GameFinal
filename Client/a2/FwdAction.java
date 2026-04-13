package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class FwdAction extends AbstractInputAction
{ 
    private MyGame game;
    private GameObject av;
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
        if (keyValue > -.05 && keyValue < .05) return; // deadzone

        String componentName = e.getComponent().getIdentifier().getName();

        // INPUT LOGIC 
        if (componentName.equalsIgnoreCase("S")) {
            keyValue = -1.0f; // Force S key to move backwards
        } else if (e.getComponent().isAnalog()) {
            keyValue = -keyValue; // Invert stick if 'Forward' was moving you 'Backward'
        }
        
        // Time-based movement scaling for consistent speed across different frame rates
        float speed = 15.0f;
        float moveAmount = speed * keyValue * (time / 1000.0f); // scale the movement amount

        if (game.getIsRiding()) {
            av = game.getAvatar();
            Vector3f fwd = av.getWorldForwardVector();
            // Calculate new position using JOML destination parameter to avoid mutation bugs
            Vector3f oldPos = av.getWorldLocation();
            newPosition = oldPos.add(fwd.mul(moveAmount, new Vector3f()), new Vector3f());
            // Ground check
            if (newPosition.y < 0.8f) newPosition.y = 0.8f;
            av.setLocalLocation(newPosition);
        } else {
            // 1. Access the TAGE camera
            Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
            // 2. Store the camera's current position
            Vector3f oldCamPos = cam.getLocation();
            // 3. Get the camera's forward direction (N vector)
            Vector3f fwd = new Vector3f(cam.getN());
            // 4. Scale the forward vector by the movement amount
            fwd.mul(moveAmount);
            // 5. Add the scaled forward vector to the camera's current position to get the new position
            newPosition = oldCamPos.add(fwd);
            // 6. Set the camera's position to the new position
            cam.setLocation(newPosition);
        }

		protClient.sendMoveMessage(av.getWorldLocation());
    } 
}
