package tests.util;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.utils.Vec3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChainSolutionTests {

	@TempDir
	public File tempDir;

	@Test
	public void testJSONSerialization() throws Exception {
		Vec3f RIGHT = new Vec3f(1f, 0f, 0f);
		float boneLength = 10f;
		int bonesToAdd = 100;

		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
		chain.addBone(baseBone);

		for (int i = 1; i < bonesToAdd; i++) {
			chain.addConsecutiveBone(RIGHT, boneLength);
		}

		solveChainForRandomTarget(chain, new Random());

		ChainSolution expectedSolution = ChainSolution.from(chain);

		File file = new File(tempDir, "serialization_test.json");
		TestAssets.serializeJSON(expectedSolution, file);

		ChainSolution deserializedSolution = TestAssets.deserializeJSON(file, ChainSolution.class);

		ChainSolution.ErrorStats report = ChainSolution.getErrorStats(expectedSolution, deserializedSolution);
		System.out.printf("%nReport: %s%n", report);

		assertEquals(expectedSolution, deserializedSolution);
	}

	private void solveChainForRandomTarget(FabrikChain3D chain, Random rand) {
		// Get half the length of the chain (to ensure target can be reached)
		float halfLength = chain.getChainLength() / 2f;

		float x = rand.nextFloat(-halfLength, halfLength);
		float y = rand.nextFloat(-halfLength, halfLength);
		float z = rand.nextFloat(-halfLength, halfLength);

		chain.solveForTarget(x, y, z);
	}

}
