package myGame.ai;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.MyGame;
import myGame.networking.client.GameClient;
import tage.ai.behaviortrees.*;

public class NPCcontroller {

	private MyGame game;
    private NPC npc;
    private GameClient hostClient;

	private int maxEnemies = 1;
	private int numEnemies = 0;

	private float enemySpawnTimer = 0;
	private float enemyThinkTimer = 0;
	private float enemyUpdateTimer = 0;

	private final float enemySpawnTime = 10f;
	private final float enemyThinkTime = 0.250f;
	private final float enemyUpdateTime = 0.025f;

	private long currentThinkTime = 0;
	private long lastThinkTime = 0;

    private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

    private boolean avatarNear = false;
	private double avatarX, avatarY, avatarZ;

	private boolean isActive = false;

    public NPCcontroller(GameClient hostClient, MyGame game) {
		this.game = game;
        this.hostClient = hostClient;
        this.npc = new NPC();
		setupBehaviorTree();
    }

	public boolean isActive() { return this.isActive; }

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

	public void update(float deltaTime)
	{
		if(!this.isActive) return;

		enemySpawnTimer += deltaTime;
		enemyThinkTimer += deltaTime;
		enemyUpdateTimer += deltaTime;

		if(enemySpawnTimer >= enemySpawnTime) {

			if(numEnemies < maxEnemies) {

				Vector3f initialPosition = new Vector3f(
					(float) npc.getX(),
					game.getTerrain().getHeight((float) npc.getX(), (float) npc.getZ()),
					(float) npc.getZ()
				);

				hostClient.sendCreateEnemy(
					npc.getID(), 
					initialPosition, 
					new Quaternionf(), 
					npc.getState(), 
					(float) npc.getSize()
				);
				numEnemies += 1;
			}

			enemySpawnTimer = 0f;
		}

		if(enemyUpdateTimer >= enemyUpdateTime) {

			npc.updateLocation();

			Vector3f position = new Vector3f(
				(float) npc.getX(),
				game.getTerrain().getHeight((float) npc.getX(), (float) npc.getZ()),
				(float) npc.getZ()
			);

			this.hostClient.sendUpdateEnemy(npc.getID(), position, new Quaternionf(), npc.getState());

			enemyUpdateTimer = 0f;
		}

		if(enemyThinkTimer >= enemyThinkTime) {
			
			this.currentThinkTime = System.currentTimeMillis();
			
			float elapsedThinkMs = currentThinkTime - lastThinkTime;

			lastThinkTime = currentThinkTime;

			bt.update(elapsedThinkMs);

			avatarNear = false;
			
			enemyThinkTimer = 0f;
		}
	}

    public void start() {
		this.isActive = true;

		enemySpawnTimer = 0f;
		enemyThinkTimer = 0f;
		enemyUpdateTimer = 0f;
    }

	public void stop() {
		this.isActive = false;
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