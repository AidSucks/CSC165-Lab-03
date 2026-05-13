package myGame.networking.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;

import org.joml.Quaternionf;

import myGame.networking.*;
import myGame.networking.client.*;
import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID> {
	
	private HashMap<UUID, EntityInfo> activeEntities = new HashMap<>();

	private boolean hostIsSet = false;
	
	public GameServer(int localPort) throws IOException
	{
		super(localPort, ProtocolType.UDP);
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

			EntityInfo player = new EntityInfo(clientID);
			
			player.position = connectPacket.getSpawnLocation();
			player.rotation = new Quaternionf();
			player.animationState = "IDLE";
			player.entityScale = 1f;
			player.type = EntityType.PLAYER;

			this.activeEntities.put(clientID, player);

			forwardPacketToAll(
				new CreateEntityServerPacket(
					clientID,
					player.position,
					player.rotation,
					player.type,
					player.animationState,
					player.entityScale
				), 
				clientID
			);

			System.out.println("Player: " + clientID + " connected.");

			if(!hostIsSet) {
				sendPacket(new ConnectServerPacket(true), clientID);
				hostIsSet = true;
				System.out.println("Player: " + clientID + " is now host.");
				return;
			}

			sendPacket(new ConnectServerPacket(false), clientID);

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleDisconnect(DisconnectClientPacket disconnectPacket) {

		try {

			UUID clientID = disconnectPacket.getClientIDFrom();

			removeClient(clientID);

			this.activeEntities.remove(clientID);

			forwardPacketToAll(new DeleteEntityServerPacket(clientID, EntityType.PLAYER), clientID);

			sendPacket(new DisconnectServerPacket(true), clientID);

			System.out.println("Player: " + clientID + " disconnected.");

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleEntityCreate(CreateEntityClientPacket createEntityPacket)
	{
		EntityInfo entity = new EntityInfo(createEntityPacket.getEntityID());

		entity.position = createEntityPacket.getPosition();
		entity.rotation = createEntityPacket.getRotation();
		entity.animationState = createEntityPacket.getAnimationState();
		entity.entityScale = createEntityPacket.getEntityScale();
		entity.type = createEntityPacket.getEntityType();

		this.activeEntities.put(createEntityPacket.getEntityID(), entity);

		System.out.println("Created Entity: " + entity.id);

		try {
			forwardPacketToAll(
				new CreateEntityServerPacket(
					createEntityPacket.getEntityID(),
					entity.position,
					entity.rotation,
					entity.type,
					entity.animationState,
					entity.entityScale
				), 
				createEntityPacket.getClientIDFrom()
			);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleEntityUpdate(UpdateEntityClientPacket updateEntityPacket)
	{
		EntityInfo entity = this.activeEntities.get(updateEntityPacket.getEntityID());

		if(entity == null) {
			System.err.println("Could not update entity: " + updateEntityPacket.getEntityID());
			return;
		}

		entity.position = updateEntityPacket.getPosition();
		entity.rotation = updateEntityPacket.getRotation();
		entity.animationState = updateEntityPacket.getAnimationState();

		try {
			forwardPacketToAll(
				new UpdateEntityServerPacket(
					updateEntityPacket.getEntityID(),
					entity.position,
					entity.rotation,
					updateEntityPacket.getEntityType(),
					entity.animationState
				), 
				updateEntityPacket.getClientIDFrom()
			);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleEntityDelete(DeleteEntityClientPacket deleteEntityPacket)
	{
		EntityInfo entity = this.activeEntities.get(deleteEntityPacket.getEntityID());

		if(entity == null) {
			System.err.println("Could not delete entity: " + deleteEntityPacket.getEntityID());
			return;
		}

		try {
			forwardPacketToAll(new DeleteEntityServerPacket(
				deleteEntityPacket.getEntityID(), 
				entity.type
			), deleteEntityPacket.getClientIDFrom());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void handleGetEntities(GetEntitiesClientPacket getEntitiesPacket) {

		EntityInfo[] entities = this.activeEntities.values().toArray(new EntityInfo[0]);

		try {
			sendPacket(new GetEntitiesServerPacket(entities), getEntitiesPacket.getClientIDFrom());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
