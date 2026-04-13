package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
* A BobbingController is a node controller that, when enabled, causes any object
* it is attached to to bob up and down in place at a specified height.
* @author Spencer Green
*/

public class BobbingController extends NodeController {
    private float bobRate;      // Speed of the bob
    private float bobHeight;    // Distance of the bob
    private float totalTime = 0f;

    public BobbingController(Engine e, float rate, float height) {
        super();
        bobRate = rate;
        bobHeight = height;
    }

    @Override
    public void apply(GameObject go) {
        float elapsTime = super.getElapsedTime();
        totalTime += elapsTime / 1000.0f;
    
        // Calculate the height offset
        float moveAmount = (float) java.lang.Math.sin(totalTime * bobRate) * bobHeight;
    
        // Get the current translation matrix
        Matrix4f localTrans = go.getLocalTranslation();
        // Use a tiny multiplier so the Home doesn't fly away
        localTrans.translate(0, (float)java.lang.Math.sin(totalTime * bobRate) * bobHeight * 0.1f, 0);
    
        go.setLocalTranslation(localTrans);
}
}