package au.edu.federation.caliko;

import au.edu.federation.caliko.core.FabrikBone3D;
import au.edu.federation.caliko.core.FabrikChain3D;
import au.edu.federation.caliko.math.Vec3f;
import au.edu.federation.caliko.utils.SerializationUtil;
import au.edu.federation.caliko.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FabrikChain3DTests {

    static boolean VERBOSE = true;

    static String TEST_ASSETS_PATH_TEMPLATE = "/assets/serialized/fabrikchain-%d.bin";

    static int totalCycles = 10;
    static int iterationsPerCycle = 50;
    static int bonesToAddPerCycle = 100;

    /**
     * verify that forward pass for 1 bone chains with local hinges works
     */
    @Test
    public void solveChainWith1Bone_LocalHingeConstrained() {
        float boneLength = 10f;
        Vec3f RIGHT = new Vec3f(1f, 0f, 0f);

        FabrikChain3D chain = new FabrikChain3D();
        FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
        chain.addBone(baseBone);
        chain.setFreelyRotatingLocalHingedBasebone(RIGHT);

        double averageMS = solveChain(chain, iterationsPerCycle);
        if (VERBOSE) System.out.printf("Average solve duration: %s ms%n", averageMS);
    }

    @Test
    public void solveChain_Unconstrained() throws Exception {
        FabrikChain3D chain = solveChain(1);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream(TEST_ASSETS_PATH_TEMPLATE.formatted(1));
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    @Test
    public void solveChain_RotorConstrained_45deg() throws Exception {
        FabrikChain3D chain = solveChain(2);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream(TEST_ASSETS_PATH_TEMPLATE.formatted(2));
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    @Test
    public void solveChain_RotorConstrained_90deg() throws Exception {
        FabrikChain3D chain = solveChain(3);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream(TEST_ASSETS_PATH_TEMPLATE.formatted(3));
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    FabrikChain3D solveChain(int testNumber) {
        // Set a fixed random seed for repeatability across cycles
        Utils.setSeed(123);

        if (VERBOSE) {
            String testDescription = switch (testNumber) {
                case 1 -> "Test 1: Unconstrained 3D chain.";
                case 2 -> "Test 2: Rotor constrained 3D chain - 45 degrees.";
                case 3 -> "Test 3: Rotor constrained 3D chain - 90 degrees.";
                default -> "";
            };

            System.out.println("----- " + testDescription + " -----");
        }

        Vec3f RIGHT = new Vec3f(1f, 0f, 0f);
        float boneLength = 10f;

        // initial chain setup requires a base bone
        FabrikChain3D chain = new FabrikChain3D();
        FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
        chain.addBone(baseBone);

        for (int i = 1; i < bonesToAddPerCycle; i++) {
            switch (testNumber) {
                case 1:
                    chain.addConsecutiveBone(RIGHT, boneLength);
                    break;
                case 2:
                    chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 45f);
                    break;
                case 3:
                    chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 90f);
                    break;
            }
        }

        if (VERBOSE) System.out.printf("Cycle 1 - %d bones...%n", chain.getNumBones());
        double averageMS = solveChain(chain, iterationsPerCycle);
        if (VERBOSE) System.out.printf("Average solve duration: %s ms%n", averageMS);

        for (int cycle = 1; cycle < totalCycles; cycle++) {
            for (int i = 0; i < bonesToAddPerCycle; i++) {
                switch (testNumber) {
                    case 1:
                        chain.addConsecutiveBone(RIGHT, boneLength);
                        break;
                    case 2:
                        chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 45f);
                        break;
                    case 3:
                        chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 90f);
                        break;
                }
            }

            if (VERBOSE) System.out.printf("Cycle %d - %d bones...%n", cycle + 1, chain.getNumBones());
            averageMS = solveChain(chain, iterationsPerCycle);
            if (VERBOSE) System.out.printf("Average solve duration: %s ms%n", averageMS);
        }

	    return chain;
    }

    private double solveChain(FabrikChain3D chain, int iterations) {
        float averageSolveDistance = 0f;

        // Get half the length of the chain (to ensure target can be reached)
        float halfLength = chain.getChainLength() / 2f;

        Vec3f target = new Vec3f();

        long totalMicroseconds = 0L;

        for (int i = 0; i < iterations; i++) {
            target.set(Utils.randRange(-halfLength, halfLength), Utils.randRange(-halfLength, halfLength), Utils.randRange(-halfLength, halfLength));

            long startTime = System.nanoTime();

            averageSolveDistance += chain.solveForTarget(target);

            long elapsedTime = System.nanoTime() - startTime;
            totalMicroseconds += elapsedTime / 1000L;
        }

        // Calculate and display average solve duration for this chain across all iterations
        long averageMicrosecondsPerIteration = totalMicroseconds / (long) iterations;
        double averageMilliseconds = averageMicrosecondsPerIteration / 1000d;

        averageSolveDistance /= iterations;
        if (VERBOSE) System.out.println("Average solve distance: " + averageSolveDistance);

        return averageMilliseconds;
    }

}
