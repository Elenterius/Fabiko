package tests.equivalence;

import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec3f;
import com.github.elenterius.fabiko.core.FabrikWorld;
import org.joml.Math;
import org.joml.*;
import org.junit.jupiter.api.Test;
import tests.util.ExtraAssertions;
import tests.util.Vector;

public class TransformationTests {

	@Test
	public void testRelativeRotationAxisFromPlusX() {
		Vector direction = new Vector(1, 0, 0).normalize();
		Vector rotationAxis = new Vector(1, 0, 0);

		Vector expected = Caliko.relativeRotationAxis(direction, rotationAxis);
		Vector actual = Fabiko.relativeRotationAxis(direction, rotationAxis);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testRelativeRotationAxisFromPlusY() {
		Vector direction = new Vector(0, 1, 0);
		Vector rotationAxis = new Vector(1, 0, 0);

		Vector expected = Caliko.relativeRotationAxis(direction, rotationAxis);
		Vector actual = Fabiko.relativeRotationAxis(direction, rotationAxis);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testRelativeRotationAxisFromNegativeZ() {
		Vector direction = new Vector(0, 0, -1);
		Vector rotationAxis = new Vector(1, 0, 0);

		Vector expected = Caliko.relativeRotationAxis(direction, rotationAxis);
		Vector actual = Fabiko.relativeRotationAxis(direction, rotationAxis);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testModelMatrix4() {
		Vector direction = new Vector(2, 0, 2).normalize();
		Vector translation = new Vector(10, 0, 0);

		Mat4f expected = Caliko.modelMatrix4(direction, translation);
		Matrix4f actual = Fabiko.modelMatrix4(direction, translation);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testMatrix4Rotation() {
		float aspectRatio = 800f / 600f;

		Mat4f expected = Mat4f.createPerspectiveProjectionMatrix(35f, aspectRatio, 1f, 5000f)
				.rotateAboutLocalAxisRads(Math.PI_OVER_2_f, new Vec3f(0, 1, 0));

		Matrix4f actual = new Matrix4f().setPerspective(Math.toRadians(35f), aspectRatio, 1f, 5000f)
				.rotate(Math.PI_OVER_2_f, 0, 1, 0);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	static class Caliko {

		private static Mat4f modelMatrix4(Vector direction, Vector translation) {
			return new Mat4f(Mat3f.createRotationMatrix(direction.toCaliko()), translation.toCaliko());
		}

		private static Vector relativeRotationAxis(Vector direction, Vector rotationAxis) {
			Mat3f m = Mat3f.createRotationMatrix(direction.toCaliko());
			Vec3f relativeRotationAxis = m.times(rotationAxis.toCaliko()).normalise();

			System.out.println(m);

			return Vector.from(relativeRotationAxis);
		}

	}

	static class Fabiko {

		private static Matrix4f modelMatrix4(Vector direction, Vector translation) {
			Quaternionf orientation = new Quaternionf().rotationTo(FabrikWorld.FORWARDS, direction.toJoml());
			Matrix4f correction = new Matrix4f().m22(-1); //negate Z colum

			return new Matrix4f().set(orientation).setTranslation(translation.toJoml()).mul(correction);
		}

		private static Vector relativeRotationAxis(Vector direction, Vector rotationAxis) {
			Quaternionf orientation = new Quaternionf().rotationTo(FabrikWorld.FORWARDS, direction.toJoml());

			Matrix3f m = new Matrix3f().set(orientation);
			System.out.println("    x         y         z");
			System.out.println(m);

			Vector3f relativeRotationAxis = orientation.transform(rotationAxis.toJoml(), new Vector3f());

			return Vector.from(relativeRotationAxis);
		}

	}

}
