package myGame.networking;

import java.util.UUID;

import org.joml.Vector3f;

public class ConnectClientPacket extends GameClientPacket {
	
	private Vector3f spawnLocation;

	public ConnectClientPacket(
		UUID clientIDFrom,
		Vector3f spawnLocation
	) {
		super(clientIDFrom);
		this.spawnLocation = spawnLocation;
	}

	public Vector3f getSpawnLocation() { return new Vector3f(this.spawnLocation); }
}
