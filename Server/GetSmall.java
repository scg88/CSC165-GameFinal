import tage.ai.behaviortrees.*;

public class GetSmall extends BTAction {
    private NPC npc;
    private NPCcontroller npcc;

    public GetSmall(NPC n, NPCcontroller c) {
        npc = n;
        npcc = c;
    }

    @Override
    protected BTStatus update(float elapsed) {
        npc.setSize(1.0f); // Back to normal size
        npcc.setNearFlag(false); // Force the flag to false!
        //npcc.setLastDanceTime(System.currentTimeMillis());
        return BTStatus.BH_SUCCESS;
    }
}