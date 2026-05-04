package myGame.server;

import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition {
    private NPCcontroller npcCtrl;

    public AvatarNear(NPCcontroller npcCtrl, boolean toNegate) {
        super(toNegate);
        this.npcCtrl = npcCtrl;
    }

    @Override
    protected boolean check() {
		boolean near = npcCtrl.isAvatarNear();
		// System.out.println("AvatarNear check = " + near);
		return near;
    }
	
}