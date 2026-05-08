package myGame;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class PushAction extends AbstractInputAction
{
	private Player player;
	private MyGame game;

	public PushAction(MyGame game)
	{	
		this.game = game;
		this.player = game.getAvatar();
	}

	@Override
	public void performAction(float deltaTime, Event event)
	{
		
		System.out.printf("push action\n");

		float force = 60000.0f;
				System.out.printf("deltaTime %.2f \n", deltaTime);

		player.push(game.getEnemy(), deltaTime * force);
	}
	
}
