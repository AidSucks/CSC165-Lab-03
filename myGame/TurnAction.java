package myGame;

import tage. * ;
import tage.shapes. * ;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml. * ;
import java.lang.Math;

public class TurnAction extends AbstractInputAction {

    private MyGame game;
    private GameObject av;

    public TurnAction(MyGame g) {
        game = g;
    }

     @ Override
    public void performAction(float time, Event e) {
        // System.out.println("Event e = " + e);
        // System.out.println("ID class = " + e.getComponent().getIdentifier().getClass());
        // System.out.println("ID = " + e.getComponent().getIdentifier());
        // System.out.println("Name = " + e.getComponent().getName());

        float turnSpeed = 3.0f;
        float keyValue = e.getValue();

        av = game.getAvatar();

        // key yaw action
        if (e.getComponent().getName().equals("A")) {
            System.out.println("OBJ pressed A");
            av.yaw(turnSpeed * time);
        }

        if (e.getComponent().getName().equals("D")) {
            System.out.println("OBJ pressed D");
            av.yaw(-turnSpeed * time);
        }

        // key pitch action
        if (e.getComponent().getName().equals("Up")) {
            System.out.println("OBJ pressed Up");
            av.pitch(-turnSpeed * time);
        }

        if (e.getComponent().getName().equals("Down")) {
            System.out.println("OBJ pressed Down");
            av.pitch(turnSpeed * time);
        }

    }
}
