package myGame.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;
import java.util.ArrayList;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID> {
	
	private class ServerEnemy {
		UUID id;
		String x, y, z;

		ServerEnemy(UUID id, String x, String y, String z) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	private ArrayList<ServerEnemy> activeEnemies = new ArrayList<>();
	
	NPCcontroller npcCtrl; 

	public GameServer(int localPort) throws IOException
	{
		super(localPort, ProtocolType.UDP);
		
		npcCtrl = new NPCcontroller(this);
        npcCtrl.start();
	}
	
	// public void sendCheckForAvatarNear() {
		// try {
			
			// String message = new String("isnr"); 
			// message += "," + (npcCtrl.getNPC()).getX(); 
			// message += "," + (npcCtrl.getNPC()).getY(); 
			// message += "," + (npcCtrl.getNPC()).getZ(); 
			// message += "," + (npcCtrl.getCriteria()); 
			
			// sendPacketToAll(message); 
		  // }  catch (IOException e){
			  // System.out.println("couldnt send msg"); e.printStackTrace(); } 
	// }
	


	@Override
	public void processPacket(Object object, InetAddress senderIP, int senderPort)
	{	
		String packet = (String) object;
		String[] tokens = packet.split(";");
		
		if(tokens.length <= 0) return;

		String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

		if(tokens[0].equalsIgnoreCase("join")) {
			runJoin(args, senderIP, senderPort);
		}
		else if(tokens[0].equalsIgnoreCase("leave")) {
			runLeave(args);
		}
		else if(tokens[0].equalsIgnoreCase("spawn")) {
			runSpawn(args);
		}
		else if(tokens[0].equalsIgnoreCase("detail-for")) {
			runDetailFor(args);
		}
		else if(tokens[0].equalsIgnoreCase("move")) {
			runMove(args);
		}
		// ++++++++++++++++++++++++++++++++++ Enemy ++++++++++++++++++++++++++++++++++
		else if(tokens[0].equalsIgnoreCase("enemy-create")) {
			runEnemyCreate(args);
		}
		else if(tokens[0].equalsIgnoreCase("enemy-move")) {
			runEnemyMove(args);
		}
		else if(tokens[0].equalsIgnoreCase("enemy-delete")) {
			runEnemyDelete(args);
		}
		// ++++++++++++++++++++++++++++++++++ npc ++++++++++++++++++++++++++++++++++
		else if(tokens[0].equalsIgnoreCase("spawnNPC")) {
			runSpawnNPC(args);
		}
		else if(tokens[0].equalsIgnoreCase("isnear")) {
			runIsNear(args);
		}
	}
	
	// IN: join;<uuid>
	// OUT: join;<status>
	private void runJoin(String[] args, InetAddress senderIP, int senderPort)
	{
		try {

			IClientInfo clientInfo;

			clientInfo = getServerSocket().createClientInfo(senderIP, senderPort);

			UUID clientID = UUID.fromString(args[0]);

			addClient(clientInfo, clientID);

			sendPacket(new String("join;true"), clientID);
			
			// sendNPCstart(clientID);
			sendExistingEnemiesToClient(clientID);

		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: leave;<uuid>
	// OUT: leave;<uuid>
	private void runLeave(String[] args)
	{
		try {

			UUID clientID = UUID.fromString(args[0]);

			removeClient(clientID);

			forwardPacketToAll(String.join(";", "leave", clientID.toString()), clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: spawn;<uuid>;<x>;<y>;<z>
	// OUT: create;<uuid>;<x>;<y>;<z>
	// OUT: detail-request;<uuid-to>
	private void runSpawn(String[] args)
	{
		try {

			UUID clientID = UUID.fromString(args[0]);

			String create = String.join(";", "create", clientID.toString(), args[1], args[2], args[3]);
			String detailRequest = String.join(";", "detail-request", clientID.toString());

			forwardPacketToAll(create, clientID);
			forwardPacketToAll(detailRequest, clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: detail-for;<uuid-to>;<uuid-from>;<x>;<y>;<z>
	// OUT: detail-for;<uuid-from>;<x>;<y>;<z>
	private void runDetailFor(String[] args)
	{
		try {

			UUID clientIDTo = UUID.fromString(args[0]);

			String detailFor = String.join(";", "detail-for", args[1], args[2], args[3], args[4]);

			sendPacket(detailFor, clientIDTo);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: move;<uuid>;<x>;<y>;<z>
	// OUT: move;<uuid>;<x>;<y>;<z>
	private void runMove(String[] args)
	{
		try {
			
			UUID clientID = UUID.fromString(args[0]);

			String move = String.join(";", "move", clientID.toString(), args[1], args[2], args[3]);

			forwardPacketToAll(move, clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	
	// ++++++++++++++++++++++++++++++++++ Enemy ++++++++++++++++++++++++++++++++++
	
	// IN: enemy-create;<enemyId>;<x>;<y>;<z>
	// OUT: enemy-create;<enemyId>;<x>;<y>;<z>
	private void runEnemyCreate(String[] args)
	{
		try {
			UUID enemyID = UUID.fromString(args[0]);

			activeEnemies.add(new ServerEnemy(enemyID, args[1], args[2], args[3]));

			String msg = String.join(";", "enemy-create",
				enemyID.toString(), args[1], args[2], args[3]);

			sendPacketToAll(msg);
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: enemy-move;<enemyId>;<x>;<y>;<z>
	// OUT: enemy-move;<enemyId>;<x>;<y>;<z>
	private void runEnemyMove(String[] args)
	{
		try {
			UUID enemyID = UUID.fromString(args[0]);

			for (ServerEnemy e : activeEnemies) {
				if (e.id.compareTo(enemyID) == 0) {
					e.x = args[1];
					e.y = args[2];
					e.z = args[3];
					break;
				}
			}

			String msg = String.join(";", "enemy-move",
				enemyID.toString(), args[1], args[2], args[3]);

			sendPacketToAll(msg);
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: enemy-delete;<enemyId>
	// OUT: enemy-delete;<enemyId>
	private void runEnemyDelete(String[] args)
	{
		try {
			 UUID enemyID = UUID.fromString(args[0]);

			
			activeEnemies.removeIf(e -> e.id.compareTo(enemyID) == 0);
			
			String msg = String.join(";", "enemy-delete", args[0]);
			sendPacketToAll(msg);
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	private void sendExistingEnemiesToClient(UUID clientID) {
		try {
			for (ServerEnemy e : activeEnemies) {
				String msg = String.join(";",
					"enemy-create",
					e.id.toString(),
					e.x, e.y, e.z
				);
				sendPacket(msg, clientID);
			}
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
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
				String.valueOf(npc.getYaw())
			);

			sendPacketToAll(message);
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
				String.valueOf(npc.getSize())
			);

			sendPacket(message, clientID);
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
				String.valueOf(npc.getSize())
			);

			sendPacketToAll(message);
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
