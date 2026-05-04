package myGame;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class JumpAction extends AbstractInputAction
{
	private Player player;

	public JumpAction(MyGame game)
	{
		this.player = game.getAvatar();
	}

	@Override
	public void performAction(float deltaTime, Event event)
	{
		if(!player.isOnGround()) return;

		player.jump(2f);

		player.setIsOnGround(false);
	}
	
}
