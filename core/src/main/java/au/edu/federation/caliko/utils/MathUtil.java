package au.edu.federation.caliko.utils;

import au.edu.federation.caliko.math.Vec2f;
import au.edu.federation.caliko.math.Vec3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class MathUtil {

	// Constants to translate values from degrees to radians and vice versa
	public static final float DEG_TO_RAD = (float) Math.PI / 180f;
	public static final float RAD_TO_DEG = 180f / (float) Math.PI;

	private MathUtil() {
	}

	/**
	 * Return the co-tangent of an angle specified in radians.
	 *
	 * @param angleRadians The angle specified in radians to return the co-tangent of.
	 * @return The co-tangent of the specified angle.
	 */
	public static float cot(float angleRadians) {
		return (float) (1f / Math.tan(angleRadians));
	}

	/**
	 * Convert radians to degrees.
	 *
	 * @param angleRadians The angle in radians.
	 * @return The converted angle in degrees.
	 */
	public static float radiansToDegrees(float angleRadians) {
		return angleRadians * RAD_TO_DEG;
	}

	/**
	 * Convert degrees to radians.
	 *
	 * @param angleDegrees The angle in degrees.
	 * @return The converted angle in radians.
	 */
	public static float degreesToRadians(float angleDegrees) {
		return angleDegrees * DEG_TO_RAD;
	}

	/**
	 * Return a FloatBuffer which can hold the specified number of floats.
	 *
	 * @param numFloats The number of floats this FloatBuffer should hold.
	 * @return A float buffer which can hold the specified number of floats.
	 */
	public static FloatBuffer createFloatBuffer(int numFloats) {
		return ByteBuffer.allocateDirect(numFloats * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	/**
	 * Determine the sign of a float value.
	 *
	 * @param value The value to return the sign of.
	 * @return 1.0f if the provided float value is positive, -1.0f otherwise.
	 */
	public static float sign(float value) {
		if (value >= 0.0f) {
			return 1.0f;
		}
		return -1.0f;
	}

	/**
	 * Validate a direction unit vector (Vec2f) to ensure that it does not have a magnitude of zero.
	 * <p>
	 * If the direction unit vector has a magnitude of zero then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV The direction unit vector to validate
	 */
	public static void validateDirectionUV(Vec2f directionUV) {
		// Ensure that the magnitude of this direction unit vector is greater than zero
		if (directionUV.length() <= 0.0f) {
			throw new IllegalArgumentException("Vec2f direction unit vector cannot be zero.");
		}
	}

	/**
	 * Validate a direction unit vector (Vec3f) to ensure that it does not have a magnitude of zero.
	 * <p>
	 * If the direction unit vector has a magnitude of zero then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV The direction unit vector to validate
	 */
	public static void validateDirectionUV(Vec3f directionUV) {
		// Ensure that the magnitude of this direction unit vector is greater than zero
		if (directionUV.length() <= 0.0f) {
			throw new IllegalArgumentException("Vec3f direction unit vector cannot be zero.");
		}
	}

	/**
	 * Validate the length of a bone to ensure that it's a positive value.
	 * <p>
	 * If the provided bone length is not greater than zero then an IllegalArgumentException is thrown.
	 *
	 * @param length The length value to validate.
	 */
	public static void validateLength(float length) {
		// Ensure that the magnitude of this direction unit vector is not zero
		if (length < 0.0f) {
			throw new IllegalArgumentException("Length must be a greater than or equal to zero.");
		}
	}

	/**
	 * Convert a value in one range into a value in another range.
	 * <p>
	 * If the original range is approximately zero then the returned value is the
	 * average value of the new range, that is: (newMin + newMax) / 2.0f
	 *
	 * @param origValue The original value in the original range.
	 * @param origMin   The minimum value in the original range.
	 * @param origMax   The maximum value in the original range.
	 * @param newMin    The new range's minimum value.
	 * @param newMax    The new range's maximum value.
	 * @return The original value converted into the new range.
	 */
	public static float convertRange(float origValue, float origMin, float origMax, float newMin, float newMax) {
		float origRange = origMax - origMin;
		float newRange = newMax - newMin;

		float newValue;
		if (origRange > -0.000001f && origRange < 0.000001f) {
			newValue = (newMin + newMax) / 2.0f;
		}
		else {
			newValue = (((origValue - origMin) * newRange) / origRange) + newMin;
		}

		return newValue;
	}

	/**
	 * Return a boolean indicating whether a float approximately equals another to within a given tolerance.
	 *
	 * @param a         The first value
	 * @param b         The second value
	 * @param tolerance The difference within the <strong>a</strong> and <strong>b</strong> values must be within to be considered approximately equal.
	 * @return Whether the a and b values are approximately equal or not.
	 */
	public static boolean approximatelyEquals(float a, float b, float tolerance) {
		return Math.abs(a - b) <= tolerance;
	}

	/**
	 * Ensure we have a legal line width with which to draw.
	 * <p>
	 * Valid line widths are between 1.0f and 32.0f pixels inclusive.
	 * <p>
	 * Line widths outside this range will cause an IllegalArgumentException to be thrown.
	 *
	 * @param lineWidth The width of the line we are validating.
	 */
	public static void validateLineWidth(float lineWidth) {
		if (lineWidth < 1f || lineWidth > 32f) {
			throw new IllegalArgumentException("Line widths must be within the range 1.0f to 32.0f - but only 1.0f is guaranteed to be supported.");
		}
	}

	public static float clamp(float value, float min, float max) {
		return Math.min(max, Math.max(value, min));
	}

}
