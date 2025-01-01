package au.edu.federation.caliko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;
import au.edu.federation.caliko.utils.SerializationUtil;
import au.edu.federation.caliko.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jsalvo / alansley
 */
public class SerializationTests {

	@TempDir
	public File tempDir;

	/**
	 * Unit-test that we can successfully serialize and deserialize a chain
	 */
	@Test
	public void testSerialization() throws Exception {
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

		File file = new File(tempDir, "SerializationTest.bin");
		SerializationUtil.serializeChain(chain, file);

		FabrikChain3D deserializedChain = SerializationUtil.deserializeChain(file, FabrikChain3D.class);

		assertEquals(chain, deserializedChain);
	}

	/**
	 * Unit-test to ensure we can deserialize a chain
	 */
	@Test
	public void deserializeFabrikChain3DFromBinaryFile() throws Exception {
		InputStream inputStream = SerializationUtil.class.getResourceAsStream("/serialized/fabrikchain-1.bin");

		if (inputStream == null) {
			System.out.println("is IS NULL =///");
		}

		FabrikChain3D deserializedChain = SerializationUtil.deserializeChain(inputStream, FabrikChain3D.class);
		assertNotNull(deserializedChain);
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
