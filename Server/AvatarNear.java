import tage.ai.behaviortrees.BTCondition;
import java.util.UUID;

public class AvatarNear extends BTCondition
{ 
    private NPC npc;
    private NPCcontroller npcc;
    private GameServerUDP server;

    public AvatarNear(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate)
    { 
        super(toNegate);
        server = s; 
        npcc = c; 
        npc = n;
    }

    @Override
    protected boolean check()
    { 
        //server.sendCheckForAvatarNear();
        //return npcc.getNearFlag();

        /*long currentTime = System.currentTimeMillis();
        if (currentTime - npcc.getLastDanceTime() < 5000) {
            return false; 
        }*/
        
        if (npcc.getNearFlag()) {
            return true;
        }

        // STICKY LOGIC: 
        // Even if the flag is false (maybe a dropped packet), 
        // if the NPC hasn't finished its rotation yet, keep the condition true
        // so the sequence doesn't break.
        float currentAngle = npc.getYAngle() % 360.0f;
        if (currentAngle > 0.5f && currentAngle < 359.5f) {
            return true; 
        }

        return false;
    } 
}