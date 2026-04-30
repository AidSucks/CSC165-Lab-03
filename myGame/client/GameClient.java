package myGame.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

import org.joml.*;

import myGame.MyGame;
import tage.networking.client.GameConnectionClient;

public class GameClient extends GameConnectionClient {

	private UUID clientUUID;
	private MyGame game;
	private GhostManager ghostManager;
	private EnemyManager enemyManager;

	public GameClient(InetAddress remoteAddr, int remotePort, MyGame game) throws IOException 
	{
		super(remoteAddr, remotePort, ProtocolType.UDP);

		this.clientUUID = UUID.randomUUID();
		this.game = game;
		this.ghostManager = new GhostManager(this.game);
		this.enemyManager = new EnemyManager(this.game);
	}

	@Override
	public void processPacket(Object object)
	{
		String packet = (String) object;

		String[] tokens = packet.split(";");

		if(tokens.length <= 0) return;

		String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

		if(tokens[0].equalsIgnoreCase("join")) {
			runJoin(args);
		}
		else if(tokens[0].equalsIgnoreCase("leave")) {
			runLeave(args);
		}
		else if(tokens[0].equalsIgnoreCase("create")) {
			runCreate(args);
		}
		else if(tokens[0].equalsIgnoreCase("detail-request")) {
			runDetailRequest(args);
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
		// ++++++++++++++++++++++++++++++++++ NPC ++++++++++++++++++++++++++++++++++
		else if(tokens[0].equalsIgnoreCase("createNPC")) {
			runCreateNPC(args);
		}
		else if(tokens[0].equalsIgnoreCase("mnpc")) {
			runMoveNPC(args);
		}
	}

	public void joinServer()
	{
		try {
			
			sendPacket(String.join(";", "join", this.clientUUID.toString()));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void leaveServer()
	{
		try {

			sendPacket(String.join(";", "leave", this.clientUUID.toString()));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendMove(Vector3f position)
	{
		try {

			sendPacket(
				String.join(";", 
				"move", 
				this.clientUUID.toString(),
				String.valueOf(position.x),
				String.valueOf(position.y),
				String.valueOf(position.z)
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	private void runJoin(String[] args) 
	{
		if(!args[0].equalsIgnoreCase("true"))
			return;

		try {

			Vector3f spawnLoc = new Vector3f(0, 0, 0);

			String xS = String.valueOf(spawnLoc.x);
			String yS = String.valueOf(spawnLoc.y);
			String zS = String.valueOf(spawnLoc.z);

			sendPacket(String.join(";", "spawn", clientUUID.toString(), xS, yS, zS));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void runLeave(String[] args)
	{
		UUID ghostID = UUID.fromString(args[0]);

		ghostManager.removeGhost(ghostID);
	}

	// Runs upon another player joining
	private void runCreate(String[] args)
	{
		UUID newGhostID = UUID.fromString(args[0]);

		Vector3f spawnPosition = new Vector3f(
			Float.parseFloat(args[1]),
			Float.parseFloat(args[2]),
			Float.parseFloat(args[3])
		);

		ghostManager.createGhost(newGhostID, spawnPosition);
	}

	private void runDetailRequest(String[] args)
	{
		Vector3f playerPos = game.getAvatar().getWorldLocation();

		try {

			sendPacket(String.join(
				";", 
				"detail-for", 
				args[0], 
				this.clientUUID.toString(),
				String.valueOf(playerPos.x),
				String.valueOf(playerPos.y),
				String.valueOf(playerPos.z)
			));

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// Runs upon this player joining and receiving other players' locations
	private void runDetailFor(String[] args)
	{
		UUID newGhostID = UUID.fromString(args[0]);

		Vector3f spawnPosition = new Vector3f(
			Float.parseFloat(args[1]),
			Float.parseFloat(args[2]),
			Float.parseFloat(args[3])
		);

		ghostManager.createGhost(newGhostID, spawnPosition);
	}

	private void runMove(String[] args)
	{
		UUID ghostID = UUID.fromString(args[0]);

		Vector3f nextPosition = new Vector3f(
			Float.parseFloat(args[1]),
			Float.parseFloat(args[2]),
			Float.parseFloat(args[3])
		);

		ghostManager.updateGhost(ghostID, nextPosition);
	}
	
	// ++++++++++++++++++++++++++++++++++ Enemy ++++++++++++++++++++++++++++++++++
	
	public void sendEnemyCreate(UUID enemyID, Vector3f position)
	{
		try {
			sendPacket(String.join(";",
				"enemy-create",
				enemyID.toString(),
				String.valueOf(position.x),
				String.valueOf(position.y),
				String.valueOf(position.z)
			));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendEnemyMove(UUID enemyID, Vector3f position)
	{
		try {
			sendPacket(String.join(";",
				"enemy-move",
				enemyID.toString(),
				String.valueOf(position.x),
				String.valueOf(position.y),
				String.valueOf(position.z)
			));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void sendEnemyDelete(UUID enemyID)
	{
		try {
			sendPacket(String.join(";",
				"enemy-delete",
				enemyID.toString()
			));
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void runEnemyCreate(String[] args)
	{
		UUID enemyID = UUID.fromString(args[0]);

		Vector3f spawnPosition = new Vector3f(
			Float.parseFloat(args[1]),
			Float.parseFloat(args[2]),
			Float.parseFloat(args[3])
		);

		enemyManager.createEnemy(enemyID, spawnPosition, 1.0f);
	}

	private void runEnemyMove(String[] args)
	{
		UUID enemyID = UUID.fromString(args[0]);

		Vector3f nextPosition = new Vector3f(
			Float.parseFloat(args[1]),
			Float.parseFloat(args[2]),
			Float.parseFloat(args[3])
		);

		enemyManager.updateEnemy(enemyID, nextPosition, 1.0f);
	}

	private void runEnemyDelete(String[] args)
	{
		UUID enemyID = UUID.fromString(args[0]);
		enemyManager.removeEnemy(enemyID);
	}
	
	public GhostManager getGhostManager() {
		return ghostManager;
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
		float size = Float.parseFloat(args[4]);

		float y = game.getTerrain().getHeight(x, z);


		Vector3f npcPos = new Vector3f(x, y, z);

		enemyManager.createEnemy(npcID, npcPos, size);
		checkAvatarNearNPC(npcPos);
	}
	
	private void runMoveNPC(String[] args) {
		UUID npcID = UUID.fromString(args[0]);
		
		float x = Float.parseFloat(args[1]);
		float z = Float.parseFloat(args[3]);
		float size = Float.parseFloat(args[4]);

		float y = game.getTerrain().getHeight(x, z);
		
		Vector3f npcPos = new Vector3f(x, y, z);


		enemyManager.updateEnemy(npcID, npcPos, size);
		checkAvatarNearNPC(npcPos);

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
				System.out.println("client sent isnear");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	

}