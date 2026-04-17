package myGame;

import tage. * ;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml. * ;

public class FwdAction extends AbstractInputAction {

    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition;
    private Vector4f fwdDirection;

    public FwdAction(MyGame g) {
        game = g;
    }

     @ Override
    public void performAction(float time, Event e) {
        // System.out.println("Event e = " + e);
        // System.out.println("ID class = " + e.getComponent().getIdentifier().getClass());
        // System.out.println("ID = " + e.getComponent().getIdentifier());
        // System.out.println("Name = " + e.getComponent().getName());
        // System.out.println("Value = " + e.getValue());

        /*
        event print should be:
        Event e = Event: component = W | value = 1.0
        ID class = class net.java.games.input.Component$Identifier$Key
        ID = W
        Name = W
        Value = 1.0
        keyValue= 1.0

        Event e = Event: component = Button 3 | value = 1.0
        ID class = class net.java.games.input.Component$Identifier$Button
        ID = 3
        Name = Button 3
        Value = 1.0
        keyValue= 1.0
         */

        float moveSpeed = 4.5f;
        float keyValue = e.getValue();
        // System.out.println("keyValue= " + keyValue);

        av = game.getAvatar();
        oldPosition = av.getWorldLocation();
        fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
        fwdDirection.mul(av.getWorldRotation());
        fwdDirection.mul(moveSpeed * time);

        // key forward
        if (e.getComponent().getName().equals("W")) {
            // System.out.println("OBJ pressed W");
            newPosition = new Vector3f(oldPosition).add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
        }

        // key backward
        if (e.getComponent().getName().equals("S")) {
            // System.out.println("Obj pressed S");
            newPosition = new Vector3f(oldPosition).sub(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
        }

        av.setLocalLocation(newPosition);

		this.game.getGameClient().sendMove(av.getWorldLocation());
    }
}
