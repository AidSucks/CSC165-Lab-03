package myGame.networking.server;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.networking.EntityType;

public class UpdateEntityServerPacket extends GameServerPacket { 	

	private UUID entityID;
	private Vector3f position;
	private Quaternionf rotation;
	private EntityType entityType;
	private String animationState;

	public UpdateEntityServerPacket(
		UUID entityID,
		Vector3f position, 
		Quaternionf rotation, 
		EntityType type,
		String animationState
	) {
		this.entityID = entityID;
		this.position = position;
		this.rotation = rotation;
		this.entityType = type;
	}

	public UUID getEntityID() { return this.entityID; }
	public Vector3f getPosition() { return new Vector3f(position); }
	public Quaternionf getRotation() { return new Quaternionf(rotation); }
	public EntityType getEntityType() { return this.entityType; }
	public String getAnimationState() { return this.animationState; }
}
