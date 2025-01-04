package com.github.elenterius.caliko;

import au.edu.federation.caliko.math.Mat3f;
import au.edu.federation.caliko.math.Vec3f;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 30, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class RotationMatrixBenchmarks {

	Vec3f direction1;
	Vec3f direction2;
	Vec3f direction3;

	@Setup(Level.Iteration)
	public void setup() {
		Random random = new Random();
		direction1 = new Vec3f(
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f)
		).normalise();
		direction2 = new Vec3f(
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f)
		).normalise();
		direction3 = new Vec3f(
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f),
				random.nextFloat(-2, 2f)
		).normalise();
	}

	@Benchmark
	public void createRotationMatrixBaseline(Blackhole bh) {
		bh.consume(Experiments.createRotationMatrixBaseline(direction1));
		bh.consume(Experiments.createRotationMatrixBaseline(direction2));
		bh.consume(Experiments.createRotationMatrixBaseline(direction3));
	}

	@Benchmark
	public void createRotationMatrixFaster(Blackhole bh) {
		bh.consume(Experiments.createRotationMatrixFast(direction1));
		bh.consume(Experiments.createRotationMatrixFast(direction2));
		bh.consume(Experiments.createRotationMatrixFast(direction3));
	}

}

class Experiments {
	static Mat3f createRotationMatrixBaseline(Vec3f referenceDirection) {
		Mat3f rotMat = new Mat3f();

		// Singularity fix provided by meaten - see: https://github.com/FedUni/caliko/issues/19
		if (Math.abs(referenceDirection.y) > 0.9999f) {
			rotMat.setZBasis(referenceDirection);
			rotMat.setXBasis(new Vec3f(1f, 0f, 0f));
			rotMat.setYBasis(Vec3f.crossProduct(rotMat.getXBasis(), referenceDirection).normalised());
		}
		else {
			rotMat.setZBasis(referenceDirection);
			rotMat.setXBasis(Vec3f.crossProduct(referenceDirection, 0f, 1f, 0f).normalised());
			rotMat.setYBasis(Vec3f.crossProduct(rotMat.getXBasis(), referenceDirection).normalised());
		}

		return rotMat;
	}

	static Mat3f createRotationMatrixFast(Vec3f referenceDirection) {
		Mat3f rotMat = new Mat3f();

		// Singularity fix provided by meaten - see: https://github.com/FedUni/caliko/issues/19
		if (Math.abs(referenceDirection.y) > 0.9999f) {
			rotMat.setZBasis(referenceDirection);
			rotMat.m00 = 1f; // == rotMat.setXBasis(1f, 0f, 0f);
			rotMat.setYBasis(Vec3f.crossProduct(1f, 0f, 0f, referenceDirection).normalise()); // xBasis cross referenceDirection
		}
		else {
			rotMat.setZBasis(referenceDirection);
			Vec3f xBasis = Vec3f.crossProduct(referenceDirection, 0f, 1f, 0f).normalise();
			rotMat.setXBasis(xBasis);
			rotMat.setYBasis(Vec3f.crossProduct(xBasis, referenceDirection).normalise());
		}

		return rotMat;
	}
}


