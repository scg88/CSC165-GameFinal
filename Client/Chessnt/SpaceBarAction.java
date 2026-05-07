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

        // Neutralized for Final Project cleanup
        game.setHUDMessage("Space Bar Pressed - Ready for Chess logic!");
		
		if(!game.getIsGameDone())
		{
			if(game.getTurn() || game.getGhostManager().isGhostAvatar())
			{
				move = game.getBoard().movePlayerPiece(game.getAvatar());
				if (move)
				{
					protClient.sendMoveMessage(game.getAvatar().getWorldLocation(), game.getPieceId());
					game.toggleTurn();
					System.out.println("Value is: " + game.getTurn());
				}
			}
		}
    }
}