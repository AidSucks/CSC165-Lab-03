package myGame.server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class StopChasing extends BTAction {
    private NPC npc;

    public StopChasing(NPC npc) {
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npc.setChasing(false);
        return BTStatus.BH_SUCCESS;
    }
}