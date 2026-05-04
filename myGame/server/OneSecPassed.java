package myGame.server;

import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
    private long lastTime;

    public OneSecPassed(boolean toNegate) {
        super(toNegate);
        lastTime = System.nanoTime();
    }

    @Override
    protected boolean check() {
        long currentTime = System.nanoTime();
        float elapsedMs = (currentTime - lastTime) / 1_000_000.0f;

        if (elapsedMs >= 1000.0f) {
            lastTime = currentTime;
            return true;
        }

        return false;
    }
}