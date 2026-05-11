package Chessnt;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class JumpAction extends AbstractInputAction {
    private MyGame game;

    public JumpAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        if (e.getValue() < 0.5f) return;

        GameObject avatar = game.getAvatar();
        float currentY = avatar.getWorldLocation().y();

        // Only trigger when button is pressed (value = 1.0)
        // And only if the dolphin is near the ground (y <= 0.81f)
        if (currentY <= 0.82f && game.getVertVel() <= 0.0f) {
            
            // JUMP LOGIC - This sets the initial "upward push"
            // The update() method in MyGame handles the actual movement and gravity
            game.setVertVel(0.1f);
            
            game.setHUDMessage("Might as well Jump!");
        }
    }
}