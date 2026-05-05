package myGame.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.networking.*;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID> {
	
	private class ServerEnemy {
		Vector3f position;
		Quaternionf rotation;

		public ServerEnemy(Vector3f initialPostion, Quaternionf initialRotation) {
			this.position = initialPostion;
			this.rotation = initialRotation;
		}
	}

	private class ServerPlayer {
		Vector3f position;
		Quaternionf rotation;

		public ServerPlayer(Vector3f initialPostion, Quaternionf initialRotation) {
			this.position = initialPostion;
			this.rotation = initialRotation;
		}
	}
	
	private HashMap<UUID, ServerEnemy> activeEnemies = new HashMap<>();
	private HashMap<UUID, ServerPlayer> connectedPlayers = new HashMap<>();
	
	NPCcontroller npcCtrl; 

	public GameServer(int localPort) throws IOException
	{
		super(localPort, ProtocolType.UDP);
		
		npcCtrl = new NPCcontroller(this);
        npcCtrl.start();
	}


	@Override
	public void processPacket(Object object, InetAddress senderIP, int senderPort)
	{	
		if(object == null || !(object instanceof GameClientPacket)) return;

		GameClientPacket packet = (GameClientPacket) object;

		if(packet instanceof ConnectClientPacket connectPacket)
			handleConnect(connectPacket, senderIP, senderPort);
		else if(packet instanceof DisconnectClientPacket disconnectPacket)
			handleDisconnect(disconnectPacket);
		else if(packet instanceof CreateEntityClientPacket createEntityPacket)
			handleEntityCreate(createEntityPacket);
		else if(packet instanceof UpdateEntityClientPacket updateEntityPacket)
			handleEntityUpdate(updateEntityPacket);
		else if(packet instanceof DeleteEntityClientPacket deleteEntityPacket)
			handleEntityDelete(deleteEntityPacket);
		else if(packet instanceof GetEntitiesClientPacket getEntitiesPacket)
			handleGetEntities(getEntitiesPacket);
	}

	private void handleConnect(ConnectClientPacket connectPacket, InetAddress senderIP, int senderPort) {
		
		try {
			IClientInfo clientInfo;

			clientInfo = getServerSocket().createClientInfo(senderIP, senderPort);

			UUID clientID = connectPacket.getClientIDFrom();

			addClient(clientInfo, clientID);

			ServerPlayer player = new ServerPlayer(connectPacket.getSpawnLocation(), new Quaternionf());

			this.connectedPlayers.put(clientID, player);

			forwardPacketToAll(
				new CreateEntityServerPacket(
					clientID,
					player.position,
					player.rotation,
					EntityType.PLAYER
				), 
				clientID
			);

			sendPacket(new ConnectServerPacket(true), clientID);

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleDisconnect(DisconnectClientPacket disconnectPacket) {

		try {

			UUID clientID = disconnectPacket.getClientIDFrom();

			removeClient(clientID);

			this.connectedPlayers.remove(clientID);

			forwardPacketToAll(new DeleteEntityServerPacket(clientID, EntityType.PLAYER), clientID);

			sendPacket(new DisconnectServerPacket(true), clientID);

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleEntityCreate(CreateEntityClientPacket createEntityPacket) {

		if(
			createEntityPacket.getEntityType() == EntityType.PLAYER
		) return;

	}

	private void handleEntityUpdate(UpdateEntityClientPacket updateEntityPacket) {

		if(updateEntityPacket.getEntityType() == EntityType.ENEMY) return;

		ServerPlayer player = this.connectedPlayers.get(updateEntityPacket.getClientIDFrom());
			
		if(player == null) return;

		player.position = updateEntityPacket.getPosition();
		player.rotation = updateEntityPacket.getRotation();

		try {
			forwardPacketToAll(
				new UpdateEntityServerPacket(
					updateEntityPacket.getClientIDFrom(),
					player.position,
					player.rotation,
					updateEntityPacket.getEntityType()
				), 
				updateEntityPacket.getClientIDFrom()
			);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleEntityDelete(DeleteEntityClientPacket deleteEntityPacket) {

		if(deleteEntityPacket.getEntityType() == EntityType.PLAYER) return;

		ServerEnemy enemy = this.activeEnemies.get(deleteEntityPacket.getEntityID());

		if(enemy == null) return;

		try {
			forwardPacketToAll(new DeleteEntityServerPacket(deleteEntityPacket.getEntityID(), EntityType.ENEMY), deleteEntityPacket.getClientIDFrom());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleGetEntities(GetEntitiesClientPacket getEntitiesPacket) {

		// Count every entity EXCEPt for the player who requested
		int entityCount = connectedPlayers.size() + activeEnemies.size() - 1;

		EntityInfo[] entities = new EntityInfo[entityCount];

		int index = 0;

		for(Entry<UUID, ServerPlayer> entry : this.connectedPlayers.entrySet()) {

			if(entry.getKey().compareTo(getEntitiesPacket.getClientIDFrom()) == 0) continue;
			
			EntityInfo entity = new EntityInfo();
			entity.id = entry.getKey();
			entity.position = entry.getValue().position;
			entity.rotation = entry.getValue().rotation;
			entity.type = EntityType.PLAYER;

			entities[index] = entity;

			index++;
		}

		for(Entry<UUID, ServerEnemy> entry : this.activeEnemies.entrySet()) {
			
			EntityInfo entity = new EntityInfo();
			entity.id = entry.getKey();
			entity.position = entry.getValue().position;
			entity.rotation = entry.getValue().rotation;
			entity.type = EntityType.ENEMY;

			entities[index] = entity;

			index++;
		}

		try {
			sendPacket(new GetEntitiesServerPacket(entities), getEntitiesPacket.getClientIDFrom());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendCreateEnemy() {

		UUID id = UUID.randomUUID();

		ServerEnemy enemy = new ServerEnemy(new Vector3f(0, 1, 0), new Quaternionf());

		this.activeEnemies.put(id, enemy);

		try {

			sendPacketToAll(new CreateEntityServerPacket(
				id,
				enemy.position,
				enemy.rotation,
				EntityType.ENEMY
			));

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	// ++++++++++++++++++++++++++++++++++ npc ++++++++++++++++++++++++++++++++++
	
	public void sendNPCinfo() {
		try {
			NPC npc = npcCtrl.getNPC();

			String message = String.join(";",
				"mnpc",
				npc.getID().toString(),
				String.valueOf(npc.getX()),
				String.valueOf(npc.getY()),
				String.valueOf(npc.getZ()),
				String.valueOf(npc.getSize()),
				String.valueOf(npc.getYaw()),
				npc.getState()
			);
			sendPacketToAll(message);
			// System.out.println("game server sendNPCinfo");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNPCstart(UUID clientID) {
		try {
			NPC npc = npcCtrl.getNPC();

			String message = String.join(";",
				"createNPC",
				npc.getID().toString(),
				String.valueOf(npc.getX()),
				String.valueOf(npc.getY()),
				String.valueOf(npc.getZ()),
				String.valueOf(npc.getSize()),
				npc.getState()
			);

			sendPacket(message, clientID);
			// System.out.println("game server sendNPCstart");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNPCstartToAll() {
		try {
			NPC npc = npcCtrl.getNPC();

			String message = String.join(";",
				"createNPC",
				npc.getID().toString(),
				String.valueOf(npc.getX()),
				String.valueOf(npc.getY()),
				String.valueOf(npc.getZ()),
				String.valueOf(npc.getSize()),
				npc.getState()
			);

			sendPacketToAll(message);
			// System.out.println("game server sendNPCstartToAll");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runSpawnNPC(String[] args) {
		UUID clientID = UUID.fromString(args[0]);

		double avatarX = Double.parseDouble(args[1]);
		double avatarY = Double.parseDouble(args[2]);
		double avatarZ = Double.parseDouble(args[3]);

		npcCtrl.spawnNPCAround(avatarX, avatarY, avatarZ);
	}

	private void runIsNear(String[] args) {
		float x = Float.parseFloat(args[1]);
		float y = Float.parseFloat(args[2]);
		float z = Float.parseFloat(args[3]);
		
		// System.out.println("server got avatar pos: " + x + "," + y + "," + z);

		npcCtrl.setAvatarNear(x, y, z);
	}
}
