package myGame.networking;

import java.io.Serializable;
import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityInfo implements Serializable {
		public UUID id;
		public Vector3f position;
		public Quaternionf rotation;
		public EntityType type;

		public EntityInfo() {}
	}