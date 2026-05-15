package myGame;

import org.joml.Vector3f;

import myGame.networking.client.BulletManager;
import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class ShootAction extends AbstractInputAction {

	private final GameObject avatar;
	private final BulletManager bulletManager;

	private long timeLastShot = 0;
	private long currTime = 0;

	private final float reloadTime = 0.5f;

	public ShootAction(GameObject avatar, BulletManager bulletManager) {
		this.avatar = avatar;
		this.bulletManager = bulletManager;
	}

	@Override
	public void performAction(float deltaTime, Event event)
	{
		if(this.avatar == null) return;

		this.currTime = System.currentTimeMillis();

		float timeSinceLastShot = (float) (currTime - timeLastShot) / 1000f;

		if(timeSinceLastShot >= reloadTime) {
			Vector3f forward = this.avatar.getWorldForwardVector();
			Vector3f startLoc = this.avatar.getWorldLocation().add(new Vector3f(forward).mul(1f));

			this.bulletManager.addBullet(startLoc, forward, false);

			timeLastShot = this.currTime;
		}
	}
	
}
