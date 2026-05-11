package myGame;

import tage.audio.Sound;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class LightAction extends AbstractInputAction {

    private Player player;
	private MyGame game;
	
    public LightAction(MyGame g)
	{
		this.game = g;
        this.player = g.getAvatar();
    }

    @Override
    public void performAction(float time, Event e) {
		game.lightToggle();
    }
}
