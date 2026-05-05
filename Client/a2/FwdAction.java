package a2;

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

		if(game.getChessM())
		{
			game.getAvatar().setLocalTranslation((game.getAvatar().getWorldTranslation()).translate(0f, 0f, 5f*keyValue));
		}
		else {
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

		//protClient.sendMoveMessage(game.getAvatar().getWorldLocation(), game.getPieceId());
    } 
}
