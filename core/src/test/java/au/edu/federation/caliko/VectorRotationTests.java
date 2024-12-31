package au.edu.federation.caliko;

import au.edu.federation.utils.Vec3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VectorRotationTests {

	@Test
	public void rotatePitch(){
		rotatePitch(new Vec3f(0.0f, 0.5f, 0.5f).normalise());

		rotatePitch(new Vec3f(0.0f, 0.0f, 1.0f));
		rotatePitch(new Vec3f(0.0f, 0.0f, -1.0f));
		rotatePitch(new Vec3f(1.0f, 0.0f, 0.0f));
		rotatePitch(new Vec3f(-1.0f, 0.0f, 0.0f));
		rotatePitch(new Vec3f(0.0f, 1.0f, 0.0f));
		rotatePitch(new Vec3f(0.0f, -1.0f, 0.0f));
	}

	@Test
	public void rotateYaw(){
		rotateYaw(new Vec3f(0.0f, 0.0f, 1.0f));
		rotateYaw(new Vec3f(0.0f, 0.0f, -1.0f));
		rotateYaw(new Vec3f(1.0f, 0.0f, 0.0f));
		rotateYaw(new Vec3f(-1.0f, 0.0f, 0.0f));
		rotateYaw(new Vec3f(0.0f, 1.0f, 0.0f));
		rotateYaw(new Vec3f(0.0f, -1.0f, 0.0f));
	}

	private void rotatePitch(Vec3f boneDirection) {
		Vec3f expected = new Vec3f(boneDirection);

		// Rotate bone around the world-space X-axis in 30 degrees increments
		float pitch;
		for (int i = 0; i < 12; i++) {
			boneDirection = Vec3f.rotateXDegs(boneDirection, 30f).normalise();
			pitch = boneDirection.getGlobalPitchDegs();
			System.out.println("After " + (i + 1) * 30 + " degrees rotation bone pitch is: " + pitch + " degrees. Direction: " + boneDirection);
		}

		assertEquals(expected, boneDirection, 0.0001f);
	}

	private void rotateYaw(Vec3f boneDirection) {
		Vec3f expected = new Vec3f(boneDirection);

		// Rotate bone around the world-space Y-axis in 30 degrees increments
		float yaw;
		for (int i = 0; i < 12; i++) {
			boneDirection = Vec3f.rotateYDegs(boneDirection, 30f).normalise();
			yaw = boneDirection.getGlobalYawDegs();
			System.out.println("After " + (i + 1) * 30 + " degrees rotation bone yaw is: " + yaw + " degrees. Direction: " + boneDirection);
		}

		assertEquals(expected, boneDirection, 0.0001f);
	}

	private void assertEquals(Vec3f expected, Vec3f actual, float delta) {
		Assertions.assertEquals(expected.x, actual.x, delta);
		Assertions.assertEquals(expected.y, actual.y, delta);
		Assertions.assertEquals(expected.z, actual.z, delta);
	}

}
