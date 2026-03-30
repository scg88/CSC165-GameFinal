package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class SpaceBarAction extends AbstractInputAction {
    private MyGame game;

    public SpaceBarAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        // Only trigger on key press
        if (e.getValue() < 0.5f) return;

        GameObject dol = game.getAvatar();
        GameObject home = game.getHome();

        // 1. Check if the dolphin is near the home
        if (dol.getWorldLocation().distance(home.getWorldLocation()) < 15.0f) {
            
            Matrix4f photoScale = (new Matrix4f()).scaling(0.75f);
            Matrix4f standUp = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));

            // 2. Process each photo INDEPENDENTLY with Null Checks
            // Photo 1
            if (game.hasPhoto1() && game.getPhotoObj1() != null && game.getPhotoObj1().getParent() != home) {
                GameObject p1 = game.getPhotoObj1();
                p1.setParent(home);
                p1.setLocalLocation(new Vector3f(9.0f, 11f, -0.2f));
                p1.setLocalScale(photoScale);
                Matrix4f turn45 = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(45.0f));
                p1.setLocalRotation(turn45.mul(standUp));
            }

            // Photo 2
            if (game.hasPhoto2() && game.getPhotoObj2() != null && game.getPhotoObj2().getParent() != home) {
                GameObject p2 = game.getPhotoObj2();
                p2.setParent(home);
                p2.setLocalLocation(new Vector3f(3.0f, 14.0f, -3.0f));
                p2.setLocalScale(photoScale);
                p2.setLocalRotation(standUp);
            }

            // Photo 3
            if (game.hasPhoto3() && game.getPhotoObj3() != null && game.getPhotoObj3().getParent() != home) {
                GameObject p3 = game.getPhotoObj3();
                p3.setParent(home);
                p3.setLocalLocation(new Vector3f(0.2f, 11f, -9.0f));
                p3.setLocalScale(photoScale);
                Matrix4f turnNeg45 = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(-45.0f));
                p3.setLocalRotation(turnNeg45.mul(standUp));
            }

            // 3. Victory Check with Null-Safe logic
            boolean p1AtHome = (game.getPhotoObj1() != null && game.getPhotoObj1().getParent() == home);
            boolean p2AtHome = (game.getPhotoObj2() != null && game.getPhotoObj2().getParent() == home);
            boolean p3AtHome = (game.getPhotoObj3() != null && game.getPhotoObj3().getParent() == home);

            if (p1AtHome && p2AtHome && p3AtHome) {
                game.setIsGameWon(true);
                game.setHUDMessage("You Beat Dolphin Astronaut!");

                Camera cam = game.getEngine().getRenderSystem().getViewport("LEFT").getCamera();
                Vector3f homeLoc = home.getWorldLocation();
                cam.setLocation(new Vector3f(homeLoc).add(-8.0f, 4.0f, 8f)); 
                cam.lookAt(new Vector3f(homeLoc).add(0.0f, 6.0f, 0.0f));
            } else {
                game.setHUDMessage("Photo(s) attached to home! Go find the rest.");
            }

        } else {
            // Not near home - update HUD with current status
            int count = 0;
            if (game.hasPhoto1()) count++;
            if (game.hasPhoto2()) count++;
            if (game.hasPhoto3()) count++;
            game.setHUDMessage("Carrying " + count + " photos. Get to Home to attach them!");
        }
    }
}