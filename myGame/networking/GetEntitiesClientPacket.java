package myGame.networking;

import java.util.UUID;

public class GetEntitiesClientPacket extends GameClientPacket {

	public GetEntitiesClientPacket(UUID clientIDFrom) {
		super(clientIDFrom);
	}
	
}
