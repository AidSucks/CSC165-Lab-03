package myGame;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class FwdAction extends AbstractInputAction {

    private Player player;
	
    public FwdAction(MyGame g)
	{
        this.player = g.getAvatar();
    }

     @ Override
    public void performAction(float time, Event e) {

		final float moveSpeed = 75f;

        // key forward
        if (e.getComponent().getName().equals("W")) {
			player.moveAlongForward(moveSpeed);
        }

        // key backward
        if (e.getComponent().getName().equals("S")) {
			player.moveAlongForward(-moveSpeed);
        }
    }
}
