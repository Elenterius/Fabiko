package au.edu.federation.caliko;

import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.utils.SerializationUtil;

import java.io.File;
import java.io.IOException;

public class TestAssetsGenerator {

	static String TEST_RESOURCES_PATH = "core/src/test/resources";

	public static void main(String[] args) {
		overrideFabrikChainTestAssets();
	}

	private static void overrideFabrikChainTestAssets() {
		System.out.printf("-> overriding test assets of %s%n", FabrikChain3DTests.class.getSimpleName());

		FabrikChain3DTests.VERBOSE = false;
		FabrikChain3DTests test = new FabrikChain3DTests();

		for (int i = 1; i < 4; i++) {
			try {
				FabrikChain3D chain = test.solveChain(i);
				File file = new File(TEST_RESOURCES_PATH + FabrikChain3DTests.TEST_ASSETS_PATH_TEMPLATE.formatted(i));
				file.getParentFile().mkdirs();
				SerializationUtil.serializeChain(chain, file);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
