package myGame.server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class ChaseAvatar extends BTAction {
    private NPC npc;
    private NPCcontroller npcCtrl;

    public ChaseAvatar(NPC npc, NPCcontroller npcCtrl) {
        this.npc = npc;
        this.npcCtrl = npcCtrl;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npc.setTarget(
            npcCtrl.getAvatarX(),
            npcCtrl.getAvatarY(),
            npcCtrl.getAvatarZ()
        );

        npc.setChasing(true);
        // System.out.println("NPC chasing avatar");

        return BTStatus.BH_SUCCESS;
    }
}