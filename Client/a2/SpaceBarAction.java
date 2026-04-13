package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

// REMOVED FOR FINAL GAME - used for win condition after taking photos from A2 assignment.

public class SpaceBarAction extends AbstractInputAction {
    private MyGame game;

    public SpaceBarAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // Only trigger on key press (value > 0.5)
        if (e.getValue() < 0.5f) return;

        // Neutralized for Final Project cleanup
        game.setHUDMessage("Space Bar Pressed - Ready for Chess logic!");
    }
}