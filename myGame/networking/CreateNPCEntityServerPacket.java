package myGame.networking;

import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CreateNPCEntityServerPacket extends CreateEntityServerPacket {

	private String state;
	private double size;

	public CreateNPCEntityServerPacket(
		UUID entityID, 
		Vector3f position, 
		Quaternionf rotation, 
		EntityType type,
		String animationState,
		double size
	) {
		super(entityID, position, rotation, type);
		this.state = animationState;
		this.size = size;
	}

	public String getState() { return this.state; }
	public double getSize() { return this.size; }
	
}
