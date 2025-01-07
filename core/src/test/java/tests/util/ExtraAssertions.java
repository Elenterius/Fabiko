package tests.util;

import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

public final class ExtraAssertions {

	public static float MAX_FLOAT_DIFFERENCE = 0.0001f;

	private ExtraAssertions() {
	}

	public static void assertEqualsByDistance(ChainSolution expected, ChainSolution actual, float delta, Supplier<String> msg) {
		if (!expected.equalsByDistance(actual, delta)) {
			failNotEqual(expected, actual, msg);
		}
	}

	public static void assertEqualsByDistance(ChainSolution expected, ChainSolution actual, float delta) {
		if (!expected.equalsByDistance(actual, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Vector expected, Vector actual, float delta) {
		if (!expected.equals(actual, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Quaternionfc expected, Quaternionfc actual, float delta) {
		if (!expected.equals(actual, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Vector3fc expected, Vector3fc actual, float delta) {
		if (!expected.equals(actual, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Matrix4f expected, Matrix4f actual, float delta) {
		if (!expected.equals(actual, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Vec3f expected, Vec3f actual, float delta) {
		if (!floatsAreEqual(expected.x, actual.x, delta) || !floatsAreEqual(expected.y, actual.y, delta) || !floatsAreEqual(expected.z, actual.z, delta)) {
			failNotEqual(expected, actual, null);
		}
	}

	public static void assertEquals(Mat3f expected, Mat3f actual, float delta) {
		float[] expectedValues = expected.toArray();
		float[] actualValues = actual.toArray();

		for (int i = 0; i < expectedValues.length; i++) {
			float e = expectedValues[i];
			float a = actualValues[i];
			if (!floatsAreEqual(e, a, delta)) {
				failNotEqual(expected, actual, null);
				break;
			}
		}
	}

	public static void assertEquals(Mat4f expected, Matrix4f actual, float delta) {
		assertEquals(new Matrix4f().set(expected.toArray()), actual, delta);
	}

	private static void failNotEqual(Object expected, Object actual, Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.expected(expected) //
				.actual(actual) //
				.buildAndThrow();
	}

	static boolean floatsAreEqual(float v1, float v2) {
		return floatsAreEqual(v1, v2, MAX_FLOAT_DIFFERENCE);
	}

	static boolean floatsAreEqual(float v1, float v2, float delta) {
		return Float.floatToIntBits(v1) == Float.floatToIntBits(v2) || Math.abs(v1 - v2) <= delta;
	}

}
