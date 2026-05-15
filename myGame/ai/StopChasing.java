package myGame.ai;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class StopChasing extends BTAction {
    
	private final NPC npc;

    public StopChasing(NPC npc) {
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npc.setTarget(null);
        return BTStatus.BH_SUCCESS;
    }
}