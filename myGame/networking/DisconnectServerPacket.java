package myGame.networking;

public class DisconnectServerPacket extends GameServerPacket {
	
	private boolean isDisconnected;

	public DisconnectServerPacket(boolean success) {
		this.isDisconnected = success;
	}

	public boolean getIsDisconnected() { return this.isDisconnected; }
}
