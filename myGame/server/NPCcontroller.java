package myGame.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.client.GameClient;
import tage.ai.behaviortrees.*;

public class NPCcontroller {

    private NPC npc;
    private GameClient hostClient;

    private long lastTickTime;
    private long lastThinkTime;

    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

    private boolean avatarNear = false;
	private double avatarX, avatarY, avatarZ;

    public NPCcontroller(GameClient hostClient) {
        this.hostClient = hostClient;
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


			Vector3f initialLocation = new Vector3f(
				0,
				5,
				0
			);

			npc.setLocation(initialLocation.x(), initialLocation.y(), initialLocation.z());

			hostClient.getEnemyManager().createEnemy(
				npc.getID(), 
				initialLocation,
				(float) npc.getSize(),
				npc.getState(),
				new Quaternionf()
			);

			hostClient.sendCreateEnemy(
				npc.getID(),
				initialLocation,
				new Quaternionf(),
				npc.getState(),
				(float) npc.getSize()
			);

			System.out.println("Spawned enemy");


		/* 
		scheduler.scheduleAtFixedRate(() -> {

			npc.updateLocation();
			//hostClient.sendUpdateEnemy();

		}, 0, 25, java.util.concurrent.TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {

			long currentTime = System.currentTimeMillis();

			float elapsedThinkMs = (currentTime - lastThinkTime);

			lastThinkTime = currentTime;

			bt.update(elapsedThinkMs);

            // reset after thinking
            avatarNear = false;

		}, 0, 250, java.util.concurrent.TimeUnit.MILLISECONDS);
		*/
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