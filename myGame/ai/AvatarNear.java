package myGame.ai;

import org.joml.Vector3f;

import myGame.networking.EntityInfo;
import myGame.networking.EntityType;
import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition {

    private final NPC npc;
	private EntityInfo[] entities;

	private final float nearDistance = 2f;

    public AvatarNear(NPC npc, boolean toNegate) {
        super(toNegate);
        this.npc = npc;
    }

	public void setEntities(EntityInfo[] entities) {
		this.entities = entities;
	}

    @Override
    protected boolean check() {

		if(this.entities == null) return false;

		for(EntityInfo entity : this.entities) {
			if(entity.type == EntityType.ENEMY) continue;

			Vector3f p = new Vector3f(entity.position);
			Vector3f n = npc.getLocation();

			float dist = Vector3f.distance(p.x, p.y, p.z, n.x, n.y, n.z);

			if(dist <= nearDistance) {
				npc.setTarget(entity);
				return true;
			}
		}

		return false;
    }
	
}