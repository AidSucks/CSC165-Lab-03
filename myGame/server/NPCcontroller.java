package myGame.server;

import tage.ai.behaviortrees.*;

public class NPCcontroller {

    private NPC npc;
    private GameServer server;

    private long lastTickTime;
    private long lastThinkTime;
	
	
    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

    private boolean avatarNear = false;
	private double avatarX, avatarY, avatarZ;


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


	public void setAvatarNear(double x, double y, double z) {
		avatarNear = true;
		avatarX = x;
		avatarY = y;
		avatarZ = z;
	}
	
	public void spawnNPCAround(double avatarX, double avatarY, double avatarZ) {
		double angle = Math.random() * Math.PI * 2.0;
		double distance = 5.0 + Math.random() * 10.0;

		double x = avatarX + Math.cos(angle) * distance;
		double z = avatarZ + Math.sin(angle) * distance;

		npc.setLocation(x, 0, z);

		server.sendNPCstartToAll();
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

        // // Sequence 10:
        // // if 1 second passed -> get small
        // bt.insert(10, new OneSecPassed(false));
        // bt.insert(10, new GetSmall(npc));

        // // Sequence 20:
        // // if avatar near -> get big
        // bt.insert(20, new AvatarNear(this, false));
        // bt.insert(20, new GetBig(npc));
		
		// If avatar is near, chase avatar
		bt.insert(10, new AvatarNear(this, false));
		bt.insert(10, new ChaseAvatar(npc, this));

		// If avatar is NOT near for 1 second, stop chasing
		bt.insert(20, new AvatarNear(this, true));
		bt.insert(20, new OneSecPassed(false));
		bt.insert(20, new StopChasing(npc));
    }
	
	public double getAvatarX() { return avatarX; }
	public double getAvatarY() { return avatarY; }
	public double getAvatarZ() { return avatarZ; }
}