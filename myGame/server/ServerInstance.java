package myGame.server;

import java.io.IOException;

public class ServerInstance {
	
	public ServerInstance(int serverPort)
	{
		try {
			new GameServer(serverPort);
		} catch(IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		if(args.length > 0) {
			new ServerInstance(Integer.parseInt(args[0]));
			System.out.println("Server started on port: " + args[0]);
		}
	}
}
