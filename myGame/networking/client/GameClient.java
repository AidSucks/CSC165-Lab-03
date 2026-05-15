package myGame.networking.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.joml.*;

import myGame.Bullet;
import myGame.MyGame;
import myGame.ai.NPCcontroller;
import myGame.networking.*;
import myGame.networking.server.*;
import tage.networking.client.GameConnectionClient;

public class GameClient extends GameConnectionClient {

	private NPCcontroller controller;
	private UUID clientUUID;
	private MyGame game;
	private GhostManager ghostManager;
	private EnemyManager enemyManager;
	private BulletManager bulletManager;

	private boolean isConnected = false;
	private boolean checkIsNear = false;

	private boolean isHost = false;

	public GameClient(InetAddress remoteAddr, int remotePort, MyGame game, BulletManager bulletManager) throws IOException 
	{
		super(remoteAddr, remotePort, ProtocolType.UDP);

		this.clientUUID = UUID.randomUUID();
		this.game = game;
		this.ghostManager = new GhostManager(this.game);
		this.enemyManager = new EnemyManager(this.game);
		this.bulletManager = bulletManager;
	}

	public void setBulletManager(BulletManager mang) { this.bulletManager = mang; }
	
	// getter
	public GhostManager getGhostManager() { return ghostManager; }

	public NPCcontroller getController() { return this.controller; }
	
	public EnemyManager getEnemyManager() { return enemyManager; }

	public BulletManager getBulletManager() { return this.bulletManager; }

	public boolean getIsConnected() { return this.isConnected; }

	public boolean getIsHost() { return this.isHost; }

	@Override
	public void processPacket(Object object)
	{
		if(object == null || !(object instanceof GameServerPacket)) return;

		GameServerPacket packet = (GameServerPacket) object;

		// Ignore other packets if connection isn't completely setup yet
		if(!isConnected) {
			if(packet instanceof ConnectServerPacket connectPacket)
				handleConnect(connectPacket);
			else if(packet instanceof GetEntitiesServerPacket getEntitiesPacket)
				handleGetEntities(getEntitiesPacket);

			return;
		}

		if(packet instanceof DisconnectServerPacket disconnectPacket) {
			handleDisconnect(disconnectPacket);
		}
		else if(packet instanceof CreateEntityServerPacket createEntityPacket) {
			handleCreateEntity(createEntityPacket);
		}
		else if(packet instanceof UpdateEntityServerPacket updateEntityPacket) {
			handleUpdateEntity(updateEntityPacket);
		}
		else if(packet instanceof DeleteEntityServerPacket deleteEntityPacket) {
			handleDeleteEntity(deleteEntityPacket);
		}
		else if(packet instanceof GetEntitiesServerPacket getEntitiesPacket) {

			if(this.isHost && this.checkIsNear) {
				handleCheckIsNear(getEntitiesPacket);
			} else {
				handleGetEntities(getEntitiesPacket);
			}

		}	
	}

