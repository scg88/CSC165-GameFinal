package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class TurnAction extends AbstractInputAction
{ 
    private MyGame game;
	private ProtocolClient protClient;
	
    public TurnAction(MyGame g, ProtocolClient p)
    { 
        game = g;
		protClient = p;
    }

    @Override
    public void performAction(float time, Event e)
    { 
        float keyValue = e.getValue();
        if (keyValue > -.05 && keyValue < .05) return; // deadzone

        float rotSpeed = 4.0f;
    
        String componentName = e.getComponent().getIdentifier().getName();

        if (componentName.equalsIgnoreCase("A")) {
            keyValue = 1.0f;  // Turn Left
        } else if (componentName.equalsIgnoreCase("D")) {
            keyValue = -1.0f; // Turn Right
        } else if (e.getComponent().isAnalog()) {
            keyValue = -keyValue; 
        }
        
        float rotAmount = rotSpeed * keyValue * (time / 1000.0f);

        if(game.getChessM())
		{
			game.getAvatar().setLocalTranslation((game.getAvatar().getWorldTranslation()).translate(5f*keyValue, 0f, 0f));
		}
        else {
            Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
            cam.yaw(rotAmount);
        }
		//protClient.sendMoveMessage(game.getAvatar().getWorldLocation(), game.getPieceId());
    } 
}

