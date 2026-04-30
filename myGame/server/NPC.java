package myGame.server;

import java.util.UUID;

public class NPC {
    private UUID id;
    private double x, y, z;
    private double dir = 0.05;
    private double size = 1.0;
	
	private double targetX, targetY, targetZ;
	private boolean chasing = false;
	private double speed = 0.09;

    public NPC() {
        id = UUID.randomUUID();
        x = 0.0;
        y = 0.0;
        z = -2.0;
    }

    public UUID getID() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    public double getSize() { return size; }

    public void getBig() {
        size = 3.0;
    }

    public void getSmall() {
        size = 1.0;
    }
	
	public void setChasing(boolean value) {
		chasing = value;
	}

	public boolean isChasing() {
		return chasing;
	}
	
	public void setLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setTarget(double x, double y, double z) {
		targetX = x;
		targetY = y;
		targetZ = z;
	}
	
	public void updateLocation() {
		if (chasing) {
			double dx = targetX - x;
			double dz = targetZ - z;

			double dist = Math.sqrt(dx * dx + dz * dz);

			if (dist > 0.1) {
				x += (dx / dist) * speed;
				z += (dz / dist) * speed;
			}
		} else {
			if (x > 3) dir = -0.05;
			if (x < -3) dir = 0.05;
			x = x + dir;
		}
	}
}