	private void handleConnect(ConnectServerPacket connectPacket) {

		if(connectPacket.getIsHost()) {
			this.isHost = true;
			this.controller = new NPCcontroller(this, this.game);
			this.controller.start();
		}

		System.out.println("Connected to Server");

		try {
			sendPacket(new GetEntitiesClientPacket(clientUUID));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleDisconnect(DisconnectServerPacket disconnectPacket) {
		
		if(!disconnectPacket.getIsDisconnected()) return;

		System.out.println("Disconnected from server");
	}

	private void handleCreateEntity(CreateEntityServerPacket createEntityPacket) {

		if(createEntityPacket.getEntityType() == EntityType.PLAYER) {
			this.ghostManager.createGhost(createEntityPacket.getEntityID(), createEntityPacket.getPosition());
		}
		else if(createEntityPacket.getEntityType() == EntityType.ENEMY) {
			this.enemyManager.createEnemy(
				createEntityPacket.getEntityID(),
				createEntityPacket.getPosition(),
				createEntityPacket.getEntityScale(),
				createEntityPacket.getAnimationState(),
				createEntityPacket.getRotation()
			);
		}
		else if(createEntityPacket.getEntityType() == EntityType.BULLET) {
			bulletManager.addBullet(createEntityPacket.getEntityID(), createEntityPacket.getPosition(), createEntityPacket.getDirection(), true);
		}
	}

	private void handleUpdateEntity(UpdateEntityServerPacket updateEntityPacket) {

		if(updateEntityPacket.getEntityType() == EntityType.ENEMY) {
			enemyManager.updateEnemy(
				updateEntityPacket.getEntityID(),
				updateEntityPacket.getPosition(),
				0.05f,
				updateEntityPacket.getAnimationState(),
				updateEntityPacket.getRotation()
			);
			return;
		}

		if(updateEntityPacket.getEntityType() == EntityType.PLAYER) {

			UUID ghostID = updateEntityPacket.getEntityID();

			this.ghostManager.updateGhostMove(ghostID, updateEntityPacket.getPosition(), updateEntityPacket.getRotation());
			return;
		}

		if(updateEntityPacket.getEntityType() == EntityType.BULLET) {

			this.bulletManager.updateServer(updateEntityPacket.getEntityID(), updateEntityPacket.getPosition());
		}
	}

	private void handleDeleteEntity(DeleteEntityServerPacket deleteEntityPacket) {

		if(deleteEntityPacket.getEntityType() == EntityType.PLAYER) {
			this.ghostManager.removeGhost(deleteEntityPacket.getEntityID());
		}
		else if(deleteEntityPacket.getEntityType() == EntityType.ENEMY) {
			this.enemyManager.removeEnemy(deleteEntityPacket.getEntityID());
		}
		else if(deleteEntityPacket.getEntityType() == EntityType.BULLET) {
			this.bulletManager.removeBullet(deleteEntityPacket.getEntityID());
		}
	}

	private void handleGetEntities(GetEntitiesServerPacket getEntitiesPacket) {

		this.isConnected = true;

		EntityInfo[] entities = getEntitiesPacket.getEntities();

		if(entities.length == 0) return;

		for(EntityInfo e : entities) {

			// Check if is current player
			if(e.id.compareTo(clientUUID) == 0) continue;

			if(e.type == EntityType.PLAYER) {
				this.ghostManager.createGhost(e.id, e.position);
			}
			else if(e.type == EntityType.ENEMY) {
				this.enemyManager.createEnemy(
					e.id, 
					e.position,
					e.entityScale,
					e.animationState,
					e.rotation
				);
			}
			else if(e.type == EntityType.BULLET) {
				this.bulletManager.addBullet(e.id, e.position, e.direction, true);
			}
		}
	}

	private void handleCheckIsNear(GetEntitiesServerPacket getEntitiesPacket)
	{
		this.controller.setEntitiesFromServer(getEntitiesPacket.getEntities());
		this.checkIsNear = false;
	}


	public void joinServer()
	{
		try {
			sendPacket(new ConnectClientPacket(clientUUID, new Vector3f(0, 10, 0)));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void leaveServer()
	{
		try {
			sendPacket(new DisconnectClientPacket(clientUUID));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendMove(
		UUID id, 
		Vector3f position, 
		Quaternionf rotation, 
		EntityType entityType, 
		String animation
	) {
		UUID uuid;

		if(id == null || entityType == EntityType.PLAYER)
			uuid = clientUUID;
		else
			uuid = id;

		try {

			sendPacket(new UpdateEntityClientPacket(
				clientUUID,
				uuid, 
				position, 
				rotation, 
				entityType,
				animation
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendCreateBullet(Bullet bullet) {

		try {
			sendPacket(new CreateEntityClientPacket(
				clientUUID, 
				bullet.getID(), 
				bullet.getWorldLocation(), 
				new Quaternionf(),
				EntityType.BULLET,
				"NONE", 
				0.25f,
				bullet.getDirection()
			));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendDeleteBullet(Bullet bullet) {

		try {
			sendPacket(new DeleteEntityClientPacket(
				clientUUID,
				bullet.getID(),
				EntityType.BULLET
			));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendCreateEnemy(
		UUID id,
		Vector3f initialPosition,
		Quaternionf initialRotation,
		String initialAnimation,
		float entityScale
	){
		try {

			sendPacket(new CreateEntityClientPacket(
				clientUUID,
				id, 
				initialPosition, 
				initialRotation, 
				EntityType.ENEMY,
				initialAnimation,
				entityScale,
				new Vector3f(1, 0, 0)
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		this.enemyManager.createEnemy(
			id, 
			initialPosition, 
			entityScale, 
			initialAnimation, 
			initialRotation
		);
	}

	public void sendUpdateEnemy(
		UUID id,
		Vector3f position,
		Quaternionf rotation,
		String animationState
	) {
		try {

			sendPacket(new UpdateEntityClientPacket(
				clientUUID,
				id, 
				position, 
				rotation, 
				EntityType.ENEMY,
				animationState
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		this.enemyManager.updateEnemy(id, position, 0.05f, animationState, rotation);
		this.enemyManager.updateAnimations();
	}

	public void sendDeleteEnemy(
		UUID id,
		EntityType type
	) {
		try {

			sendPacket(new DeleteEntityClientPacket(
				clientUUID,
				id, 
				type
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		this.enemyManager.removeEnemy(id);
	}

	public void checkIsNear() {

		if(!isHost) return;

		try {
			sendPacket(new GetEntitiesClientPacket(clientUUID));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		this.checkIsNear = true;
	}

	
}