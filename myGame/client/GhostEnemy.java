package myGame.client;

import java.util.UUID;
import org.joml.*;
import tage.*;
import tage.TextureImage;
import tage.shapes.AnimatedShape;

public class GhostEnemy extends GameObject {
    private UUID id;

	private AnimatedShape ghostEnemyS;
	private String currentAnim = "";

	
    public GhostEnemy(UUID id, AnimatedShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.id = id;
		this.ghostEnemyS = s;
        setLocalLocation(p);
    }

	// getter
    public UUID getID() {
        return id;
    }
	
	// setter
	public void setSize(float size) {
		setLocalScale(new Matrix4f().scaling(size));
	}
	
	public void setYaw(float yawAmt) {
		setLocalRotation(new Matrix4f().rotationY(yawAmt));
	}
	
	public void playAnimation(String animName) {
		// System.out.println("game client playAnimation: " + currentAnim);
        if (animName.equals(currentAnim)) {
            return;
        }

        ghostEnemyS.stopAnimation();
        ghostEnemyS.playAnimation(animName, 0.5f, AnimatedShape.EndType.LOOP, 0);
        
        currentAnim = animName;
		// System.out.println("game client playAnimation: " + animName);
    }
	
	public void updateAnimation() {
		// if (ghostEnemyS != NULL) 
		ghostEnemyS.updateAnimation();
		// System.out.println("game client updateAnimation");
    }
	
}