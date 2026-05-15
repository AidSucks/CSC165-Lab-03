package myGame.ai;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import myGame.networking.EntityInfo;
import tage.GameObject;
import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;

public class NPC {

	private AvatarNear avatarNear;
	private AvatarNear avatarNearNeg;
	private final GameObject terrain;
	private BehaviorTree behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);

    private UUID id;
	private EntityInfo target;

	private Vector3f location;
	private Quaternionf rotation;

    private float size = 0.05f;
	private float speed = 0.09f;

	private String state = "IDLE";


    public NPC(GameObject terrain) {
		this.terrain = terrain;

		id = UUID.randomUUID();

		this.location = new Vector3f(0, terrain.getHeight(0, 0), 0);
		this.rotation = new Quaternionf();

		setupBehaviorTree();
    }

	private void setupBehaviorTree() {
        behaviorTree.insertAtRoot(new BTSequence(10));
        behaviorTree.insertAtRoot(new BTSequence(20));

        // // Sequence 10:
        // // if 1 second passed -> get small
        // bt.insert(10, new OneSecPassed(false));
        // bt.insert(10, new GetSmall(npc));

        // // Sequence 20:
        // // if avatar near -> get big
        // bt.insert(20, new AvatarNear(this, false));
        // bt.insert(20, new GetBig(npc));

		avatarNear = new AvatarNear(this, false);
		avatarNearNeg = new AvatarNear(this, true);
		
		// If avatar is near, chase avatar
		behaviorTree.insert(10, avatarNear);

		// If avatar is NOT near for 1 second, stop chasing
		behaviorTree.insert(20, avatarNearNeg);
		behaviorTree.insert(20, new OneSecPassed(false));
		behaviorTree.insert(20, new StopChasing(this));
    }

	public void setEntities(EntityInfo[] entities) {
		avatarNear.setEntities(entities);
		avatarNearNeg.setEntities(entities);
	}

    public UUID getID() { return id; }

	public Vector3f getLocation() { return new Vector3f(this.location); }

	public boolean hasTarget() { return this.target != null; }
	
    public float getSize() { return size; }

	public String getState() { return state; }

	public Quaternionf getRotation() { return new Quaternionf(this.rotation); }

	public BehaviorTree getBehaviorTree() { return this.behaviorTree; }
	
	public void setTarget(EntityInfo entity) {
		this.target = entity;
	}
	
	public void updateLocation() {
		
		if (hasTarget()) {
			float dx = target.position.x - location.x;
			float dz = target.position.z - location.z;

			float dist = (float) Math.sqrt(dx * dx + dz * dz);
			
			if (dist > 0.1) {
				state = "WALK";
				
				// move
				this.location.add(new Vector3f((dx / dist) * speed, 0, (dz / dist) * speed));
				this.location.setComponent(1, terrain.getHeight(location.x, location.z));
				
				// Rotation
			} else {
				state = "ATTACK";
			}
		} else {
			state = "IDLE";
		}

	}
}