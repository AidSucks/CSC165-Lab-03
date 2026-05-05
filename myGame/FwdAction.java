package myGame;

import tage.audio.Sound;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class FwdAction extends AbstractInputAction {

    private Player player;
	private Sound footstepSound;
	
    public FwdAction(MyGame g, Sound footstepSound)
	{
        this.player = g.getAvatar();
		this.footstepSound = footstepSound;
    }

    @Override
    public void performAction(float time, Event e) {

		final float moveSpeed = 100f;

        // key forward
        if (e.getComponent().getName().equals("W")) {
			player.moveAlongForward(moveSpeed);
        }

        // key backward
        if (e.getComponent().getName().equals("S")) {
			player.moveAlongForward(-moveSpeed);
        }

		if(footstepSound.getIsPlaying() || !player.isOnGround()) return;

		footstepSound.play();
    }
}
