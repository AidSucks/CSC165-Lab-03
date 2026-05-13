package myGame.networking.server;

import org.joml.Vector3f;
import myGame.networking.EntityType;
import java.util.UUID;
import org.joml.Quaternionf;


public class CreateEntityServerPacket extends GameServerPacket {
	
	private UUID entityID;
	private Vector3f position;
	private Quaternionf rotation;
	private EntityType entityType;
	private String animationState;
	private float entityScale;

	public CreateEntityServerPacket(
		UUID entityID,
		Vector3f position, 
		Quaternionf rotation, 
		EntityType type,
		String animationState,
		float entityScale
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
	public float getEntityScale() { return this.entityScale; }
}
