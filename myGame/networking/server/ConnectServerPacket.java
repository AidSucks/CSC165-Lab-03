package myGame.networking.server;

public class ConnectServerPacket extends GameServerPacket {
	
	private boolean isHost;

	public ConnectServerPacket(boolean isHost) {
		this.isHost = isHost;
	}

	public boolean getIsHost () { return this.isHost; }
}
