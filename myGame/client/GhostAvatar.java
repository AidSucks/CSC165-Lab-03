package myGame.client;

import java.util.UUID;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;

public class GhostAvatar extends GameObject {
	
	private final UUID uuid;

	public GhostAvatar(UUID ghostID, ObjShape shape, TextureImage texture)
	{
		super(GameObject.root(), shape, texture);
		this.uuid = ghostID;
	}

	public UUID getUUID() { return this.uuid; }

}
