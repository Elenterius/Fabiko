package com.github.elenterius.fabiko.math;

import org.joml.GeometryUtils;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class JomlMath {

	private static final Vector3f BLACK_HOLE_V3f = new Vector3f();

	/// Projects a vector onto a plane defined by its normal.
	///
	/// @param v           the vector to project
	/// @param planeNormal the normal vector of the plane (must be normalized)
	/// @return the input vector `v` projected onto the plane
	/// @see Vector3fc#normalize
	public static Vector3f projectOntoPlane(Vector3f v, Vector3fc planeNormal) {
		return projectOntoPlane(v, planeNormal, v);
	}

	/// Projects a vector onto a plane defined by its normal and store the result in `dest`.
	///
	/// @param v           the vector to project
	/// @param planeNormal the normal vector of the plane (must be normalized)
	/// @param dest        will hold the result
	/// @return dest
	/// @see Vector3fc#normalize
	public static Vector3f projectOntoPlane(Vector3fc v, Vector3fc planeNormal, Vector3f dest) {
		Vector3f normalProjection = planeNormal.mul(v.dot(planeNormal), new Vector3f());
		v.sub(normalProjection, dest);
		return dest;
	}

	public static boolean floatsAreEqual(float v1, float v2, float delta) {
		return Float.floatToIntBits(v1) == Float.floatToIntBits(v2) || Math.abs(v1 - v2) <= delta;
	}

	public static boolean isUnitVector(Vector3fc v) {
		return floatsAreEqual(v.length(), 1f, 1E-6f);
	}

	/// Return whether the two provided vectors are perpendicular (to a dot-product tolerance of 0.01f).
	///
	/// @param v1 the first vector
	/// @param v2 the second vector
	/// @return `true` when the two vectors are perpendicular
	public static boolean isPerpendicular(Vector3fc v1, Vector3fc v2) {
		return floatsAreEqual(v1.dot(v2), 0f, 0.01f);
	}

	/// Return a normalised [Vector3f] which is perpendicular to the vector provided.
	///
	/// This is a very fast method of generating a perpendicular vector that works for any vector
	/// which is 5 degrees or more from vertical 'up'.
	///
	/// The code is copied from the caliko library which in turn is adapted from: <a href="http://blog.selfshadow.com/2011/10/17/perp-vectors/">Perpendicular Possibilities</a>
	///
	/// @param v The vector to use as the basis for generating the perpendicular vector.
	/// @return A normalised vector which is perpendicular to the provided vector argument.
	/// @author Al Lansley
	public static Vector3f perpendicularQuick(Vector3fc v, Vector3f dest) {
		if (Math.abs(v.y()) < 0.99f) {
			dest.set(-v.z(), 0f, v.x()); // cross(u, UP)
		}
		else {
			dest.set(0f, v.z(), -v.y()); // cross(u, RIGHT)
		}

		return dest.normalize();
	}

	/// Compute one arbitrary vector perpendicular to the given [normalized][Vector3f#normalize()] vector `v`, and store them in `dest`.
	///
	/// The computed vector will be perpendicular and normalized.
	///
	/// @param v    the `normalized` input vector
	/// @param dest will hold the perpendicular vector
	public static Vector3f perpendicular(Vector3fc v, Vector3f dest) {
		GeometryUtils.perpendicular(v, dest, BLACK_HOLE_V3f);
		return dest;
	}

	/// Compute two arbitrary vectors perpendicular to the given [normalized][Vector3f#normalize()] vector `v`, and
	/// store them in `dest1` and `dest2`, respectively.
	///
	/// The computed vectors will themselves be perpendicular to each another and normalized.
	/// So the tree vectors `v`, `dest1` and `dest2` form an orthonormal basis.
	///
	/// @param v     the `normalized` input vector
	/// @param dest1 will hold the first perpendicular vector
	/// @param dest2 will hold the second perpendicular vector
	public static void perpendicular(Vector3fc v, Vector3f dest1, Vector3f dest2) {
		GeometryUtils.perpendicular(v, dest1, dest2);
	}

}
