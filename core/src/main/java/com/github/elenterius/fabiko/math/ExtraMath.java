package com.github.elenterius.fabiko.math;

import org.joml.Math;

public final class ExtraMath {

	private ExtraMath() {
	}

	/// Return the co-tangent of an angle.
	///
	/// @param angleRadians the angle specified in radians to return the co-tangent of.
	/// @return the co-tangent of the specified angle
	public static float cot(float angleRadians) {
		return 1f / Math.tan(angleRadians);
	}

}
