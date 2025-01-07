package tests.util;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import com.github.elenterius.fabiko.core.FabrikBone3f;
import com.github.elenterius.fabiko.core.FabrikChain3f;

import java.util.*;

public final class ChainSolution {

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

	public static ChainSolution from(FabrikChain3f fabikoChain) {
		List<Bone> chain = new ArrayList<>(fabikoChain.getBoneCount());
		for (FabrikBone3f bone : fabikoChain.getBones()) {
			chain.add(Bone.from(bone));
		}

		Vector targetLocation = Vector.from(fabikoChain.getLastTargetLocation());

		int maxIterationAttempts = fabikoChain.getMaxIterationAttempts();
		float minIterationChange = fabikoChain.getMinIterationChange();
		float solveDistanceThreshold = fabikoChain.getSolveDistanceThreshold();

		return new ChainSolution(chain, targetLocation, maxIterationAttempts, minIterationChange, solveDistanceThreshold);
	}

	public record ErrorStats(double absoluteSumError, double absoluteMeanError, double absoluteMinError, double absoluteMaxError) {}

	public static ErrorStats getErrorStats(ChainSolution expected, ChainSolution actual) {
		double[] errors = new double[expected.chain.size() * 2];

		for (int i = 0; i < expected.chain.size(); i++) {
			Bone expectedBone = expected.chain.get(i);
			Bone actualBone = actual.chain.get(i);
			errors[i * 2] = expectedBone.start.distance(actualBone.start);
			errors[i * 2 + 1] = expectedBone.end.distance(actualBone.end);
		}

		DoubleSummaryStatistics statistics = Arrays.stream(errors).summaryStatistics();

		return new ErrorStats(statistics.getSum(), statistics.getAverage(), statistics.getMin(), statistics.getMax());
	}

	private static boolean equalsByDistance(List<Bone> chainA, List<Bone> chainB, float delta) {
		if (chainA.size() != chainB.size()) return false;

		for (int i = 0; i < chainA.size(); i++) {
			Bone boneA = chainA.get(i);
			Bone boneB = chainB.get(i);
			if (!boneA.equalsByDistance(boneB, delta)) {
				return false;
			}
		}

		return true;
	}

	private float getSolveDistance() {
		Vector endEffector = chain.getLast().end;
		return endEffector.distance(targetLocation);
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

	public boolean equalsByDistance(ChainSolution o, float delta) {
		return maxIterationAttempts == o.maxIterationAttempts &&
				Float.compare(minIterationChange, o.minIterationChange) == 0 &&
				Float.compare(solveDistanceThreshold, o.solveDistanceThreshold) == 0 &&
				equalsByDistance(chain, o.chain, delta) &&
				targetLocation.equals(o.targetLocation, delta);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chain, targetLocation, maxIterationAttempts, minIterationChange, solveDistanceThreshold);
	}

	public record Bone(Vector start, Vector end, float length) {

		public static Bone from(FabrikBone3D bone) {
			return new Bone(Vector.from(bone.getStartLocation()), Vector.from(bone.getEndLocation()), bone.length());
		}

		public static Bone from(FabrikBone3f bone) {
			return new Bone(Vector.from(bone.getStartLocation()), Vector.from(bone.getEndLocation()), bone.length());
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Bone(Vector start1, Vector end1, float length1)) {
				return Objects.equals(end, end1) && Objects.equals(start, start1) && ExtraAssertions.floatsAreEqual(length, length1);
			}
			return false;
		}

		public boolean equals(Bone o, float delta) {
			return end().equals(o.end, delta) && start.equals(o.start, delta) && ExtraAssertions.floatsAreEqual(length, o.length, delta);
		}

		public boolean equalsByDistance(Bone o, float delta) {
			return end().distance(o.end) < delta && start.distance(o.start) < delta;
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

}
