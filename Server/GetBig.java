import tage.ai.behaviortrees.*;

public class GetBig extends BTAction {
    private NPC npc;

    public GetBig(NPC n) {
        npc = n;
    }

    @Override
    protected BTStatus update(float elapsedMilliSecs) {
        npc.setSize(2.0f); // Set to your "Large" scale
        return BTStatus.BH_SUCCESS;
    }
}