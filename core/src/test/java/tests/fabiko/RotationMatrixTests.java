package tests.fabiko;

import au.edu.federation.caliko.math.Mat3f;
import au.edu.federation.caliko.math.Vec3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RotationMatrixTests {

	@Test
	public void createRotationMatrixFromPlusX() {
		Vec3f plusX = new Vec3f(1f, 0f, 0f);
		Mat3f actual = Mat3f.createRotationMatrix(plusX);

		Mat3f expected = new Mat3f(
				0f, 0f, 1f,
				0f, 1f, 0f,
				1f, 0f, 0f
		);

		assertEquals(expected, actual);
	}

	@Test
	public void createRotationMatrixFromPlusY() {
		Vec3f plusY = new Vec3f(0f, 1f, 0f);
		Mat3f actual = Mat3f.createRotationMatrix(plusY);

		Mat3f expected = new Mat3f(
				1f, 0f, 0f,
				0f, 0f, 1f,
				0f, 1f, 0f
		);

		assertEquals(expected, actual);
	}

	@Test
	public void createRotationMatrixFromPlusZ() {
		Vec3f plusZ = new Vec3f(0f, 0f, 1f);
		Mat3f actual = Mat3f.createRotationMatrix(plusZ);

		Mat3f expected = new Mat3f(
				-1f, 0f, 0f,
				0f, 1f, -0f,
				0f, 0f, 1f
		);

		assertEquals(expected, actual);
	}

	@Test
	public void createRotationMatrixFromMinusX() {
		Vec3f minusX = new Vec3f(-1f, 0f, 0f);
		Mat3f actual = Mat3f.createRotationMatrix(minusX);

		Mat3f expected = new Mat3f(
				0f, 0f, -1f,
				0f, 1f, 0f,
				-1f, 0f, 0f
		);

		assertEquals(expected, actual);
	}

	@Test
	public void createRotationMatrixFromMinusY() {
		Vec3f minusY = new Vec3f(0f, -1f, 0f);
		Mat3f actual = Mat3f.createRotationMatrix(minusY);

		Mat3f expected = new Mat3f(
				1f, 0f, 0f,
				0f, 0f, -1f,
				0f, -1f, 0f
		);

		assertEquals(expected, actual);
	}

	@Test
	public void createRotationMatrixFromMinusZ() {
		Vec3f minusZ = new Vec3f(0f, 0f, -1f);
		Mat3f actual = Mat3f.createRotationMatrix(minusZ);

		Mat3f expected = new Mat3f(
				1f, -0f, 0f,
				0f, 1f, 0f,
				0f, 0f, -1f
		);

		assertEquals(expected, actual);
	}

}
