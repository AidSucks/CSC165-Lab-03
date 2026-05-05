package myGame.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

import org.joml.*;

import myGame.MyGame;
import myGame.networking.*;
import tage.networking.client.GameConnectionClient;

public class GameClient extends GameConnectionClient {

	private UUID clientUUID;
	private MyGame game;
	private GhostManager ghostManager;
	private EnemyManager enemyManager;

	private boolean isConnected = false;

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

		if(packet instanceof ConnectServerPacket connectPacket)
			handleConnect(connectPacket);
		else if(packet instanceof DisconnectServerPacket disconnectPacket) {
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
			handleGetEntities(getEntitiesPacket);
		}
	}

	private void handleConnect(ConnectServerPacket connectPacket) {

		if(!connectPacket.getIsConnected()) return;

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

		// TODO implement enemy logic

		if(createEntityPacket.getEntityType() == EntityType.PLAYER) {
			this.ghostManager.createGhost(createEntityPacket.getEntityID(), createEntityPacket.getPosition());
		}
		
	}

	private void handleUpdateEntity(UpdateEntityServerPacket updateEntityPacket) {

		// TODO implement enemy logic

		if(updateEntityPacket.getEntityType() == EntityType.PLAYER) {

			UUID ghostID = updateEntityPacket.getEntityID();

			this.ghostManager.updateGhostMove(ghostID, updateEntityPacket.getPosition());
			this.ghostManager.updateGhostRotate(ghostID, updateEntityPacket.getRotation());
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

		// TODO implement enemy logic

		EntityInfo[] entities = getEntitiesPacket.getEntities();

		for(EntityInfo e : entities) {
			if(e.type == EntityType.PLAYER) {
				this.ghostManager.createGhost(e.id, e.position);
			}
		}

		this.isConnected = true;
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

	public void sendMove(Vector3f position, Quaternionf rotation)
	{
		try {

			sendPacket(new UpdateEntityClientPacket(
				clientUUID, 
				position, 
				rotation, 
				EntityType.PLAYER
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	
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

		enemyManager.createEnemy(npcID, npcPos, size, state);
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
		
		enemyManager.updateEnemy(npcID, npcPos, size, yaw, state);
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