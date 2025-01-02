package com.github.elenterius.caliko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 10, time = 10, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 50, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Threads(Threads.MAX)
@Fork(2)
public class FabrikChain3DSolverBenchmark {

	@Param({"100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"})
	public int numberOfBones = 100;

	Random random;

	FabrikChain3D unconstrained3dChain;
	FabrikChain3D rotor45degConstrained3dChain;
	FabrikChain3D rotor90degConstrained3dChain;

	@Setup
	public void setup() {
		random = new Random(123);

		float boneLength = 10f;
		unconstrained3dChain = createUnconstrained3dChain(numberOfBones, boneLength);
		rotor45degConstrained3dChain = createRotorConstrained3dChain(numberOfBones, boneLength, 45f);
		rotor90degConstrained3dChain = createRotorConstrained3dChain(numberOfBones, boneLength, 90f);
	}

	@Benchmark
	public float solveUnconstrained3dChain() {
		return solveChain(unconstrained3dChain);
	}

	@Benchmark
	public float solveRotor45degConstrained3dChain() {
		return solveChain(rotor45degConstrained3dChain);
	}

	@Benchmark
	public float solveRotor90degConstrained3dChain() {
		return solveChain(rotor45degConstrained3dChain);
	}

	private float solveChain(FabrikChain3D chain) {
		// Get half the length of the chain (to ensure target can be reached)
		float halfLength = chain.getChainLength() / 2f;

		float x = random.nextFloat(-halfLength, halfLength);
		float y = random.nextFloat(-halfLength, halfLength);
		float z = random.nextFloat(-halfLength, halfLength);

		return chain.solveForTarget(x, y, z);
	}

	private FabrikChain3D createUnconstrained3dChain(int bonesToAdd, float boneLength) {
		FabrikChain3D chain = createBaseChain(boneLength);

		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);
		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveBone(RIGHT, boneLength);
		}
		return chain;
	}

	private FabrikChain3D createRotorConstrained3dChain(int bonesToAdd, float boneLength, float constraintAngle) {
		FabrikChain3D chain = createBaseChain(boneLength);

		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);
		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, constraintAngle);
		}
		return chain;
	}

	private FabrikChain3D createBaseChain(float boneLength) {
		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
		chain.addBone(baseBone);
		return chain;
	}

}
