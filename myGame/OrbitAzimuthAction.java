package myGame;

import tage. * ;
import tage.shapes. * ;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml. * ;

public class OrbitAzimuthAction extends AbstractInputAction {
	
    private MyGame game;
    private CameraOrbit3D orbitCamera;

    public OrbitAzimuthAction(MyGame g, CameraOrbit3D c) {
        game = g;
		orbitCamera = c;
    }
	
	@ Override
    public void performAction(float time, Event event) {

        // System.out.println("Event event = " + event);
        // System.out.println("ID class = " + event.getComponent().getIdentifier().getClass());
        // System.out.println("ID = " + event.getComponent().getIdentifier());
        // System.out.println("Name = " + event.getComponent().getName());
        // System.out.println("Value = " + event.getValue());
        // System.out.println("time is = " + time);

        float rotAmount = 0.0f;

        if (event.getValue() < -0.2 && event.getValue() > -0.99) {
            rotAmount = -5f;
        } else {
            if (event.getValue() > 0.2) {
                rotAmount = 5f;
            } else {
                rotAmount = 0.0f;

            }
        }
		
		// key action
        if (event.getComponent().getName().equals("C")) {
            // System.out.println("OBJ pressed C");
            rotAmount = -5f;
        }

        if (event.getComponent().getName().equals("V")) {
            // System.out.println("OBJ pressed V");
            rotAmount = 5f;
        }

        orbitCamera.orbitAzimuth(time * rotAmount);
    }
}
