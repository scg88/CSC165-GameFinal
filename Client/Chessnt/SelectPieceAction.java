package Chessnt;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class SelectPieceAction extends AbstractInputAction {
    private MyGame game;

    public SelectPieceAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        if (e.getValue() < 0.5f) return; // Safety check for JInput
    
        int currentId = game.getSelectedId();
        currentId = (currentId == 15) ? 0 : currentId + 1;
    
        game.setSelectedId(currentId);
    
        ChessPiece newAvatar = (ChessPiece) game.getPlayerPieces()[currentId];
        game.setAvatar(newAvatar);
    
        System.out.println("Switched to piece: " + currentId);
}
}