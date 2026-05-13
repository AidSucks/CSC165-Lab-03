package myGame.networking.client;

import java.util.UUID;

public class GetEntitiesClientPacket extends GameClientPacket {

	public GetEntitiesClientPacket(UUID clientIDFrom) {
		super(clientIDFrom);
	}
	
}
