import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
    private NPC npc;
    //private GameObject player;
    private NPCcontroller controller;
    private long lastUpdateTime;
    private boolean timerStarted = false;

    public OneSecPassed(NPC n, NPCcontroller c, boolean neg) {
        super(neg);
        npc = n;
        controller = c;
       // player = p; // Store the player object
        //lastUpdateTime = System.nanoTime();
    }

    @Override
    protected boolean check() {

        // Use the controller's nearFlag! 
        // This flag is set by AvatarNear when a client reports they are close.
        if (controller.getNearFlag()) {
            timerStarted = false; 
            return false;
        }

        // Now the timer ONLY starts/runs if we passed the distance check above
        // Initialize the timer only when the BT actually checks this node for the first time
        if (!timerStarted) {
            lastUpdateTime = System.nanoTime();
            timerStarted = true;
            return false;
        }

        long currentTime = System.nanoTime();
        // Convert nanoseconds to seconds
        float elapsedSeconds = (currentTime - lastUpdateTime) / 1000000000.0f;

        if (elapsedSeconds >= 0.5f) {
            timerStarted = false; // Reset the timer
            return true; 
        }

        return false;
    }
}