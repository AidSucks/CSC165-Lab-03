package myGame.networking;

import java.util.Arrays;

public class GetEntitiesServerPacket extends GameServerPacket {

	private EntityInfo[] entities;

	public GetEntitiesServerPacket(EntityInfo[] entities) {
		this.entities = entities;
	}

	public EntityInfo[] getEntities() { return Arrays.copyOf(entities, entities.length); }
}
