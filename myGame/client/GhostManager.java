package myGame.client;

import java.util.UUID;
import java.util.Vector;

import org.joml.Vector3f;

import myGame.MyGame;
import tage.ObjShape;
import tage.TextureImage;
import tage.VariableFrameRateGame;

public class GhostManager {
	
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<>();

	public GhostManager(VariableFrameRateGame game)
	{
		this.game = (MyGame) game;
	}

	public void createGhost(UUID id, Vector3f initialPosition)
	{
		ObjShape ghostShape = game.getAvatar().getShape();
		TextureImage ghostTexture = game.getAvatar().getTextureImage();

		GhostAvatar avatar = new GhostAvatar(id, ghostShape, ghostTexture);
		avatar.setLocalLocation(initialPosition);

		this.ghostAvatars.add(avatar);
	}

	public void removeGhost(UUID id)
	{
		GhostAvatar ghostToRemove = this.getAvatarByID(id);

		if(ghostToRemove == null) {
			System.err.println("Avatar not found: " + id.toString());
			return;
		}

		MyGame.getEngine().getSceneGraph().removeGameObject(ghostToRemove);
		this.ghostAvatars.remove(ghostToRemove);
	}

	public GhostAvatar getAvatarByID(UUID id)
	{
		for(GhostAvatar avatar : this.ghostAvatars)
			if(avatar.getUUID().equals(id))
				return avatar;

		return null;
	}

	public void updateGhost(UUID id, Vector3f newPosition)
	{
		GhostAvatar affectedGhost = this.getAvatarByID(id);

		if(affectedGhost == null) {
			System.err.println("Avatar not found: " + id.toString());
			return;
		}

		affectedGhost.setLocalLocation(newPosition);
	}
}
