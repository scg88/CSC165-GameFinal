import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer 
{
	private GameServerUDP thisUDPServer;
	private GameServerTCP thisTCPServer;
	private NPCcontroller npcCtrl; // Added for CSC165 NPC requirement

	public NetworkingServer(int serverPort, String protocol) 
	{	
		// 1. Initialize the NPC Controller
        npcCtrl = new NPCcontroller();

		try 
		{	if(protocol.toUpperCase().compareTo("TCP") == 0)
			{	thisTCPServer = new GameServerTCP(serverPort);
			}
			else
			{	thisUDPServer = new GameServerUDP(serverPort, npcCtrl);
				npcCtrl.start(thisUDPServer); // Start the NPC controller with the UDP server
			}
		} 
		catch (IOException e) 
		{	e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{	if(args.length > 1)
		{	NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
		else 
        {	System.out.println("Usage: java NetworkingServer <port> <protocol>");
        }
	}

}
