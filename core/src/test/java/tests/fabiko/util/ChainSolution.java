package tests.fabiko.util;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class ChainSolution implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public static float MAX_FLOAT_DIFFERENCE = 0.0001f;

	private final List<Bone> chain;
	private final Vector targetLocation;

	private final int maxIterationAttempts;
	private final float minIterationChange;
	private final float solveDistanceThreshold;

	public ChainSolution(List<Bone> chain, Vector targetLocation, int maxIterationAttempts, float minIterationChange, float solveDistanceThreshold) {
		this.maxIterationAttempts = maxIterationAttempts;
		this.chain = chain;
		this.targetLocation = targetLocation;
		this.minIterationChange = minIterationChange;
		this.solveDistanceThreshold = solveDistanceThreshold;
	}

	public static ChainSolution from(FabrikChain3D fabrikChain) {
		List<Bone> chain = new ArrayList<>(fabrikChain.getChain().size());
		for (FabrikBone3D bone : fabrikChain.getChain()) {
			chain.add(Bone.from(bone));
		}

		Vector targetLocation = Vector.from(fabrikChain.getLastTargetLocation());

		int maxIterationAttempts = fabrikChain.getMaxIterationAttempts();
		float minIterationChange = fabrikChain.getMinIterationChange();
		float solveDistanceThreshold = fabrikChain.getSolveDistanceThreshold();

		return new ChainSolution(chain, targetLocation, maxIterationAttempts, minIterationChange, solveDistanceThreshold);
	}

	static boolean floatsAreEqual(float v1, float v2) {
		return floatsAreEqual(v1, v2, MAX_FLOAT_DIFFERENCE);
	}

	static boolean floatsAreEqual(float v1, float v2, float delta) {
		return Float.floatToIntBits(v1) == Float.floatToIntBits(v2) || Math.abs(v1 - v2) <= delta;
	}

	private float getSolveDistance() {
		Vector endEffector = chain.getLast().end;
		return endEffector.distance(targetLocation);
	}

	public static float differenceError(ChainSolution expected, ChainSolution actual) {
		float error = 0f;

		error += Math.abs(expected.chain.size() - actual.chain.size()) * 2;
		if (error > 0f) return error;

		for (int i = 0; i < expected.chain.size(); i++) {
			Bone expectedBone = expected.chain.get(i);
			Bone actualBone = actual.chain.get(i);
			error += expectedBone.start.distance(actualBone.start);
			error += expectedBone.end.distance(actualBone.end);
		}

		return error;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		for (Bone bone : chain) {
			joiner.add("\n" + bone);
		}

		return "ChainSolution {" +
				"\ntargetLocation=" + targetLocation +
				",\nmaxIterationAttempts=" + maxIterationAttempts +
				",\nminIterationChange=" + minIterationChange +
				",\nsolveDistanceThreshold=" + solveDistanceThreshold +
				",\nchain=[" + joiner +
				"]\n}";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ChainSolution that)) return false;
		return maxIterationAttempts == that.maxIterationAttempts &&
				Float.compare(minIterationChange, that.minIterationChange) == 0 &&
				Float.compare(solveDistanceThreshold, that.solveDistanceThreshold) == 0 &&
				Objects.equals(chain, that.chain) &&
				Objects.equals(targetLocation, that.targetLocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chain, targetLocation, maxIterationAttempts, minIterationChange, solveDistanceThreshold);
	}

	public record Bone(Vector start, Vector end, float length) implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		public static Bone from(FabrikBone3D bone) {
			return new Bone(Vector.from(bone.getStartLocation()), Vector.from(bone.getEndLocation()), bone.length());
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Bone(Vector start1, Vector end1, float length1)) {
				return Objects.equals(end, end1) && Objects.equals(start, start1) && floatsAreEqual(length, length1);
			}
			return false;
		}

		@Override
		public String toString() {
			return "Bone{" +
					"start=" + start +
					", end=" + end +
					", length=" + length +
					"}";
		}
	}

	public record Vector(float x, float y, float z) implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		public static Vector from(Vec3f v) {
			return new Vector(v.x, v.y, v.z);
		}

		public float distance(Vector v) {
			float dx = v.x - x;
			float dy = v.y - y;
			float dz = v.z - z;
			return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Vector(float x1, float y1, float z1)) {
				return floatsAreEqual(x, x1) && floatsAreEqual(y, y1) && floatsAreEqual(z, z1);
			}
			return false;
		}

		@Override
		public String toString() {
			return "[x=%s, y=%s, z=%s]".formatted(x, y, z);
		}
	}

}
