package myGame.networking.client;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.networking.EntityType;

public class CreateEntityClientPacket extends GameClientPacket {

	private UUID entityID;
	private Vector3f position;
	private Quaternionf rotation;
	private EntityType entityType;
	private String animationState;
	private float entityScale;
	private Vector3f direction;

	public CreateEntityClientPacket(
		UUID clientIDFrom, 
		UUID entityID,
		Vector3f position, 
		Quaternionf rotation, 
		EntityType type,
		String animationState,
		float entityScale,
		Vector3f direction
	) {
		super(clientIDFrom);
		this.entityID = entityID;
		this.position = position;
		this.rotation = rotation;
		this.entityType = type;
		this.animationState = animationState;
		this.entityScale = entityScale;
		this.direction = direction;
	}

	public UUID getEntityID() { return this.entityID; }
	public Vector3f getPosition() { return new Vector3f(position); }
	public Quaternionf getRotation() { return new Quaternionf(rotation); }
	public EntityType getEntityType() { return this.entityType; }
	public String getAnimationState() { return this.animationState; }
	public float getEntityScale() { return this.entityScale; }
	public Vector3f getDirection() { return new Vector3f(this.direction); }
}
