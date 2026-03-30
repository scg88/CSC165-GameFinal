package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class TakePhotoAction extends AbstractInputAction {
    private MyGame game;

    public TakePhotoAction(MyGame g) { game = g; }

    @Override
    public void performAction(float time, Event e) {
        GameObject dol = game.getAvatar();

        // Reset message to a default before checking
        game.setHUDMessage("Searching for targets...");

        // Check distance and take photo if one doesn't already exist.
        checkAndTakePhoto(dol, game.getPlanet1(), game.getTex1(), game.hasPhoto1(), 1);
        checkAndTakePhoto(dol, game.getPlanet2(), game.getTex2(), game.hasPhoto2(), 2);
        checkAndTakePhoto(dol, game.getPlanet3(), game.getTex3(), game.hasPhoto3(), 3);
    }

    private void checkAndTakePhoto(GameObject dol, GameObject planet, TextureImage tex, boolean alreadyTaken, int planetNum) {
        if (alreadyTaken) return;

        float distance = dol.getWorldLocation().distance(planet.getWorldLocation());
        float photoThreshold = 7.5f;

        // If close enough (7.5 units) take photo
        if (distance < photoThreshold) {
            GameObject photo = new GameObject(GameObject.root(), game.getRectS(), tex);
            photo.setParent(dol);

            // Apply Bobbing Node Controller based on planet number
            if (planetNum == 1) {
                // Apply Bobbing to the 1st Pyramid
                game.getPyramidBobController().addTarget(planet);
                game.getPyramidBobController().enable();
            } else {
                // Apply Rotation to Pyramids 2 and 3
                game.getPyramidRotController().addTarget(planet);
                game.getPyramidRotController().enable();
            }

            // Positioning logic based on planet number
            Vector3f localPos;
            if (planetNum == 1) { 
                localPos = new Vector3f(-0.5f, 0.0f, -0.5f); 
                game.setPhoto1(true); 
                game.setPhotoObj1(photo); // SAVE THE OBJECT
            }
            else if (planetNum == 2) { 
                localPos = new Vector3f(0.5f, 0.0f, -0.5f);  
                game.setPhoto2(true); 
                game.setPhotoObj2(photo); // SAVE THE OBJECT
            }
            else { 
                localPos = new Vector3f(0.0f, 0.0f, 0.5f);   
                game.setPhoto3(true); 
                game.setPhotoObj3(photo); // SAVE THE OBJECT
            }

            photo.setLocalLocation(localPos);
            photo.setLocalRotation((new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f)));
            photo.setLocalScale((new Matrix4f()).scaling(0.1f));

            game.setHUDMessage("PICTURE TAKEN!"); // UPDATED MESSAGE
            System.out.println("Photo taken and attached for planet " + planetNum);
        }
        else if (distance < 15.0f) { 
            // Only show this if the player is somewhat near a target
            game.setHUDMessage("NOT CLOSE ENOUGH!"); 
        }
    }
}