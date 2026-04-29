package myGame.server;

import tage.ai.behaviortrees.*;

public class NPCcontroller {

    private NPC npc;
    private GameServer server;

    private long lastTickTime;
    private long lastThinkTime;
	
	
    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

    private boolean avatarNear = false;

    public NPCcontroller(GameServer server) {
        this.server = server;
        this.npc = new NPC();
		setupBehaviorTree();

    }

    public NPC getNPC() {
        return npc;
    }
	
	public boolean isAvatarNear() {
        return avatarNear;
    }

    public void setAvatarNear(boolean value) {
        avatarNear = value;
    }

    public void start() {
        lastTickTime = System.nanoTime();
        lastThinkTime = System.nanoTime();

        Thread npcThread = new Thread(() -> npcLoop());
        npcThread.start();
    }

    private void npcLoop() {
        while (true) {
            long currentTime = System.nanoTime();

            float elapsedTickMs =
                (currentTime - lastTickTime) / 1_000_000.0f;

            float elapsedThinkMs =
                (currentTime - lastThinkTime) / 1_000_000.0f;

            // TICK: movement/update often
            if (elapsedTickMs >= 25.0f) {
                lastTickTime = currentTime;

                npc.updateLocation();
                server.sendNPCinfo();
            }

            // THINK: AI decision less often
            if (elapsedThinkMs >= 250.0f) {
                lastThinkTime = currentTime;
				bt.update(elapsedThinkMs);
				
				
                // reset after thinking
                avatarNear = false;

            }

            Thread.yield();
        }
    }
	
	
    private void setupBehaviorTree() {
        bt.insertAtRoot(new BTSequence(10));
        bt.insertAtRoot(new BTSequence(20));

        // Sequence 10:
        // if 1 second passed -> get small
        bt.insert(10, new OneSecPassed(false));
        bt.insert(10, new GetSmall(npc));

        // Sequence 20:
        // if avatar near -> get big
        bt.insert(20, new AvatarNear(this, false));
        bt.insert(20, new GetBig(npc));
    }
}