package myGame.networking;

import java.util.UUID;

public class DeleteEntityClientPacket extends GameClientPacket {

	private UUID entityID;
	private EntityType entityType;

	public DeleteEntityClientPacket(UUID clientIDFrom, UUID entityID, EntityType type) {
		super(clientIDFrom);
		this.entityID = entityID;
		this.entityType = type;
	}

	public UUID getEntityID() { return this.entityID; }
	public EntityType getEntityType() { return this.entityType; }	
}
