package myGame.networking;

import java.io.Serializable;
import java.util.UUID;

public abstract class GameClientPacket implements Serializable {
	
	private UUID clientIDFrom;

	public GameClientPacket(UUID clientIDFrom)
	{
		this.clientIDFrom = clientIDFrom;
	}

	public UUID getClientIDFrom() { return this.clientIDFrom; }
}