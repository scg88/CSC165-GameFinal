package Chessnt;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

public class ChessMoveAction extends AbstractInputAction {
    private MyGame game;

    public ChessMoveAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // --- TEMPORARY DEBUG ---
        if (e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.POV) {
            System.out.println("POV Event! Raw Value: " + e.getValue());
        }
        
        float val = e.getValue();
        boolean isDPad = (e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.POV);
        
        float moveX = 0f;
        float moveZ = 0f;

        if (isDPad) {
            // Check all 4 directions in one place
            if (val > 0.15f && val < 0.35f)      moveZ = 5.0f;  // Up
            else if (val > 0.65f && val < 0.85f) moveZ = -5.0f; // Down
            else if (val > 0.45f && val < 0.55f) moveX = -5.0f; // Left
            else if (val > 0.95f && val <= 1.0f) moveX = 5.0f;  // Right
            else return; 
        }

        // Apply movement to Physics and Visuals
        if (moveX != 0 || moveZ != 0) {
            GameObject av = game.getAvatar();
            Vector3f currentLoc = av.getPhysicsObject().getLocation();
    
            float newX = currentLoc.x() + moveX;
            //float newY = currentLoc.y(); // Keep current height
            float newZ = currentLoc.z() + moveZ;
            
            // Set Physics
            av.getPhysicsObject().setLocation(new float[]{newX, 0, newZ});
            
            // Set Visuals (Sync)
            av.setLocalTranslation(av.getWorldTranslation().translate(moveX, 0, moveZ));
            
            System.out.println("D-Pad Moved to: " + newX + ", " + newZ);
        }
    }
}