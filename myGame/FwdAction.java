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

		final float moveSpeed = 500f;
		float keyValue = e.getValue();
		
		if (keyValue >  - .3 && keyValue < .3) {
			return; // deadzone
		}
		
		// gamepad  backward
		if (keyValue >  - .3) {
			player.moveAlongForward(time * -moveSpeed);
		}
                // gamepad  forward
		if (keyValue < .3) {
			player.moveAlongForward(time * moveSpeed);
		}

        // key forward
        if (e.getComponent().getName().equals("W")) {
			player.moveAlongForward(time * moveSpeed);
        }

        // key backward
        if (e.getComponent().getName().equals("S")) {
			player.moveAlongForward(time * -moveSpeed);
        }

		if(footstepSound.getIsPlaying() || !player.isOnGround()) return;

		footstepSound.play();
    }
}
