package myGame.client;

import java.util.UUID;
import org.joml.*;
import tage.*;
import tage.TextureImage;
import tage.ObjShape;

public class GhostEnemy extends GameObject {
    private UUID id;

    public GhostEnemy(UUID id, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.id = id;
        setLocalLocation(p);
    }

    public UUID getID() {
        return id;
    }
}