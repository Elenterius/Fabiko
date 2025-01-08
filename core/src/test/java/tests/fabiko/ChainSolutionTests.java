package tests.fabiko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;
import au.edu.federation.caliko.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tests.fabiko.util.ChainSolution;
import tests.fabiko.util.TestAssets;

import java.io.File;

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

		solveChain(chain);

		ChainSolution solution = ChainSolution.from(chain);

		File file = new File(tempDir, "serialization_test.json");
		TestAssets.serializeJSON(solution, file);

		ChainSolution deserializedSolution = TestAssets.deserializeJSON(file, ChainSolution.class);

		float differenceError = ChainSolution.differenceError(solution, deserializedSolution);
		System.out.println("Total Difference Error: " + differenceError);

		assertEquals(solution, deserializedSolution);
	}

	private void solveChain(FabrikChain3D chain) throws Exception {
		Utils.setSeed((int) System.currentTimeMillis());

		// Get half the length of the chain (to ensure target can be reached)
		float halfLength = chain.getChainLength() / 2f;

		float x = Utils.randRange(-halfLength, halfLength);
		float y = Utils.randRange(-halfLength, halfLength);
		float z = Utils.randRange(-halfLength, halfLength);

		chain.solveForTarget(x, y, z);
	}

}
