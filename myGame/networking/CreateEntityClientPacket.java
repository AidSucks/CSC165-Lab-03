package myGame.networking;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CreateEntityClientPacket extends GameClientPacket {

	private Vector3f position;
	private Quaternionf rotation;
	private EntityType entityType;

	public CreateEntityClientPacket(
		UUID clientIDFrom, 
		Vector3f position, 
		Quaternionf rotation, 
		EntityType type
	) {
		super(clientIDFrom);
		this.position = position;
		this.rotation = rotation;
		this.entityType = type;
	}

	public Vector3f getPosition() { return new Vector3f(position); }
	public Quaternionf getRotation() { return new Quaternionf(rotation); }
	public EntityType getEntityType() { return this.entityType; }
	
}
