package myGame.ai;

import java.util.ArrayList;
import java.util.Arrays;

import myGame.MyGame;
import myGame.networking.EntityInfo;
import myGame.networking.client.GameClient;

public class NPCcontroller {

	private MyGame game;

	private ArrayList<NPC> npcs;
    private GameClient hostClient;

	private int maxEnemies = 10;

	private float enemySpawnTimer = 0;
	private float enemyThinkTimer = 0;
	private float enemyUpdateTimer = 0;

	private final float enemySpawnTime = 10f; 		// 10 S
	private final float enemyThinkTime = 0.250f; 	// 250 MS
	private final float enemyUpdateTime = 0.025f;	// 25 MS

	private boolean isActive = false;

    public NPCcontroller(GameClient hostClient, MyGame game) {
		this.game = game;
        this.hostClient = hostClient;
		this.npcs = new ArrayList<>();
    }

	public boolean isActive() { return this.isActive; }

	public void setEntitiesFromServer(EntityInfo[] entities) {

		if(entities == null) return;

		EntityInfo[] entityArr = Arrays.copyOf(entities, entities.length);

		for(NPC npc : this.npcs)
			npc.setEntities(entityArr);
	}

	public void update(float deltaTime)
	{
		if(!this.isActive) return;

		enemySpawnTimer += deltaTime;
		enemyThinkTimer += deltaTime;
		enemyUpdateTimer += deltaTime;

		queryUpdateTimer();
		
		queryThinkTimer(deltaTime);

		querySpawnTimer();
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

	private void querySpawnTimer() 
	{
		if(enemySpawnTimer < enemySpawnTime) return;

		// Reset timer
		enemySpawnTimer = 0f;

		if(this.npcs.size() >= maxEnemies) return;

		NPC npc = new NPC(game.getTerrain());

		hostClient.sendCreateEnemy(
			npc.getID(), 
			npc.getLocation(), 
			npc.getRotation(), 
			npc.getState(), 
			(float) npc.getSize()
		);

		this.npcs.add(npc);
	}

	private void queryUpdateTimer()
	{
		if(enemyUpdateTimer < enemyUpdateTime) return;

		enemyUpdateTimer = 0f;

		for(NPC npc : this.npcs) {

			// Run updates for each npc
			npc.updateLocation();

			this.hostClient.sendUpdateEnemy(
				npc.getID(), 
				npc.getLocation(), 
				npc.getRotation(),
				npc.getState()
			);
		}
	}

	private void queryThinkTimer(float dt)
	{
		if(enemyThinkTimer < enemyThinkTime) return;

		enemyThinkTimer = 0f;

		this.hostClient.checkIsNear();

		for(NPC npc : this.npcs) {
			npc.getBehaviorTree().update(dt);
		}
	}
}