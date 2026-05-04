package myGame.server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class GetBig extends BTAction {
    private NPC npc;

    public GetBig(NPC npc) {
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
		// System.out.println("NPC GET BIG");
        npc.getBig();
        return BTStatus.BH_SUCCESS;
    }
}