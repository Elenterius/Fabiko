package benchmarks.fabiko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.core.FabrikJoint3D;
import au.edu.federation.caliko.math.Vec3f;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 8, time = 1)
@Measurement(iterations = 50, time = 5, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(2)
public class FabrikChain3DSolverBenchmarks {

	@Param({"2", "3", "4", "10", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"})
	public int numberOfBones = 100;

	Random random;

	FabrikChain3D unconstrained3dChain;
	FabrikChain3D rotor45degConstrained3dChain;
	FabrikChain3D rotor90degConstrained3dChain;
	FabrikChain3D localHingeConstrainedChain;

	@Setup
	public void setup() {
		random = new Random(123);

		float boneLength = 10f;
		unconstrained3dChain = createUnconstrainedChain(numberOfBones, boneLength);
		rotor45degConstrained3dChain = createRotorConstrainedChain(numberOfBones, boneLength, 45f);
		rotor90degConstrained3dChain = createRotorConstrainedChain(numberOfBones, boneLength, 90f);
		localHingeConstrainedChain = createLocalHingeConstrainedChain(numberOfBones, boneLength);
	}

	@Benchmark
	public float solveUnconstrainedChain() {
		return solveChain(unconstrained3dChain);
	}

	@Benchmark
	public float solveRotor45degConstrainedChain() {
		return solveChain(rotor45degConstrained3dChain);
	}

	@Benchmark
	public float solveRotor90degConstrainedChain() {
		return solveChain(rotor45degConstrained3dChain);
	}

	@Benchmark
	public float solveLocalHingeConstrainedChain() {
		return solveChain(localHingeConstrainedChain);
	}


	private float solveChain(FabrikChain3D chain) {
		// Get half the length of the chain (to ensure target can be reached)
		float halfLength = chain.getChainLength() / 2f;

		float x = random.nextFloat(-halfLength, halfLength);
		float y = random.nextFloat(-halfLength, halfLength);
		float z = random.nextFloat(-halfLength, halfLength);

		return chain.solveForTarget(x, y, z);
	}

	private FabrikChain3D createUnconstrainedChain(int bonesToAdd, float boneLength) {
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

		FabrikChain3D chain = createBaseChain(RIGHT, boneLength);

		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveBone(RIGHT, boneLength);
		}
		return chain;
	}

	private FabrikChain3D createRotorConstrainedChain(int bonesToAdd, float boneLength, float constraintAngle) {
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

		FabrikChain3D chain = createBaseChain(RIGHT, boneLength);

		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, constraintAngle);
		}
		return chain;
	}

	private FabrikChain3D createLocalHingeConstrainedChain(int bonesToAdd, float boneLength) {
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

		FabrikChain3D chain = createBaseChain(RIGHT, boneLength);
		chain.setFreelyRotatingLocalHingedBasebone(RIGHT);

		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveFreelyRotatingHingedBone(RIGHT, boneLength, FabrikJoint3D.JointType.LOCAL_HINGE, RIGHT);
		}
		return chain;
	}

	private FabrikChain3D createBaseChain(Vec3f direction, float boneLength) {
		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), direction.times(boneLength));
		chain.addBone(baseBone);
		return chain;
	}

}
