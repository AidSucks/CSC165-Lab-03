package myGame.networking.client;

import java.util.Map.Entry;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import myGame.Bullet;
import myGame.MyGame;
import myGame.networking.EntityType;
import tage.ObjShape;

public class BulletManager {
	
	private ObjShape bulletShape;
	private ConcurrentHashMap<UUID, Bullet> bullets;
	private GameClient hostClient;

	public BulletManager(GameClient hostClient, ObjShape bulletShape)
	{
		this.bullets = new ConcurrentHashMap<>();
		this.hostClient = hostClient;
		this.bulletShape = bulletShape;
	}

	public void updateServer(UUID id, Vector3f loc)
	{
		Bullet b = this.bullets.get(id);

		if(b == null) return;

		b.getPhysicsObject().setLocation(new float[]{loc.x, loc.y, loc.z});
	}

	public void updateLocal(float deltaTime) 
	{
		for(Entry<UUID, Bullet> entry : bullets.entrySet()) {
			
			Bullet b = entry.getValue();
			
			if(b.isFromServer()) continue;

			b.update(deltaTime);

			if(this.hostClient == null) continue;

			this.hostClient.sendMove(
				b.getID(), 
				b.getWorldLocation(), 
				new Quaternionf(), 
				EntityType.BULLET, 
				"NONE"
			);
		}
	}

	public void addBullet(Vector3f loc, Vector3f dir, boolean fromServer) 
	{
		Bullet b = new Bullet(loc, dir, this, bulletShape);
		b.setFromServer(fromServer);

		this.bullets.put(b.getID(), b);

		if(this.hostClient == null || fromServer) return;

		this.hostClient.sendCreateBullet(b);
	}

	public void addBullet(UUID id, Vector3f loc, Vector3f dir, boolean fromServer)
	{
		Bullet b = new Bullet(id, loc, dir, this, bulletShape);
		b.setFromServer(fromServer);

		this.bullets.put(b.getID(), b);

		if(this.hostClient == null || fromServer) return;

		this.hostClient.sendCreateBullet(b);
	}

	public void removeBullet(UUID id) 
	{
		Bullet b = this.bullets.get(id);

		if(b == null) return;

		this.bullets.remove(id);
		MyGame.getEngine().getSceneGraph().removeGameObject(b);
		MyGame.getEngine().getSceneGraph().removePhysicsObject(b.getPhysicsObject());

		if(this.hostClient == null || b.isFromServer()) return;

		this.hostClient.sendDeleteBullet(b);
	}
}
