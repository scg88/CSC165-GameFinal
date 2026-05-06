package a2;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;

public class GhostManager
{
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
	private Vector<GhostNPC> ghostNPCs = new Vector<GhostNPC>(); // Added for NPC requirement

	public GhostManager(VariableFrameRateGame vfrg)
	{	
		game = (MyGame)vfrg;
	}
	
	public void createGhostAvatar(UUID id, Vector3f position) throws IOException
	{	
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(game, id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(0.25f);
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
	}
	
	public void removeGhostAvatar(UUID id)
	{	
		GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		{	
			game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		}
		else
		{	
			System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	private GhostAvatar findAvatar(UUID id)
	{	
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext())
		{	
			ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0)
			{	
				return ghostAvatar;
			}
		}		
		return null;
	}

	// NPC related methods
	public void createGhostNPC(UUID id, Vector3f position) {
        System.out.println("Adding ghost NPC with ID --> " + id);
        
		// Instead of creating a new object, get the one already in your game
    	GameObject sentinel = game.getNPC();

		if (sentinel != null) {
        	System.out.println("Sentinel linked to Server NPC ID: " + id);
            
            // Move your existing sentinel to the server's starting coordinates
            sentinel.setLocalTranslation(new Matrix4f().translation(position.x(), position.y(), position.z()));
            
            // Set initial scale for the milestone
            sentinel.setLocalScale((new Matrix4f()).scaling(0.7f));
    	} else {
        	System.out.println("Error: Sentinel object in MyGame is NULL!");
    	}
		
    }

	public void updateGhostNPC(UUID id, Vector3f position, double gsize, double gangle) {
	
    	GameObject sentinel = game.getNPC();
    
    	if (sentinel != null) {
        	// 1. Update Position using Matrix translation
        	sentinel.setLocalTranslation(new Matrix4f().translation(position.x(), position.y(), position.z()));
        
        	// 2. Update Scale (Visual feedback for Behavior Tree)
        	float s = (float)gsize;
        	sentinel.setLocalScale(new Matrix4f().scaling(s, s, s));

			// 3. Update Rotation (The 360 Spin)
        	// Convert the degrees from the server into radians for JOML
        	float rad = (float)java.lang.Math.toRadians(gangle);
        	// Apply rotation around the Y-axis
        	sentinel.setLocalRotation(new Matrix4f().rotationY(rad));
    	}
	}

	private GhostNPC findNPC(UUID id) {
        for (GhostNPC npc : ghostNPCs) {
            if (npc.getID().equals(id)) {
                return npc;
            }
        }
        return null;
    }

	public void removeGhostNPC(UUID id) {
    	GhostNPC npc = findNPC(id);
    	if (npc != null) {
        	game.getEngine().getSceneGraph().removeGameObject(npc);
        	ghostNPCs.remove(npc);
    	}
	}

	public void setNPCsize(UUID id, boolean big) {
        GhostNPC npc = findNPC(id);
        if (npc != null) { npc.setSize(big); }
    }
	
	public void updateGhostAvatar(UUID id, Vector3f position, int pieceID)
	{	
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null)
		{	
			ghostAvatar.setPosition(position, pieceID);
		}
		else
		{	
			System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}
	
	public boolean isGhostAvatar()
	{
		return ghostAvatars.isEmpty();
	}
}
