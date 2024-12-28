package au.edu.federation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author jsalvo / alansley
 */
public class CalikoUnitTests {
	
	@TempDir
	public File tempDir;

	/**
	 * Unit-test that we can successfully serialize and deserialize a chain
	 */
	@Test
	public void testSerialization() throws Exception {
		Vec3f RIGHT      = new Vec3f(1.0f, 0.0f, 0.0f);
		float boneLength = 10.0f;		
		int bonesToAdd   = 100;
		
		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D basebone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0.0f, 0.0f) );
		chain.addBone(basebone);
		for (int loop = 1; loop < bonesToAdd; ++loop)
		{
			chain.addConsecutiveBone(RIGHT, boneLength);
		}
		solveChain(chain);
		
		File file = new File(tempDir, "SerializationTest.bin");
		FileOutputStream fos = new FileOutputStream(file);
		SerializationUtil.serializeChain(chain, fos);
		
		FileInputStream fis = new FileInputStream(file);
		FabrikChain3D deserializedChain = SerializationUtil.unserializeChain(fis, FabrikChain3D.class);
		
		assertEquals(chain, deserializedChain);
	}

	/* Unit-test to ensure we can deserialize a chain */
	@Test
	public void testUnserializing() throws Exception
	{
		InputStream is = SerializationUtil.class.getResourceAsStream("/serialization-presaved-chain-1.bin");
		
		if (is == null)
		{
			System.out.println("is IS NULL =///");
		}
		
		FabrikChain3D deserializedChain = SerializationUtil.unserializeChain(is, FabrikChain3D.class);
		assertNotNull(deserializedChain);
		
		System.out.println("*** Unserialization is working ***");
	}

	/** Test for pitch / yaw functionality 
	 */
	@Test
	public void testPitchAndYaw() throws Exception
	{		
		Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
		Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);

		Vec3f boneDirection = new Vec3f(0.0f, 0.0f, -1.0f); // Bone direction (obtain via 'getDirectionUV()') points directly INTO screen
		
		// Test pitch around global X-axis in 30 degree increments		
		System.out.println("---------- Pitch ----------");
		float pitch = 0.0f;
		for (int loop = 0; loop < 12; loop++)
		{	
			boneDirection = Vec3f.rotateXDegs(boneDirection, 30.0f).normalise();
			pitch = boneDirection.getGlobalPitchDegs();			
			System.out.println("After " + (loop + 1) * 30 + " degrees rotation bone pitch is: " + pitch + " degrees. Direction: " + boneDirection);
		}
		
		// Test yaw around global Y-axis in 30 degree increments
		System.out.println("\n---------- Yaw ----------");
		float yaw = 0.0f;		
		for (int loop = 0; loop < 12; loop++)
		{	
			// Rotate bone by 30 degrees around Y-axis
			boneDirection = Vec3f.rotateYDegs(boneDirection, 30.0f).normalise();
			yaw = boneDirection.getGlobalYawDegs();
			System.out.println("After " + (loop + 1) * 30 + " degrees rotation bone yaw is: " + yaw + " degrees. Direction: " + boneDirection);
		}
	}

	void solveChain(FabrikChain3D chain) throws Exception {
		Utils.setSeed((int)System.currentTimeMillis());
		// Get half the length of the chain (to ensure target can be reached)
		float len = chain.getChainLength() / 2.0f;
		
		// Generate a new random target location
		Vec3f target = new Vec3f(Utils.randRange(-len, len), Utils.randRange(-len, len), Utils.randRange(-len, len));
		
		// Solve for target
		chain.solveForTarget(target);
	}
	
	
}
