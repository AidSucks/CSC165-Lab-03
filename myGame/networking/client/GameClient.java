package myGame.networking.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.joml.*;

import myGame.MyGame;
import myGame.ai.NPCcontroller;
import myGame.networking.*;
import myGame.networking.server.*;
import tage.networking.client.GameConnectionClient;

public class GameClient extends GameConnectionClient {

	private UUID clientUUID;
	private MyGame game;
	private GhostManager ghostManager;
	private EnemyManager enemyManager;
	private NPCcontroller npcController;

	private boolean isConnected = false;
	private boolean checkIsNear = false;

	private boolean isHost = false;

	public GameClient(InetAddress remoteAddr, int remotePort, MyGame game) throws IOException 
	{
		super(remoteAddr, remotePort, ProtocolType.UDP);

		this.clientUUID = UUID.randomUUID();
		this.game = game;
		this.ghostManager = new GhostManager(this.game);
		this.enemyManager = new EnemyManager(this.game);
	}
	
	// getter
	public GhostManager getGhostManager() {
		return ghostManager;
	}
	
	public EnemyManager getEnemyManager() {
		return enemyManager;
	}

	public boolean getIsConnected() {
		return this.isConnected;
	}

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
			this.setupHost();
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
	}

	private void handleUpdateEntity(UpdateEntityServerPacket updateEntityPacket) {

		if(updateEntityPacket.getEntityType() == EntityType.ENEMY) {
			enemyManager.updateEnemy(
				updateEntityPacket.getEntityID(),
				updateEntityPacket.getPosition(),
				1f,
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
	}

	private void handleDeleteEntity(DeleteEntityServerPacket deleteEntityPacket) {

		if(deleteEntityPacket.getEntityType() == EntityType.PLAYER) {
			this.ghostManager.removeGhost(deleteEntityPacket.getEntityID());
		}
		else if(deleteEntityPacket.getEntityType() == EntityType.ENEMY) {
			this.enemyManager.removeEnemy(deleteEntityPacket.getEntityID());
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
		}
	}

	private void handleCheckIsNear(GetEntitiesServerPacket getEntitiesPacket)
	{
		this.checkIsNear = false;
	}

	private void setupHost() {

		this.npcController = new NPCcontroller(this);

		this.npcController.start();
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
				entityScale
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
	}

	public void checkIsNear() {

		try {
			sendPacket(new GetEntitiesClientPacket(clientUUID));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}

		this.checkIsNear = true;
	}

	public boolean getIsHost() { return this.isHost; }

	
	// ++++++++++++++++++++++++++++++++++ NPC ++++++++++++++++++++++++++++++++++
	
	public void sendSpawnNPCRequest(Vector3f avatarPos) {
		try {
			sendPacket(String.join(
				";",
				"spawnNPC",
				clientUUID.toString(),
				String.valueOf(avatarPos.x),
				String.valueOf(avatarPos.y),
				String.valueOf(avatarPos.z)
			));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void runCreateNPC(String[] args) {
		UUID npcID = UUID.fromString(args[0]);
		
		float x = Float.parseFloat(args[1]);
		float z = Float.parseFloat(args[3]);
		float y = game.getTerrain().getHeight(x, z);
		float size = Float.parseFloat(args[4]);
		String state = args[5];

		Vector3f npcPos = new Vector3f(x, y, z);

		checkAvatarNearNPC(npcPos);
		
		System.out.println("game client runCreateNPC");
	}
	
	private void runMoveNPC(String[] args) {
		UUID npcID = UUID.fromString(args[0]);
		
		float x = Float.parseFloat(args[1]);
		float z = Float.parseFloat(args[3]);
		float y = game.getTerrain().getHeight(x, z);
		float size = Float.parseFloat(args[4]);
		float yaw = Float.parseFloat(args[5]);
		String state = args[6];

		Vector3f npcPos = new Vector3f(x, y, z);
		
		checkAvatarNearNPC(npcPos);
		
		// System.out.println("game client runMoveNPC");
	}
	
	private void checkAvatarNearNPC(Vector3f npcPos) {
		Vector3f avatarPos = game.getAvatar().getWorldLocation();

		float distance = avatarPos.distance(npcPos);

		// System.out.println("Distance to NPC = " + distance);

		if (distance < 3.0f) {
			try {
				sendPacket(String.join(
					";",
					"isnear",
					clientUUID.toString(),
					String.valueOf(avatarPos.x),
					String.valueOf(avatarPos.y),
					String.valueOf(avatarPos.z)
				));
				// System.out.println("client sent isnear");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	

}