package tests.util;

import au.edu.federation.caliko.FabrikChain3D;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tests.caliko.CalikoChainTests;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class TestAssets {

	private static final String ASSETS_PATH = "/assets";
	private static final String EXPORT_PATH = "core/src/test/resources" + ASSETS_PATH;

	public static void main(String[] args) {
		System.out.println("\nGenerate Test Assets...");
		overrideExpectedChainSolutions();
	}

	private static void overrideExpectedChainSolutions() {
		System.out.printf("-> overriding expected solutions for %s%n", CalikoChainTests.class.getSimpleName());

		CalikoChainTests.ChainConfigurations.VERBOSE = false;
		for (CalikoChainTests.ChainConfigurations.Configuration configuration : CalikoChainTests.ChainConfigurations.ALL) {
			File file = new File("%s/expected_solutions/%s.json".formatted(EXPORT_PATH, configuration.name));
			try {
				file.getParentFile().mkdirs();

				FabrikChain3D solvedChain = configuration.solveChain();
				ChainSolution solution = ChainSolution.from(solvedChain);

				serializeJSON(solution, file);
			}
			catch (IOException e) {
				System.out.println("Failed to export " + file.getPath());
				throw new RuntimeException(e);
			}
		}
	}

	public static ChainSolution getExpectedChainSolution(String name) throws IOException, ClassNotFoundException {
		String path = ASSETS_PATH + "/expected_solutions/" + name + ".json";

		InputStream is = TestAssets.class.getResourceAsStream(path);
		if (is == null) {
			throw new FileNotFoundException("Could not find test resource: " + path);
		}

		return deserializeJSON(is, ChainSolution.class);
	}

	public static <T> T deserializeJSON(File inputFile, final Class<T> type) throws FileNotFoundException {
		return deserializeJSON(new FileInputStream(inputFile), type);
	}

	public static <T> T deserializeJSON(InputStream input, final Class<T> type) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), type);
	}

	public static void serializeJSON(Object object, File outputFile) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try (FileWriter fw = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
			gson.toJson(object, object.getClass(), fw);
		}
	}

}
