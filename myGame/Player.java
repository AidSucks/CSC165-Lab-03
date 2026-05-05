package myGame;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;
import tage.physics.PhysicsObject;

public class Player extends GameObject
{
	private boolean physicsInitialized = false;

	private boolean isOnGround = false;

	private UUID uuid;

	public Player(ObjShape shape, TextureImage texture)
	{
		super(GameObject.root(), shape, texture);

		initializeObject();
	}

	private void initializeObject()
	{
		this.uuid = UUID.randomUUID();

		setLocalLocation(new Vector3f(0, 0, 0));
	}

	public void initializePhysics()
	{
		if(physicsInitialized) return;

		PhysicsObject physicsObject = MyGame.getEngine().getSceneGraph().addPhysicsCapsule(
			0.5f,
			new Vector3f(0, 20f, 0),
			new Quaternionf().rotationAxis((float) (Math.PI / 2), new Vector3f(0, 0, 1)),
			0,
			0.25f, 
			0.5f
		);

		physicsObject.getRigidBody().setAngularFactor(new com.jme3.math.Vector3f(0, 0, 0));

		setPhysicsObject(physicsObject);

		physicsInitialized = true;
	}

	public UUID getUUID() { return this.uuid; }

	public void setIsOnGround(boolean b) { this.isOnGround = b; }

	public boolean isOnGround() { return this.isOnGround; }

	public void moveAlongForward(float accel)
	{
		final float maxSpeed = 3f;

		PhysicsObject po = getPhysicsObject();

		com.jme3.math.Vector3f linearVelocity = new com.jme3.math.Vector3f();

		po.getRigidBody().getLinearVelocity(linearVelocity);
		
		float reduceFactor = Math.clamp((2 * maxSpeed - linearVelocity.length()) / (2 * maxSpeed), 0, 1);

		Vector3f worldForward = getWorldForwardVector();

		Vector3f forward = new Vector3f(worldForward).mul(accel * (float) MyGame.getDeltaTime() * reduceFactor);

		po.applyForce(forward.x(), forward.y(), forward.z(), 0, 0, 0);
	}

	public void jump(float impulseStrength)
	{
		PhysicsObject po = getPhysicsObject();

		po.applyImpulse(0, impulseStrength, 0, 0, 0, 0);
	}

}
