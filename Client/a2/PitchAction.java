package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class PitchAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;

    public PitchAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -.1 && keyValue < .1) return; // deadzone

        float rotAmount;
        String componentName = e.getComponent().getIdentifier().getName();

        if (e.getComponent().isAnalog()) {
            // Gamepad: Typically RY axis. 
            // Note: You may need to flip this sign depending on your controller preference
            rotAmount = keyValue * 0.02f; 
        } else {
            // Keyboard logic
            // If Up arrow is pressed, we want a negative rotation (pitch down/nose down)
            if (componentName.equalsIgnoreCase("Up")) {
                rotAmount = -0.02f; 
            } else if (componentName.equalsIgnoreCase("Down")) {
                rotAmount = 0.02f;
            } else {
                return;
            }
        }

        if (game.getIsRiding()) {
            game.getAvatar().pitch(rotAmount);
        } else {
            // Updated viewport name to "LEFT" to match your createViewports()
            Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
            cam.pitch(rotAmount);
        }
    }
}