package myGame.client;

import java.util.ArrayList;
import java.util.UUID;
import org.joml.*;
import myGame.MyGame;
import tage.*;

public class EnemyManager {
    private MyGame game;
    private ArrayList<GhostEnemy> enemies = new ArrayList<>();

    public EnemyManager(MyGame g) {
        game = g;
    }

    public void createEnemy(UUID id, Vector3f p, float size, String state) {
        if (findEnemy(id) != null) return;

        GhostEnemy e = new GhostEnemy(id, game.getEnemyAnimatedShape(), game.getEnemyTexture(), p);
        e.setSize(size);
        // e.setLocalRotation(new Matrix4f().rotationY((float)Math.toRadians(180.0f)));
		e.playAnimation("IDLE");
        enemies.add(e);
    }

    public void updateEnemy(UUID id, Vector3f p, float size, float yawAmt, String state) {
        GhostEnemy e = findEnemy(id);
        if (e != null) {
            e.setLocalLocation(p);
			e.setSize(size);
			e.setYaw(yawAmt);
			e.playAnimation(state);
        }
    }

    public void removeEnemy(UUID id) {
        GhostEnemy e = findEnemy(id);
        if (e != null) {
            game.getEngine().getSceneGraph().removeGameObject(e);
            enemies.remove(e);
        }
    }

    private GhostEnemy findEnemy(UUID id) {
        for (GhostEnemy e : enemies) {
            if (e.getID().compareTo(id) == 0) {
                return e;
            }
        }
        return null;
    }
	
	public void updateAnimations() {
		// System.out.println("game enemy manager updateAnimations");
        for (GhostEnemy e : enemies) {
            e.updateAnimation();
        }
    }
}