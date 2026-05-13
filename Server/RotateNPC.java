//import Chessnt.MyGame;
import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;
//import tage.audio.Sound;

public class RotateNPC extends BTAction {
    private NPC npc;
    private NPCcontroller npcc;
    private GameServerUDP server;
    private float degreesRotated = 0f;
    private float spinSpeed = 360.0f; // Degrees per second

    public RotateNPC(NPC n, NPCcontroller c, GameServerUDP s) {
        npc = n;
        npcc = c;
        server = s;
    }

    @Override
    protected BTStatus update(float elapsedTime) {

        if (degreesRotated == 0f) {
            if (server != null) {
                server.sendNPCWelcomeSound();
                server.sendGameLoopSound();
            }
        }

        // elapsedTime is in milliseconds from the BT update
        float sec = elapsedTime / 1000.0f;
        float frameRotation = spinSpeed * sec;
        
        // Update the NPC's logical angle
        npc.setYAngle(npc.getYAngle() + frameRotation);
        degreesRotated += frameRotation;

        // --- Update 3D Sound Position ---
        // Make sure the sound source moves with the NPC's world position
        if (npc.getWelcomeSound() != null) {
            npc.getWelcomeSound().setLocation(npc.getWorldLocation());
        }
        if (npc.getGameLoopSound() != null) {
            npc.getGameLoopSound().setLocation(npc.getWorldLocation());
        }

        // Once we hit 1440, reset for next time and return success
        if (degreesRotated >= 720f) {
            degreesRotated = 0f; // Reset for next time

            npcc.setNearFlag(false); // Reset the near flag so AvatarNear will check distance again
            return BTStatus.BH_SUCCESS;
        }
        
        // While spinning, return RUNNING so the sequence doesn't skip ahead
        return BTStatus.BH_RUNNING;
    }
}