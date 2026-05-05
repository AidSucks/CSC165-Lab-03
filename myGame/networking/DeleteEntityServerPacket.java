package myGame.networking;

import java.util.UUID;

public class DeleteEntityServerPacket extends GameServerPacket {
	
	private UUID entityID;
	private EntityType entityType;

	public DeleteEntityServerPacket(UUID entityID, EntityType type) {
		this.entityID = entityID;
		this.entityType = type;
	}

	public UUID getEntityID() { return this.entityID; }
	public EntityType getEntityType() { return this.entityType; }
}
