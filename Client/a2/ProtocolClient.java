package a2;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.networking.client.GameConnectionClient;

public class ProtocolClient extends GameConnectionClient
{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	
	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException 
	{	super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	}
	
	public UUID getID() { return id; }
	
	@Override
	protected void processPacket(Object message)
	{	String strMessage = (String)message;
		//System.out.println("message received -->" + strMessage);
		String[] messageTokens = strMessage.split(",");
		
		// Game specific protocol to handle the message
		if(messageTokens.length > 0)
		{
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
			if(messageTokens[0].compareTo("join") == 0)
			{	if(messageTokens[1].compareTo("success") == 0)
				{	System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
					askForNPC(); // Ask the server to send us the NPC data when we first join
				}
				if(messageTokens[1].compareTo("failure") == 0)
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
			}	}
			
			// Handle BYE message
			// Format: (bye,remoteId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}
			
			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0))
			{	// create a new ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));

				try
				{	ghostManager.createGhostAvatar(ghostID, ghostPosition);
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
			}
			
			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0)
			{
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			}
			
			// Handle MOVE message
			// Format: (move,remoteId,x,y,z,pieceId)
			if (messageTokens[0].compareTo("move") == 0)
			{
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
				int pieceId = (int)Float.parseFloat(messageTokens[5]);
				
				ghostManager.updateGhostAvatar(ghostID, ghostPosition, pieceId);
			}
			
			// Handle CREATE NPC (Sent by server when a client joins)
        	// Format: (createNPC, npcID, x, y, z)
			if(messageTokens[0].compareTo("createNPC") == 0) {
            	// We don't need to parse '0' as a UUID! 
    			// Just extract the position tokens directly.
    			Vector3f pos = new Vector3f(
        			Float.parseFloat(messageTokens[2]),
     				Float.parseFloat(messageTokens[3]),
        			Float.parseFloat(messageTokens[4])
    			);
				
				UUID dummyNPCID = new UUID(0L, 0L);
    			// Call your method to spawn the ghost/NPC in MyGame
    			game.createGhostNPC(dummyNPCID, pos);
        	}

			// Handle NPC INFO (Sent by server every 25ms "tick")
        	// Format: (npcInfo, npcID, x, y, z, size, angle)
			if(messageTokens[0].compareTo("npcInfo") == 0) {
    			//System.out.println("Received NPC Update: " + messageTokens[2] + ", " + messageTokens[3]);
				// Server sends: [0]npcInfo, [1]id, [2]x, [3]y, [4]z, [5]size, [6]angle
    			if (messageTokens.length < 7) return; 
    			try {

					UUID dummyID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        			// Since we only have one NPC, we don't need to parse a UUID from the packet
        			// We just need the coordinates and size
        			Vector3f pos = new Vector3f(
            			Float.parseFloat(messageTokens[2]), // WAS [1], now [2] for X
    					Float.parseFloat(messageTokens[3]), // WAS [2], now [3] for Y
    					Float.parseFloat(messageTokens[4])  // WAS [3], now [4] for Z
        			);
        			
					double gsize = Double.parseDouble(messageTokens[5]); // Size
					
					double gangle = Double.parseDouble(messageTokens[6]); // Angle

        			// If your updateGhostNPC method REQUIRES a UUID, 
        			// you can pass null or a fixed ID since there's only one Sentinel
        			updateGhostNPC(dummyID, pos, gsize, gangle); 
        
    			} catch (Exception e) {
        			System.out.println("Error parsing NPC info: " + e.getMessage());
    			}
			}

			// 3. Handle IS NEAR (Server asking: "Are you close to the NPC?")
        	// Format: (isnr, x, y, z, range)
			if(messageTokens[0].compareTo("isnr") == 0) {
            	Vector3f npcPos = new Vector3f(
                	Float.parseFloat(messageTokens[1]),
                	Float.parseFloat(messageTokens[2]),
                	Float.parseFloat(messageTokens[3])
            	);
            	float range = Float.parseFloat(messageTokens[4]);
            
            	// Check distance between our local player and the NPC
            	float dist = game.getPlayerPosition().distance(npcPos);
            	if (dist < range) {
                	// Pass the player's unique ID to the helper or send it directly
        			sendIsNearMessage(game.getPieceId());
            	}
        	}

		}	
	}
	
	// ------------- GHOST NPC SECTION --------------
	private void createGhostNPC(UUID npcID, Vector3f position) {
    	ghostManager.createGhostNPC(npcID, position);
	}

	private void updateGhostNPC(UUID npcID, Vector3f position, double gsize, double gangle) {
		
		ghostManager.updateGhostNPC(npcID, position, gsize, gangle);

		//ghostManager.updateGhostNPC(npcID, position, gsize);
    	// Visual feedback for Behavior Tree: 1.0 is "small/normal", anything else is "big"
    	//boolean isBig = (gsize != 1.0);
    	//ghostManager.setNPCsize(npcID, isBig);
	}

	// The initial message from the game client requesting to join the 
	// server. localId is a unique identifier for the client. Recommend 
	// a random UUID.
	// Message Format: (join,localId)
	
	public void sendJoinMessage()
	{	try 
		{	sendPacket(new String("join," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the client is leaving the server. 
	// Message Format: (bye,localId)

	public void sendByeMessage()
	{	try 
		{	sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the clients Avatars position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position)
	{	try 
		{	String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the local avatar's position. The server then 
	// forwards this message to the client with the ID value matching remoteId. 
	// This message is generated in response to receiving a WANTS_DETAILS message 
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position)
	{	try 
		{	String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the local avatar has changed position.  
	// Message Format: (move,localId,x,y,z, pieceId) where x, y, and z represent the position and pieceId represents the piece to move.

	public void sendMoveMessage(Vector3f position, int pieceId)
	{	try 
		{	String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + pieceId;
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
		}	
	}

	// Tells the server we are close enough to trigger the Sentinel's behavior
	public void sendIsNearMessage(int id) {
		try {
        	// Format: isnear, [ID]
        	String message = "isnear," + id;
        	sendPacket(message);
    	} catch (IOException e) {
        	e.printStackTrace();
    }
	}

	// Crucial: This tells the server to send us the NPC data when we first join
	public void askForNPC() 
	{   try {
        sendPacket(new String("needNPC," + id.toString()));
    	} catch (IOException e) {
        e.printStackTrace();
    	}
	}
}
