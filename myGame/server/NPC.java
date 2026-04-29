package myGame.server;

import java.util.UUID;

public class NPC {
    private UUID id;
    private double x, y, z;
    private double dir = 0.1;
    private double size = 1.0;

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
        size = 10.0;
    }

    public void getSmall() {
        size = 1.0;
    }

    public void updateLocation() {
        if (x > 10) dir = -0.1;
        if (x < -10) dir = 0.1;

        x += dir;
    }
}