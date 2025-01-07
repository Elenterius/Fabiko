package tests.fabiko;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikJoint3D;
import au.edu.federation.utils.Vec3f;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikSolver3f;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;
import tests.caliko.CalikoHack;
import tests.util.ChainSolution;
import tests.util.ExtraAssertions;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ChainSolvingTests {

	@Test
	public void testUnconstrainedChain() {
		int boneCount = 10;
		float boneLength = 10f;

		compareSolutionsByDistanceError(
				10_000, 10, 0.00077f,
				() -> Caliko.createUnconstrainedChain(boneLength, boneCount),
				() -> Fabiko.createUnconstrainedChain(boneLength, boneCount)
		);
	}

	@Test
	public void testLocalHingeChain() {
		int boneCount = 10;
		float boneLength = 10f;

		compareSolutionsByDistanceError(
				10_000, 2, 4.92198f,
				() -> Caliko.createLocalHingeChain(boneLength, boneCount),
				() -> Fabiko.createLocalHingeChain(boneLength, boneCount)
		);
	}

	@Test
	public void testLocalRotorChain() {
		int boneCount = 10;
		float boneLength = 10f;
		float angleDegrees = Math.toRadians(22.5f);
		float angleRadians = Math.toRadians(angleDegrees);

		compareSolutionsByDistanceError(
				10_000, 2, 0.12557f,
				() -> Caliko.createLocalRotorChain(boneLength, boneCount, angleDegrees),
				() -> Fabiko.createLocalRotorChain(boneLength, boneCount, angleRadians)
		);
	}

	public void compareSolutionsByDistanceError(int population, int solveCycles, float maxDistanceDelta, Supplier<FabrikChain3D> calikoFactory, Supplier<FabrikChain3f> fabikoFactory) {
		List<Double> errorsAboveThreshold = new ArrayList<>();

		Random random = new Random(123);

		for (int i = 0; i < population; i++) {
			int seed = random.nextInt();

			ChainSolution expected = Caliko.solveChain(calikoFactory.get(), solveCycles, new Random(seed));
			ChainSolution actual = Fabiko.solveChain(fabikoFactory.get(), solveCycles, new Random(seed));

			if (!expected.equalsByDistance(actual, 0.0001f)) {
				errorsAboveThreshold.add(ChainSolution.getErrorStats(expected, actual).absoluteMaxError());
			}

			final int n = i + 1;
			ExtraAssertions.assertEqualsByDistance(expected, actual, maxDistanceDelta, () -> "i=%d/%d seed=%d%n%s%n".formatted(n, population, seed, ChainSolution.getErrorStats(expected, actual)));
		}

		DoubleSummaryStatistics stats = errorsAboveThreshold.stream().mapToDouble(Double::doubleValue).summaryStatistics();
		DoubleSummaryStatistics statsAbove1E3f = errorsAboveThreshold.stream().mapToDouble(Double::doubleValue).filter(value -> value > 0.001f).summaryStatistics();

		System.out.printf("error > 0.0001f => count: %d (%.2f%%), min: %.5f, avg: %.5f, max: %.5f %n", stats.getCount(), stats.getCount() / (double) population * 100d, stats.getMin(), stats.getAverage(), stats.getMax());
		System.out.printf("error > 0.001f  => count: %d (%.2f%%), min: %.5f, avg: %.5f, max: %.5f %n", statsAbove1E3f.getCount(), statsAbove1E3f.getCount() / (double) population * 100d, statsAbove1E3f.getMin(), statsAbove1E3f.getAverage(), statsAbove1E3f.getMax());
	}

	private static class Fabiko {

		private static FabrikChain3f createUnconstrainedChain(float boneLength, int boneCount) {
			Vector3fc direction = new Vector3f(1, 0, 0);

			FabrikChain3f.ConsecutiveBoneBuilder builder = FabrikChain3f.builder()
					.addFreelyRotatingRotorBaseBone(new Vector3f(), direction, boneLength, true);

			for (int i = 1; i < boneCount; i++) {
				builder.addFreelyRotatingRotorBone(direction, boneLength, true);
			}

			return builder.build();
		}

		private static FabrikChain3f createLocalRotorChain(float boneLength, int boneCount, float constraintAngleRadians) {
			Vector3fc direction = new Vector3f(1, 0, 0);

			FabrikChain3f.ConsecutiveBoneBuilder builder = FabrikChain3f.builder()
					.addRotorConstrainedBaseBone(new Vector3f(), direction, boneLength, constraintAngleRadians, true);

			for (int i = 1; i < boneCount; i++) {
				builder.addRotorConstrainedBone(direction, boneLength, constraintAngleRadians, true);
			}

			return builder.build();
		}

		private static FabrikChain3f createLocalHingeChain(float boneLength, int boneCount) {
			Vector3fc direction = new Vector3f(1, 0, 0);
			Vector3fc rotationAxis = new Vector3f(1, 0, 0);

			FabrikChain3f.ConsecutiveBoneBuilder builder = FabrikChain3f.builder()
					.addFreelyRotatingHingeBaseBone(new Vector3f(), direction, boneLength, rotationAxis, true);

			for (int i = 1; i < boneCount; i++) {
				builder.addFreelyRotatingHingeBone(direction, boneLength, rotationAxis, true);
			}

			return builder.build();
		}

		private static ChainSolution solveChain(FabrikChain3f chain, int cycles, Random rand) {
			FabrikSolver3f solver = new FabrikSolver3f();

			// Get half the length of the chain (to ensure target can be reached)
			float halfLength = chain.getLength() / 2f;

			for (int i = 0; i < cycles; i++) {
				float x = rand.nextFloat(-halfLength, halfLength);
				float y = rand.nextFloat(-halfLength, halfLength);
				float z = rand.nextFloat(-halfLength, halfLength);
				solver.solveForTarget(chain, x, y, z);
			}

			return ChainSolution.from(chain);
		}

	}

	private static class Caliko {

		private static FabrikChain3D createUnconstrainedChain(float boneLength, int boneCount) {
			Vec3f direction = new Vec3f(1, 0, 0); //right

			FabrikChain3D chain = new FabrikChain3D();
			FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
			chain.addBone(baseBone);

			for (int i = 1; i < boneCount; i++) {
				chain.addConsecutiveBone(direction, boneLength);
			}

			return chain;
		}

		private static FabrikChain3D createLocalRotorChain(float boneLength, int boneCount, float constraintAngleDegrees) {
			Vec3f direction = new Vec3f(1, 0, 0); //right

			FabrikChain3D chain = new FabrikChain3D();
			FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
			chain.addBone(baseBone);
			chain.setRotorBaseboneConstraint(FabrikChain3D.BaseboneConstraintType3D.LOCAL_ROTOR, direction, constraintAngleDegrees);

			for (int i = 1; i < boneCount; i++) {
				chain.addConsecutiveRotorConstrainedBone(direction, boneLength, constraintAngleDegrees);
			}

			return chain;
		}

		private static FabrikChain3D createLocalHingeChain(float boneLength, int boneCount) {
			Vec3f direction = new Vec3f(1, 0, 0); //right
			Vec3f rotationAxis = new Vec3f(1, 0, 0);

			FabrikChain3D chain = new FabrikChain3D();
			FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
			chain.addBone(baseBone);
			chain.setFreelyRotatingLocalHingedBasebone(rotationAxis);
			CalikoHack.setBaseboneRelativeConstraintUV(chain, chain.getBaseboneConstraintUV());

			for (int i = 1; i < boneCount; i++) {
				chain.addConsecutiveFreelyRotatingHingedBone(direction, boneLength, FabrikJoint3D.JointType.LOCAL_HINGE, rotationAxis);
			}

			return chain;
		}

		private static ChainSolution solveChain(FabrikChain3D chain, int cycles, Random rand) {
			// Get half the length of the chain (to ensure target can be reached)
			float halfLength = chain.getChainLength() / 2f;

			for (int i = 0; i < cycles; i++) {
				float x = rand.nextFloat(-halfLength, halfLength);
				float y = rand.nextFloat(-halfLength, halfLength);
				float z = rand.nextFloat(-halfLength, halfLength);
				chain.solveForTarget(x, y, z);
			}

			return ChainSolution.from(chain);
		}

	}

}