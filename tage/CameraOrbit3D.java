package tage;

import tage. * ;
import tage.shapes. * ;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import tage.input.InputManager;
import org.joml. * ;
import java.lang.Math;

/**
 * A third-person orbit camera controller for a target avatar.
 * <p>
 * This controller maintains a camera position relative to a target
 * {@link GameObject} using spherical coordinates:
 * azimuth, elevation, and radius.
 * The camera always looks at the avatar while allowing the user to:
 * <ul>
 *   <li>orbit left and right around the avatar,</li>
 *   <li>adjust the elevation angle, and</li>
 *   <li>zoom in and out by changing the orbit radius.</li>
 * </ul>
 * The camera position is recomputed whenever one of these values changes.
 *
 * @author Chik Pan Wong
 */
public class CameraOrbit3D {
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target

    private float minElevation = -10.f;
    private float maxElevation = 40.0f;
    private float minRadius = 1.0f;
    private float maxRadius = 3.5f;

	/**
	 * Constructs a third-person orbit camera controller for the specified camera
	 * and avatar.
	 * <p>
	 * The camera is initialized with default azimuth, elevation, and radius
	 * values, and its initial position is computed immediately.
	 *
	 * @param cam the camera to control
	 * @param av the target avatar that the camera orbits around
	 */
    public CameraOrbit3D(Camera cam, GameObject av) {
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 2.0f; // distance from camera to avatar
        camera = cam;
        avatar = av;
        updateCameraPosition();
    }

    // Compute the camera’s azimuth, elevation, and distance, relative to
    // the target in spherical coordinates, then convert to world Cartesian
    // coordinates and set the camera position from that.
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double)avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float)avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float)(Math.sin(phi));
        float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }
	
	/**
	 * Adjusts the camera's azimuth angle around the avatar.
	 * <p>
	 * Positive values rotate the camera in one horizontal direction around the
	 * avatar, while negative values rotate it in the opposite direction.
	 *
	 * @param rotAmount the azimuth rotation amount
	 */
    public void orbitAzimuth(float rotAmount) {

        // System.out.println("orbitAzimuth rotAmount is = " + rotAmount);

        cameraAzimuth += rotAmount;
        cameraAzimuth = cameraAzimuth % 360;
        updateCameraPosition();
    }

	/**
	 * Adjusts the camera's elevation angle above or below the avatar.

	 * @param rotAmount the elevation adjustment amount
	 */
    public void orbitElevation(float rotAmount) {

        // System.out.println("orbitElevation rotAmount is = " + rotAmount);

        cameraElevation += rotAmount;

        if (cameraElevation <= minElevation) {
            cameraElevation = minElevation;
        }
        if (cameraElevation >= maxElevation) {
            cameraElevation = maxElevation;
        }
        cameraElevation = cameraElevation % 360;
        updateCameraPosition();
    }
	
	/**
	 * Adjusts the camera's orbit radius relative to the avatar.
	 * <p>
	 * Increasing the radius moves the camera farther away from the avatar, while
	 * decreasing it zooms the camera closer.
	 *
	 * @param zoomAmount the amount to change the orbit radius
	 */
    public void orbitRadius(float zoomAmount) {

        // System.out.println("orbitRadius zoomAmount is = " + zoomAmount);

        cameraRadius += zoomAmount;

        if (cameraRadius <= minRadius) {
            cameraRadius = minRadius;
        }

        if (cameraRadius >= maxRadius) {
            cameraRadius = maxRadius;
        }

        cameraRadius = cameraRadius % 360;
        updateCameraPosition();
    }

}
