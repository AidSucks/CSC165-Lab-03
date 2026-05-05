package myGame.networking;

import java.util.UUID;

public class DisconnectClientPacket extends GameClientPacket {
	
	public DisconnectClientPacket(UUID clientIDFrom) {
		super(clientIDFrom);
	}
}
