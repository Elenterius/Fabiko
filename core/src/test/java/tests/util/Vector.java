package tests.util;

import au.edu.federation.utils.Vec3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;

public record Vector(float x, float y, float z) {

	public static Vector from(Vec3f v) {
		return new Vector(v.x, v.y, v.z);
	}

	public Vec3f toCaliko() {
		return new Vec3f(x, y, z);
	}

	public static Vector from(Vector3fc v) {
		return new Vector(v.x(), v.y(), v.z());
	}

	public Vector3f toJoml() {
		return new Vector3f(x, y, z);
	}

	public float distance(Vector v) {
		float dx = v.x - x;
		float dy = v.y - y;
		float dz = v.z - z;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public Vector normalize() {
		float invNorm = 1f / (float) Math.sqrt(x * x + y * y + z * z);
		return new Vector(x * invNorm, y * invNorm, z * invNorm);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Vector(float x1, float y1, float z1)) {
			return ExtraAssertions.floatsAreEqual(x, x1) && ExtraAssertions.floatsAreEqual(y, y1) && ExtraAssertions.floatsAreEqual(z, z1);
		}
		return false;
	}

	public boolean equals(Vector v, float delta) {
		return ExtraAssertions.floatsAreEqual(x, v.x, delta) && ExtraAssertions.floatsAreEqual(y, v.y, delta) && ExtraAssertions.floatsAreEqual(z, v.z, delta);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public String toString() {
		return "[x=%s, y=%s, z=%s]".formatted(x, y, z);
	}

}
