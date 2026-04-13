package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

// REMOVED FOR FINAL GAME - used for taking photos from A2 assignment.

public class TakePhotoAction extends AbstractInputAction {
    private MyGame game;

    public TakePhotoAction(MyGame g) { game = g; }

    @Override
    public void performAction(float time, Event e) {
        // Neutralized for Final Project cleanup
        // We can possibly use this later for a "Select Piece" or "Capture" mechanic
        game.setHUDMessage("Action Button Pressed - No target assigned.");
    }
}