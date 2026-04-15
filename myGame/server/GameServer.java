package myGame.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID> {

	public GameServer(int localPort) throws IOException
	{
		super(localPort, ProtocolType.UDP);
	}

	@Override
	public void processPacket(Object object, InetAddress senderIP, int senderPort)
	{	
		String packet = (String) object;
		String[] tokens = packet.split(";");
		
		if(tokens.length <= 0) return;

		String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

		if(tokens[0].equalsIgnoreCase("join")) {
			runJoin(args, senderIP, senderPort);
		}
		else if(tokens[0].equalsIgnoreCase("leave")) {
			runLeave(args);
		}
		else if(tokens[0].equalsIgnoreCase("spawn")) {
			runSpawn(args);
		}
		else if(tokens[0].equalsIgnoreCase("detail-for")) {
			runDetailFor(args);
		}
		else if(tokens[0].equalsIgnoreCase("move")) {
			runMove(args);
		}
	}
	
	// IN: join;<uuid>
	// OUT: join;<status>
	private void runJoin(String[] args, InetAddress senderIP, int senderPort)
	{
		try {

			IClientInfo clientInfo;

			clientInfo = getServerSocket().createClientInfo(senderIP, senderPort);

			UUID clientID = UUID.fromString(args[0]);

			addClient(clientInfo, clientID);

			sendPacket(new String("join;true"), clientID);

		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: leave;<uuid>
	// OUT: leave;<status>
	private void runLeave(String[] args)
	{
		try {

			UUID clientID = UUID.fromString(args[0]);

			removeClient(clientID);

			sendPacket(new String("leave;true"), clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: spawn;<uuid>;<x>;<y>;<z>
	// OUT: create;<uuid>;<x>;<y>;<z>
	private void runSpawn(String[] args)
	{
		try {

			UUID clientID = UUID.fromString(args[0]);

			String create = String.join(";", "create", clientID.toString(), args[1], args[2], args[3]);
			String detailRequest = String.join(";", "detail-request", clientID.toString());

			forwardPacketToAll(create, clientID);
			forwardPacketToAll(detailRequest, clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: detail-for;<uuid-to>;<uuid-from>;<x>;<y>;<z>
	// OUT: detail-for;<uuid-from>;<x>;<y>;<z>
	private void runDetailFor(String[] args)
	{
		try {

			UUID clientIDTo = UUID.fromString(args[0]);

			String detailFor = String.join(";", "detail-for", args[1], args[2], args[3], args[4]);

			sendPacket(detailFor, clientIDTo);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	// IN: move;<uuid>;<x>;<y>;<z>
	// OUT: move;<uuid>;<x>;<y>;<z>
	private void runMove(String[] args)
	{
		try {
			
			UUID clientID = UUID.fromString(args[0]);

			String move = String.join(";", "move", clientID.toString(), args[1], args[2], args[3]);

			forwardPacketToAll(move, clientID);

		} catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}
}
