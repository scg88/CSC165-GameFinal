package tage;

import tage.*;
import tage.Camera;
import tage.Engine;
import tage.GameObject;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math;

// FROM code04b_OrbitController.pdf (presented in class)

/**
* A BobbingController is a node controller that, when enabled, causes any object
* it is attached to to bob up and down in place at a specified height.
*/

public class CameraOrbit3D {
    private Engine engine;
    private Camera camera;
    private GameObject avatar;     // the target object the camera looks at
    private float cameraAzimuth;   // rotation around target Y axis (degrees)
    private float cameraElevation; // elevation above target (degrees)
    private float cameraRadius;    // distance between camera and target
    
    public CameraOrbit3D(Camera cam, GameObject av, String gpName, Engine e) {
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 0.0f;     // Start directly behind
        cameraElevation = 20.0f;  // Start slightly above
        cameraRadius = 3.5f;      // Default zoom distance
        
        setupInputs(gpName);
        updateCameraPosition();
    }

    private void setupInputs(String gp) {
        InputManager im = engine.getInputManager();

        // If no gamepad is found, gp will be null. 
        // We only try to associate actions if a controller actually exists.
        if (gp != null) {
            OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
            OrbitElevationAction elvAction = new OrbitElevationAction();
            OrbitZoomAction zoomAction = new OrbitZoomAction();

            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, 
            azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, 
            elvAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, 
            zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
            System.out.println("CameraOrbit3D: Controller '" + gp + "' bound.");
        } else {
            System.out.println("CameraOrbit3D: No controller found. Input restricted to keyboard.");
        }
    }

    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double) avatarRot.angleSigned(
            new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));

        // Calculate camera position in spherical coordinates relative to avatar
        // (r, theta, phi) where: r = cameraRadius, theta = cameraAzimuth, 
        // phi = cameraElevation
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);

        // Spherical to Cartesian Conversion
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));

        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    // Inner classes for input actions
    private class OrbitAzimuthAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float rotAmount = (event.getValue() < -0.2f) ? -0.5f : (event.getValue() > 0.2f ? 0.5f : 0.0f);
            cameraAzimuth += rotAmount;
            cameraAzimuth %= 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float elvAmount = (event.getValue() < -0.2f) ? -0.5f : (event.getValue() > 0.2f ? 0.5f : 0.0f);
            cameraElevation += elvAmount;
            // Bound elevation to prevent camera flipping at the poles (90 degrees)
            cameraElevation = Math.max(5.0f, Math.min(70.0f, cameraElevation));
            updateCameraPosition();
        }
    }

    private class OrbitZoomAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float zoomAmount = (event.getValue() < -0.2f) ? -0.01f : (event.getValue() > 0.2f ? 0.01f : 0.0f);
            cameraRadius += zoomAmount;
            cameraRadius = Math.max(0.5f, Math.min(10.0f, cameraRadius)); // Min/Max zoom
            updateCameraPosition();
        }
    }

}
