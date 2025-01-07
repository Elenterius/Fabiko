package tests.equivalence;

import au.edu.federation.utils.Vec3f;
import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import tests.util.ExtraAssertions;
import tests.util.Vector;

public class ProjectionTests {

	@Test
	public void testPerspectiveProjection() {
		float windowWidth = 800f;
		float windowHeight = 600f;
		float aspectRatio = windowWidth / windowHeight;

		float verticalFoVDegrees = 35f;
		float zNear = 1f;
		float zFar = 5_000f;

		Matrix4f expected = Caliko.createPerspectiveProjectionMatrix(verticalFoVDegrees, aspectRatio, zNear, zFar);
		Matrix4f actual = new Matrix4f().setPerspective(Math.toRadians(verticalFoVDegrees), aspectRatio, zNear, zFar);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testOrthographicProjection() {
		float zNear = 1f;
		float zFar = 5_000f;
		float orthoExtent = 120f;

		Matrix4f expected = Caliko.createOrthographicProjectionMatrix(-orthoExtent, orthoExtent, orthoExtent, -orthoExtent, zNear, zFar);

		Matrix4f actual = new Matrix4f().setOrtho(-orthoExtent, orthoExtent, -orthoExtent, orthoExtent, zNear, zFar);

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	@Test
	public void testProjectVectorOntoPlane() {
		Vector v = new Vector(1, 2, 3);
		Vector planeNormal = new Vector(0, 1, 0);

		Vector expected = Caliko.projectVectorOntoPlane(v.toCaliko(), planeNormal.toCaliko());
		Vector actual = Fabiko.projectVectorOntoPlane(v.toJoml(), planeNormal.toJoml());

		ExtraAssertions.assertEquals(expected, actual, 0.0001f);
	}

	static class Fabiko {

		static Vector projectVectorOntoPlane(Vector3f v, Vector3f planeNormal) {
			v.normalize();
			Vector3f projected = JomlMath.projectOntoPlane(v, planeNormal, new Vector3f()).normalize();
			return Vector.from(projected);
		}

	}

	static class Caliko {

		static Matrix4f createPerspectiveProjectionMatrix(float verticalFoVDegrees, float aspectRatio, float zNear, float zFar) {
			if (aspectRatio < 0f) {
				throw new IllegalArgumentException("Aspect ratio cannot be negative.");
			}
			if (zNear <= 0f || zFar <= 0f) {
				throw new IllegalArgumentException("The values of zNear and zFar must be positive.");
			}
			if (zNear >= zFar) {
				throw new IllegalArgumentException("zNear must be less than than zFar.");
			}
			if (verticalFoVDegrees < 1f || verticalFoVDegrees > 179f) {
				throw new IllegalArgumentException("Vertical FoV must be within 1 and 179 degrees inclusive.");
			}

			float frustumLength = zFar - zNear;

			// Calculate half the vertical field of view in radians
			float halfVertFoVRads = Math.toRadians(verticalFoVDegrees / 2f);

			float cotangent = 1f / Math.tan(halfVertFoVRads);

			Matrix4f m = new Matrix4f(); //identity matrix

			m.m00(cotangent / aspectRatio);
			m.m11(cotangent);
			m.m22(-(zFar + zNear) / frustumLength);
			m.m23(-1f);
			m.m32((-2f * zNear * zFar) / frustumLength);
			m.m33(0f);

			return m;
		}

		static Matrix4f createOrthographicProjectionMatrix(float left, float right, float top, float bottom, float near, float far) {
			if (Float.compare(right - left, 0f) == 0) {
				throw new IllegalArgumentException("(right - left) cannot be zero.");
			}
			if (Float.compare(top - bottom, 0f) == 0) {
				throw new IllegalArgumentException("(top - bottom) cannot be zero.");
			}
			if (Float.compare(far - near, 0f) == 0) {
				throw new IllegalArgumentException("(far - near) cannot be zero.");
			}

			Matrix4f m = new Matrix4f();

			m.m00(2f / (right - left));
			m.m01(0f);
			m.m02(0f);
			m.m03(0f);

			m.m10(0f);
			m.m11(2f / (top - bottom));
			m.m12(0f);
			m.m13(0f);

			m.m20(0f);
			m.m21(0f);
			m.m22(-2f / (far - near));
			m.m23(0f);

			m.m30(-(right + left) / (right - left));
			m.m31(-(top + bottom) / (top - bottom));
			m.m32(-(far + near) / (far - near));
			m.m33(1f);

			return m;
		}

		static Vector projectVectorOntoPlane(Vec3f v, Vec3f planeNormal) {
			Vec3f projected = v.projectOntoPlane(planeNormal);
			return Vector.from(projected);
		}

	}

}
