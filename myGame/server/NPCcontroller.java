package myGame.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import tage.ai.behaviortrees.*;

public class NPCcontroller {

    private NPC npc;
    private GameServer server;

    private long lastTickTime;
    private long lastThinkTime;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	
	
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
		
	}

    public void start() {
        lastTickTime = System.currentTimeMillis();
        lastThinkTime = System.currentTimeMillis();

		scheduler.schedule(() -> {

			npc.setLocation(0, 0, 0);

			server.sendCreateEnemy();

			System.out.println("Spawned enemy");

		}, 30, java.util.concurrent.TimeUnit.SECONDS);

		scheduler.scheduleAtFixedRate(() -> {

			npc.updateLocation();
			server.sendUpdateEnemy();

		}, 0, 25, java.util.concurrent.TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {

			long currentTime = System.currentTimeMillis();

			float elapsedThinkMs = (currentTime - lastThinkTime);

			lastThinkTime = currentTime;

			bt.update(elapsedThinkMs);

            // reset after thinking
            avatarNear = false;

		}, 0, 250, java.util.concurrent.TimeUnit.MILLISECONDS);
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