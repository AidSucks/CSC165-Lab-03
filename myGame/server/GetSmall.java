package myGame.server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class GetSmall extends BTAction {
    private NPC npc;

    public GetSmall(NPC npc) {
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npc.getSmall();
        return BTStatus.BH_SUCCESS;
    }
}