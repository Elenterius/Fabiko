package tests.fabiko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.core.FabrikJoint3D;
import au.edu.federation.caliko.math.Vec3f;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import tests.fabiko.util.ChainSolution;
import tests.fabiko.util.TestAssets;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FabrikChain3DTests {

	/**
	 * verify that forward pass for 1 bone chains with local hinges works
	 */
	@Test
	public void solve_chain_with_1_bone_local_hinge_constraint() {
		float boneLength = 10f;
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
		chain.addBone(baseBone);
		chain.setFreelyRotatingLocalHingedBasebone(RIGHT);

		Random rand = new Random();
		float halfLength = chain.getChainLength() / 2f;

		for (int i = 0; i < 10; i++) {
			float x = rand.nextFloat(-halfLength, halfLength);
			float y = rand.nextFloat(-halfLength, halfLength);
			float z = rand.nextFloat(-halfLength, halfLength);
			chain.solveForTarget(x, y, z);
		}
	}

	@TestFactory
	public Stream<DynamicTest> solve_chains() {
		Stream<ChainConfigurations.Configuration> inputStream = ChainConfigurations.ALL.stream();
		Function<ChainConfigurations.Configuration, String> displayNameGenerator = configuration -> configuration.name;

		ThrowingConsumer<ChainConfigurations.Configuration> testExecutor = configuration -> {
			ChainSolution actualIKSolution = ChainSolution.from(configuration.solveChain());
			ChainSolution expectedIKSolution = TestAssets.getExpectedChainSolution(configuration.name);

			float differenceError = ChainSolution.differenceError(expectedIKSolution, actualIKSolution);
			System.out.println("\nTotal Difference Error: " + differenceError);

			assertEquals(expectedIKSolution, actualIKSolution);
		};

		return DynamicTest.stream(inputStream, displayNameGenerator, testExecutor);
	}

	public static class ChainConfigurations {

		public static final Set<Configuration> ALL = Set.of(
				new Configuration("chain_unconstrained") {
					@Override
					public void addBoneTo(FabrikChain3D chain) {
						chain.addConsecutiveBone(direction, boneLength);
					}
				},
				new Configuration("chain_local_hinge_constraints") {
					@Override
					protected FabrikChain3D createChain() {
						FabrikChain3D chain = super.createChain();
						chain.setFreelyRotatingLocalHingedBasebone(direction);
						return chain;
					}

					@Override
					public void addBoneTo(FabrikChain3D chain) {
						chain.addConsecutiveFreelyRotatingHingedBone(direction, boneLength, FabrikJoint3D.JointType.LOCAL_HINGE, direction);
					}
				},
				new Configuration("chain_with_45deg_rotor_constraints") {
					@Override
					public void addBoneTo(FabrikChain3D chain) {
						chain.addConsecutiveRotorConstrainedBone(direction, boneLength, 45f);
					}
				},
				new Configuration("chain_with_90deg_rotor_constraints") {
					@Override
					public void addBoneTo(FabrikChain3D chain) {
						chain.addConsecutiveRotorConstrainedBone(direction, boneLength, 90f);
					}
				}
		);
		public static boolean VERBOSE = true;

		public static abstract class Configuration {

			public final String name;
			public final Vec3f direction;
			public final float boneLength;

			public final int cycles;
			public final int iterationsPerCycle;
			public final int bonesToAddPerCycle;

			public Configuration(String name) {
				this(name, new Vec3f(1f, 0f, 0f), 10f, 10, 50, 100);
			}

			public Configuration(String name, Vec3f direction, float boneLength, int cycles, int iterationsPerCycle, int bonesToAddPerCycle) {
				this.name = name;
				this.boneLength = boneLength;
				this.direction = direction;
				this.cycles = cycles;
				this.iterationsPerCycle = iterationsPerCycle;
				this.bonesToAddPerCycle = bonesToAddPerCycle;
			}

			private static double solveChain(FabrikChain3D chain, int iterations, Random random) {
				// Get half the length of the chain (to ensure target can be reached)
				float halfLength = chain.getChainLength() / 2f;

				Vec3f target = new Vec3f();

				long totalMicroseconds = 0L;

				for (int i = 0; i < iterations; i++) {
					target.set(
							random.nextFloat(-halfLength, halfLength),
							random.nextFloat(-halfLength, halfLength),
							random.nextFloat(-halfLength, halfLength)
					);

					long startTime = System.nanoTime();

					chain.solveForTarget(target);

					long elapsedTime = System.nanoTime() - startTime;
					totalMicroseconds += elapsedTime / 1000L;
				}

				// Calculate and display average solve duration for this chain across all iterations
				long averageMicrosecondsPerIteration = totalMicroseconds / (long) iterations;
				return averageMicrosecondsPerIteration / 1000d;
			}

			protected FabrikChain3D createChain() {
				FabrikChain3D chain = new FabrikChain3D();
				FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), direction.times(boneLength));
				chain.addBone(baseBone);
				return chain;
			}

			protected abstract void addBoneTo(FabrikChain3D chain);

			public FabrikChain3D solveChain() {
				Random random = new Random(123);

				FabrikChain3D chain = createChain();

				for (int i = 1; i < bonesToAddPerCycle; i++) {
					addBoneTo(chain);
				}

				double averageMS = solveChain(chain, iterationsPerCycle, random);
				if (VERBOSE) {
					System.out.printf("Cycle,\tBones,\t\tAverage solve duration %n");
					System.out.printf("1,\t\t%d,\t\t%s ms %n", chain.getNumBones(), averageMS);
				}

				for (int cycle = 1; cycle < cycles; cycle++) {
					for (int i = 0; i < bonesToAddPerCycle; i++) {
						addBoneTo(chain);
					}

					averageMS = solveChain(chain, iterationsPerCycle, random);
					if (VERBOSE) System.out.printf("%d,\t\t%d,\t\t%s ms %n", cycle + 1, chain.getNumBones(), averageMS);
				}

				return chain;
			}
		}
	}

}
