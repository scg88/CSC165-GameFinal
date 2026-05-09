import java.util.Random;

//import Chessnt.MyGame;
import tage.ai.behaviortrees.*;

public class NPCcontroller {
    private NPC npc;
    private Random rn = new Random();
    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
    private boolean nearFlag = false;
    private long lastThinkUpdateTime, lastTickUpdateTime;
    private GameServerUDP server; // Note: Changed to match your file name
    private double criteria = 2.0;
    private long lastDanceTime = 0;

    public void setLastDanceTime(long time) { lastDanceTime = time; }
    public long getLastDanceTime() { return lastDanceTime; }

    public void start(GameServerUDP s) {
        server = s;
        setupNPCs();
        setupBehaviorTree();

        lastThinkUpdateTime = System.nanoTime();
        lastTickUpdateTime = System.nanoTime();

        // Run the loop in a separate thread so the Server can keep doing its job
        Thread npcThread = new Thread(new Runnable() {
            public void run() {
                npcLoop();
            }
        });
        npcThread.start();
    }

    public void setupNPCs() {
        npc = new NPC();
        npc.randomizeLocation(rn.nextInt(40), rn.nextInt(40));
    }

    public void sendNPCinfo() {
    // We send: npcID (0 for now), X, Y, Z, and Size
    // Note: npc.getSize() needs to exist in your NPC.java
        server.sendNPCInfo(
            npc.getX(), 
            npc.getY(), 
            npc.getZ(), 
            npc.getSize(),
            (double)npc.getYAngle() // Ensure your server's sendNPCInfo method handles this 5th double
            //npc.getYAngle()
        );
    }

    public void npcLoop() {
        while (true) {
            long currentTime = System.nanoTime();

            float elapsedTickMilliSecs = (currentTime - lastTickUpdateTime) / 1000000.0f;
            float elapsedThinkMilliSecs = (currentTime - lastThinkUpdateTime) / 1000000.0f;

            // --- TICK: Physical updates (Every 25ms) ---
            if (elapsedTickMilliSecs >= 25.0f) {
                lastTickUpdateTime = currentTime;
                npc.updateLocation(); // Moves the sentinel

                if (server.getClientCount() > 0) { 
                // Broadcast the new position/size to everyone
                    sendNPCinfo(); 
                // Ask clients to check if they are near the new position
                    server.sendCheckForAvatarNear();
                }
            }
            // --- THINK: AI Decision making (Every 250ms) ---
            if (elapsedThinkMilliSecs >= 25.0f) {
                lastThinkUpdateTime = currentTime;
                bt.update(elapsedThinkMilliSecs);

                //nearFlag = false; // Reset the near flag for the next round of checks
            }
            Thread.yield();
        }
    }

    public void setupBehaviorTree() {
        // sequence 1: If Avatar is near, then grow, spin, and shrink
        bt.insertAtRoot(new BTSequence(10));
        bt.insert(10, new AvatarNear(server, this, npc, false));
        bt.insert(10, new GetBig(npc)); // Action 1 : grow (Professor's example)
        bt.insert(10, new RotateNPC(npc, this, server)); // Action 2: spin (New example)

        // sequence 2: If Avatar is NOT near, and .5 seconds have passed, then grow big (just for fun)
        bt.insertAtRoot(new BTSequence(20));
        bt.insert(20, new OneSecPassed(npc, this, false)); // Professor's Time check example
        bt.insert(20, new GetSmall(npc, this)); // Professor's example
    }

    public NPC getNPC() { return npc; }
    public double getCriteria() { return criteria; }
    public boolean getNearFlag() { return nearFlag; }
    public void setNearFlag(boolean f) { nearFlag = f; }
}