import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class BTWait extends BTAction {
    private float waitTimeMilli; // Total time to wait
    private float startTime = 0; // Cumulative time passed
    private boolean initialized = false;

    /**
     * @param time The amount of time to wait in milliseconds
     */
    public BTWait(float time) {
        waitTimeMilli = time;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        // elapsedTime comes from bt.update(elapsedThinkMilliSecs) in your loop
        startTime += elapsedTime;

        if (startTime >= waitTimeMilli) {
            startTime = 0; // Reset for next time this node is used
            return BTStatus.BH_SUCCESS;
        }

        return BTStatus.BH_RUNNING;
    }
}