package benchmarks.fabiko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 8, time = 1)
@Measurement(iterations = 40, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CloneIKChainBenchmarks {

	List<FabrikBone3D> ikChainList;
	FabrikBone3D[] ikChainArray;

	private static List<FabrikBone3D> setupChain(int bones, float boneLength) {
		Random random = new Random();
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), RIGHT.times(boneLength));
		chain.addBone(baseBone);

		for (int i = 1; i < bones; i++) {
			chain.addConsecutiveBone(RIGHT, boneLength);
		}

		float halfLength = chain.getChainLength() / 2f;
		float x = random.nextFloat(-halfLength, halfLength);
		float y = random.nextFloat(-halfLength, halfLength);
		float z = random.nextFloat(-halfLength, halfLength);

		chain.solveForTarget(x, y, z);

		return chain.getChain();
	}

	@Setup
	public void setup() {
		ikChainList = setupChain(1000, 10f);
		ikChainArray = ikChainList.toArray(FabrikBone3D[]::new);
	}

	@TearDown
	public void tearDown() {
		ikChainList = null;
	}

	@Benchmark
	public List<FabrikBone3D> cloneList() {
		List<FabrikBone3D> ikChain = ikChainList;

		List<FabrikBone3D> clonedChain = null;
		for (int iteration = 0; iteration < 20; iteration++) {
			clonedChain = new ArrayList<>(ikChainList.size());
			for (FabrikBone3D bone : ikChainList) {
				clonedChain.add(new FabrikBone3D(bone));
			}
		}

		ikChain = clonedChain;
		return ikChain;
	}

	@Benchmark
	public List<FabrikBone3D> cloneArray() {
		List<FabrikBone3D> ikChain = ikChainList;

		FabrikBone3D[] clonedChain = new FabrikBone3D[ikChainArray.length];
		for (int iteration = 0; iteration < 20; iteration++) {
			for (int i = 0; i < ikChainArray.length; i++) {
				FabrikBone3D bone = ikChainArray[i];
				clonedChain[i] = new FabrikBone3D(bone);
			}
		}

		ikChain = new ArrayList<>(ikChainArray.length);
		ikChain.addAll(Arrays.asList(clonedChain));

		return ikChain;
	}

	@Benchmark
	public List<FabrikBone3D> cloneListToArray() {
		List<FabrikBone3D> ikChain = ikChainList;

		FabrikBone3D[] clonedChain = new FabrikBone3D[ikChainList.size()];
		for (int iteration = 0; iteration < 20; iteration++) {
			for (int i = 0; i < ikChainList.size(); i++) {
				FabrikBone3D bone = ikChainList.get(i);
				clonedChain[i] = new FabrikBone3D(bone);
			}
		}

		ikChain = new ArrayList<>(Arrays.asList(clonedChain));

		return ikChain;
	}

}
