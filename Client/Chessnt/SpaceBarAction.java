package Chessnt;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

// REMOVED FOR FINAL GAME - used for win condition after taking photos from A2 assignment.

public class SpaceBarAction extends AbstractInputAction {
    private MyGame game;
	private boolean move;
	private ProtocolClient protClient;

    public SpaceBarAction(MyGame g, ProtocolClient p) {
        game = g;
		protClient = p;
    }

    @Override
    public void performAction(float time, Event e) {
        // Only trigger on key press (value > 0.5)
        if (e.getValue() < 0.5f) return;
		
		if(!game.getIsGameDone())
		{
			if(game.getTurn() || game.getGhostManager().isGhostAvatar())
			{
				move = game.getBoard().movePlayerPiece(game.getAvatar());
				if (move)
				{
					game.setHUDMessage("Move confirmed!");

					// Send move message to server and update avatar position
					protClient.sendMoveMessage(game.getAvatar().getWorldLocation(), game.getPieceId());
					game.getAvatar().getPhysicsObject().setLocation((new float[]{game.getAvatar().getPhysicsObject().getLocation().x(),
					game.getAvatar().getPhysicsObject().getLocation().y()+5f, game.getAvatar().getPhysicsObject().getLocation().z()}));
					game.setRunning(true);
					//game.toggleTurn();
					//System.out.println("Value is: " + game.getTurn());
					
				} else {
					game.setHUDMessage("Invalid move. Try again.");
				}
			} else {
				game.setHUDMessage("Not your turn! Please wait.");
			}
		}
    }
}