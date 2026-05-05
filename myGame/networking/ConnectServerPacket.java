package myGame.networking;

public class ConnectServerPacket extends GameServerPacket {
	
	private boolean connectionSuccess;

	public ConnectServerPacket(boolean connectionSuccess) {
		this.connectionSuccess = connectionSuccess;
	}

	public boolean getIsConnected () { return this.connectionSuccess; }
}
