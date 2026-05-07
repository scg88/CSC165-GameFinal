import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> 
{
	private NPCcontroller npcCtrl;
	private boolean first = true;

	public GameServerUDP(int localPort, NPCcontroller npc) throws IOException 
	{	super(localPort, ProtocolType.UDP);
		this.npcCtrl = npc;
	}

	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort)
	{
		String message = (String)o;
		String[] messageTokens = message.split(",");
		
		if(messageTokens.length > 0)
		{	// JOIN -- Case where client just joined the server
			// Received Message Format: (join,localId)
			if(messageTokens[0].compareTo("join") == 0)
			{	try 
				{	IClientInfo ci;					
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					if(first)
					{
						sendJoinedMessage(clientID, true, true);
						first = false;
					}
					else
						sendJoinedMessage(clientID, true, false);
				} 
				catch (IOException e) 
				{	e.printStackTrace();
			}	}
			
			// BYE -- Case where clients leaves the server
			// Received Message Format: (bye,localId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			}
			
			// CREATE -- Case where server receives a create message (to specify avatar location)
			// Received Message Format: (create,localId,x,y,z)
			if(messageTokens[0].compareTo("create") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendCreateMessages(clientID, pos);
				sendWantsDetailsMessages(clientID);
			}
			
			// DETAILS-FOR --- Case where server receives a details for message
			// Received Message Format: (dsfr,remoteId,localId,x,y,z)
			if(messageTokens[0].compareTo("dsfr") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				UUID remoteID = UUID.fromString(messageTokens[2]);
				String[] pos = {messageTokens[3], messageTokens[4], messageTokens[5]};
				sendDetailsForMessage(clientID, remoteID, pos);
			}
			
			// MOVE --- Case where server receives a move message
			// Received Message Format: (move,localId,x,y,z,pieceID)
			if(messageTokens[0].compareTo("move") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				String pieceID = messageTokens[5];
				sendMoveMessages(clientID, pos, pieceID);
			}

			// --- PROFESSOR'S NPC ADDITIONS ---
            // Message Format: (needNPC, localId)
        	if(messageTokens[0].equals("needNPC")) {
            	if(messageTokens.length > 1) {
                	UUID clientID = UUID.fromString(messageTokens[1]);
                	sendNPCstart(clientID); 
                	System.out.println("NPC start data sent to: " + clientID);
            	}
        	}

            // 2. Client responding that they ARE close to the NPC
        	// Message Format: (isnear, localId)
        	if(messageTokens[0].equals("isnear")) {
            	if(messageTokens.length > 1) {
                	//UUID clientID = UUID.fromString(messageTokens[1]);
                	handleNearTiming(null);
            	}
        	}
	}	}	

	public void handleNearTiming(UUID clientID) { 
        npcCtrl.setNearFlag(true); 
    }

	public int getClientCount() {
        // This returns the number of active players in the server's map
        return getClients().size(); 
    }

	// CLASS EXAMPLE - SENDING NPC MESSAGES
	// informs client of the wherabouts of the NPC
	public void sendCheckForAvatarNear() {
        try {
            String message = "isnr," + npcCtrl.getNPC().getX() + "," 
                           + npcCtrl.getNPC().getY() + "," 
                           + npcCtrl.getNPC().getZ() + "," 
                           + npcCtrl.getCriteria();
            sendPacketToAll(message);
        } catch (IOException e) { e.printStackTrace(); }
    }

	public void sendNPCInfo(double x, double y, double z, double s, double a) {
    	String message = "npcInfo,0," + x + "," + y + "," + z + "," + s + "," + a;
    
    	// Check if we have anyone to talk to
    	if (getClients() == null || getClients().isEmpty()) return;

    	// Use the keySet to loop through all connected UUIDs
    	for (Object clientID : getClients().keySet()) {
        	try {
            	sendPacket(message, (UUID)clientID);
        	} catch (IOException e) {
            	// If one packet fails, we don't care, the next tick (25ms) will fix it
        	} catch (Exception e) {
            	// Catch-all for any weird thread-access issues
        	}
 		}
	}


    public void sendNPCstart(UUID clientID) {
        String[] pos = { ""+npcCtrl.getNPC().getX(), ""+npcCtrl.getNPC().getY(), ""+npcCtrl.getNPC().getZ() };
        sendCreateNPCmsg(clientID, pos);
    }

    public void sendCreateNPCmsg(UUID clientID, String[] position) {
        try {
            String message = "createNPC," + "0," + position[0] + "," + position[1] + "," + position[2];
            sendPacket(message, clientID);
        } catch (IOException e) { e.printStackTrace(); }
    }

	// Informs the client who just requested to join the server if their if their 
	// request was able to be granted. 
	// Message Format: (join,success,first), (join,success,second), (join,failure,first), or (join,failure,second)
	
	public void sendJoinedMessage(UUID clientID, boolean success, boolean first)
	{	try 
		{	System.out.println("trying to confirm join");
			String message = new String("join,");
			if(success)
				message += "success,";
			else
				message += "failure,";
			if(first)
				message += "first";
			else
				message += "second";
			sendPacket(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that the avatar with the identifier remoteId has left the server. 
	// This message is meant to be sent to all client currently connected to the server 
	// when a client leaves the server.
	// Message Format: (bye,remoteId)
	
	public void sendByeMessages(UUID clientID)
	{	try 
		{	String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that a new avatar has joined the server with the unique identifier 
	// remoteId. This message is intended to be send to all clients currently connected to 
	// the server when a new client has joined the server and sent a create message to the 
	// server. This message also triggers WANTS_DETAILS messages to be sent to all client 
	// connected to the server. 
	// Message Format: (create,remoteId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessages(UUID clientID, String[] position)
	{	try 
		{	String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client of the details for a remote clients avatar. This message is in response 
	// to the server receiving a DETAILS_FOR message from a remote client. That remote clients 
	// messages localId becomes the remoteId for this message, and the remote clients messages 
	// remoteId is used to send this message to the proper client. 
	// Message Format: (dsfr,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID clientID, UUID remoteId, String[] position)
	{	try 
		{	String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	
			sendPacket(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a local client that a remote client wants the local clients avatars information. 
	// This message is meant to be sent to all clients connected to the server when a new client 
	// joins the server. 
	// Message Format: (wsds,remoteId)
	
	public void sendWantsDetailsMessages(UUID clientID)
	{	try 
		{	String message = new String("wsds," + clientID.toString());	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that a remote clients avatar has changed position. x, y, and z represent 
	// the new position of the remote avatar. This message is meant to be forwarded to all clients
	// connected to the server when it receives a MOVE message from the remote client.   
	// Message Format: (move,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessages(UUID clientID, String[] position, String pieceID)
	{	try 
		{	String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + pieceID;
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
}
