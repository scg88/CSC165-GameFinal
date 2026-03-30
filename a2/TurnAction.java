package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class TurnAction extends AbstractInputAction
{ 
    private MyGame game;

    public TurnAction(MyGame g)
    { 
        game = g;
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

        if (game.getIsRiding()) {
            // Rotate the dolphin based on input
            game.getAvatar().globalYaw(rotAmount);
        } else {
            Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
            cam.yaw(rotAmount);
            /* 
            float angle = keyValue * -0.02f; // Adjust sensitivity
            Vector3f up = cam.getV();
            Vector3f newU = new Vector3f(cam.getU()).rotateAxis(angle, up.x, up.y, up.z);
            Vector3f newN = new Vector3f(cam.getN()).rotateAxis(angle, up.x, up.y, up.z);
            cam.setU(newU);
            cam.setN(newN);
            */
        }
        
    } 
}

