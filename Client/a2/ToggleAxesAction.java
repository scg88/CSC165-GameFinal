package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ToggleAxesAction extends AbstractInputAction {
    private MyGame game;

    public ToggleAxesAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // Only trigger on the initial press (value 1.0), not while holding it
        if (e.getValue() < 1.0f) return;

        // 1. Flip the boolean value in MyGame
        boolean isVisible = !game.getAxesVisible();
        game.setAxesVisible(isVisible);

        // 2. Determine the new Y position
        float yPos = isVisible ? 0.01f : -100.0f;

        // 3. Update the locations of the axis objects
        game.getXAxis().setLocalLocation(new Vector3f(0f, yPos, 0f));
        game.getYAxis().setLocalLocation(new Vector3f(0f, yPos, 0f));
        game.getZAxis().setLocalLocation(new Vector3f(0f, yPos, 0f));

        // 4. Update the HUD
        game.setHUDMessage("Axes " + (isVisible ? "Enabled" : "Hidden"));
    }
}