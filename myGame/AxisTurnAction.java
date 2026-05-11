package myGame;

import tage. * ;
import tage.shapes. * ;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml. * ;

public class AxisTurnAction extends AbstractInputAction {

    private MyGame game;
    private GameObject av;

    public AxisTurnAction(MyGame g) {
        game = g;
    }

     @ Override
    public void performAction(float time, Event e) {



		float turnSpeed = 3.0f;
        float keyValue = e.getValue();
		av = game.getAvatar();
		
        if (keyValue >  - .3 && keyValue < .3) {
            return; // deadzone
        }

		av = game.getAvatar();
		if (keyValue >  - .3) {
			av.yaw(-turnSpeed * time);
		}
        if (keyValue < .3) {
			
			av.yaw(turnSpeed * time);
		}


    }
}
