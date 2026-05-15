package myGame;

import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.networking.client.BulletManager;
import tage.GameObject;
import tage.ObjShape;
import tage.physics.PhysicsObject;

public class Bullet extends GameObject {

	private final BulletManager bulletManager;

	private boolean isFromServer = false;

	private final UUID id;

	private final Vector3f direction;

	private float expirationTimer = 0f;
	private final float expirationTime = 2.5f; // 5 S

	private final float speed = 10f;
	private final float scale = 0.0625f;

	public Bullet(Vector3f initialPosition, Vector3f direction, BulletManager bulletManager, ObjShape bulletShape) 
	{
		this(UUID.randomUUID(), initialPosition, direction, bulletManager, bulletShape);
	}

	public Bullet(UUID id, Vector3f initialPosition, Vector3f direction, BulletManager bulletManager, ObjShape bulletShape)
	{
		super(GameObject.root(), bulletShape);

		this.id = id;
		this.direction = direction.normalize();
		this.bulletManager = bulletManager;

		setLocalLocation(initialPosition);
		setLocalScale(new Matrix4f().scaling(scale));

		initializePhysics();
	}

	public void setFromServer(boolean b) { this.isFromServer = b; }

	public boolean isFromServer() { return this.isFromServer; }

	private void initializePhysics() 
	{
		PhysicsObject po = MyGame.getEngine().getSceneGraph().addPhysicsSphere(
			0.2f, 
			getLocalLocation(),
			new Quaternionf(),
			this.scale
		);

		po.getRigidBody().setGravity(new com.jme3.math.Vector3f(0, 0, 0));

		setPhysicsObject(po);
	}

	public UUID getID() { return this.id; }

	public Vector3f getDirection() { return new Vector3f(this.direction); }

	public void update(float deltaTime)
	{
		this.expirationTimer += deltaTime;

		if(this.expirationTimer >= this.expirationTime) {
			this.bulletManager.removeBullet(this.id);
			return;
		}

		PhysicsObject po = getPhysicsObject();

		Vector3f newLoc = po.getLocation();
		newLoc.add(getDirection().mul(speed * deltaTime));

		po.setLocation(new float[]{newLoc.x, newLoc.y, newLoc.z});
	}
}
