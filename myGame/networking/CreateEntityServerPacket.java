package myGame.networking;

import java.util.UUID;

import org.joml.Vector3f;
import org.joml.Quaternionf;


public class CreateEntityServerPacket extends GameServerPacket {
	
	UUID entityID;
	Vector3f position;
	Quaternionf rotation;
	EntityType entityType;

	public CreateEntityServerPacket(
		UUID entityID, 
		Vector3f position,
		Quaternionf rotation,
		EntityType type
	) {
		this.entityID = entityID;
		this.position = position;
		this.rotation = rotation;
		this.entityType = type;
	}

	public UUID getEntityID() { return this.entityID; }
	public Vector3f getPosition() { return new Vector3f(this.position); }
	public Quaternionf getRotation() { return new Quaternionf(this.rotation); }
	public EntityType getEntityType() { return this.entityType; }
}
