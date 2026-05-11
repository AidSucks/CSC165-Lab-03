package myGame;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.lang.Math;

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
			new Vector3f(0, 5f, 0),
			new Quaternionf().rotationAxis((float) (Math.PI / 2), new Vector3f(0, 0, 1)),
			0,
			0.25f, 
			0.5f
		);

		physicsObject.getRigidBody().setAngularFactor(new com.jme3.math.Vector3f(0, 0, 0));
		physicsObject.disableSleeping(); 
		setPhysicsObject(physicsObject);

		physicsInitialized = true;
	}

	public UUID getUUID() { return this.uuid; }

	public void setIsOnGround(boolean b) { this.isOnGround = b; }

	public boolean isOnGround() { return this.isOnGround; }

	public void moveAlongForward(float speed)
	{
		// final float maxSpeed = 3f;

		// PhysicsObject po = getPhysicsObject();

		// com.jme3.math.Vector3f linearVelocity = new com.jme3.math.Vector3f();

		// po.getRigidBody().getLinearVelocity(linearVelocity);
		
		// float reduceFactor = Math.max(
			// 0f,
			// Math.min((2 * maxSpeed - linearVelocity.length()) / (2 * maxSpeed), 1f)
		// );

		// Vector3f worldForward = getWorldForwardVector();

		// // Vector3f forward = new Vector3f(worldForward).mul(accel * (float) MyGame.getDeltaTime() * reduceFactor);
		
		// Vector3f forward = new Vector3f(worldForward).mul(speed * maxSpeed);
		
		// System.out.printf("forward amount %.2f,%.2f,%.2f: \n", forward.x(), forward.y(), forward.z());
		// System.out.printf("forward print done\n");
		

		// po.applyForce(forward.x(), forward.y(), forward.z(), 0, 0, 0);
		
		// ================================================================
		
		PhysicsObject po = getPhysicsObject();
		// po.setDamping(0.2f, 0.2f);
		Vector3f forward = getWorldForwardVector();
		forward.y = 0;

		float[] oldVelocity = po.getLinearVelocity();
		// System.out.printf("oldVelocity amount %.2f,%.2f,%.2f: \n", oldVelocity[0], oldVelocity[1], oldVelocity[2]);
		
		float[] velocity = {
			forward.x() * speed,
			oldVelocity[1],          // keep jump / gravity velocity
			forward.z() * speed
		};
		// System.out.printf("velocity amount %.2f,%.2f,%.2f: \n", velocity[0], velocity[1], velocity[2]);

		po.setLinearVelocity(velocity);
	}

	public void jump(float impulseStrength)
	{
		PhysicsObject po = getPhysicsObject();

		po.applyImpulse(0, impulseStrength, 0, 0, 0, 0);
	}
	
	public void push(GameObject target, float force)
	{
		if (target == null || target.getPhysicsObject() == null) return;
		PhysicsObject playerPO = this.getPhysicsObject();
		PhysicsObject targetPO = target.getPhysicsObject();
		
		boolean touching = playerPO.getFullCollidedSet().contains(targetPO);
		if (!touching){
			System.out.println("no collision to target");
			return;
		}
		
		Vector3f playerPos = this.getWorldLocation();
		// System.out.printf("playerPos amount %.2f,%.2f,%.2f: \n", playerPos.x(), playerPos.y(), playerPos.z());
		
		Vector3f targetPos = target.getWorldLocation();
		// System.out.printf("targetPos amount %.2f,%.2f,%.2f: \n", targetPos.x(), targetPos.y(), targetPos.z());
		
		Vector3f pushDir = new Vector3f(targetPos).sub(playerPos);

		// System.out.printf("pushDir amount %.2f,%.2f,%.2f: \n", pushDir.x(), pushDir.y(), pushDir.z());
		targetPO.applyImpulse(pushDir.x() * force, 0.5f, pushDir.z() * force, 0, 0, 0);
		// System.out.printf("pushDir amount %.2f,%.2f,%.2f: \n", pushDir.x(), pushDir.y(), pushDir.z());

	}

}
