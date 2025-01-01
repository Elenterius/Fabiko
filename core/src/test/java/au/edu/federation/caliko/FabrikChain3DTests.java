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

public class FabrikChain3DTests {

    // We'll run this many cycles
    static int totalCycles = 10;

    // Each run will solve the chain for a pseudo-random location this many times
    static int iterationsPerCycle = 50;

    // Number of bones to add per cycle
    static int bonesToAdd = 100;

    @TempDir
    File tempDir;

    @Test
    public void solveUnconstrainedChain() throws Exception {
        FabrikChain3D chain = solveChain(1);

        File file = new File("out/serialized/fabrikchain-" + 1 + ".bin");
        file.getParentFile().mkdirs();
        SerializationUtil.serializeChain(chain, file);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream("/serialized/fabrikchain-" + 1 + ".bin");
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    @Test
    public void solveRotor45degConstrained3dChain() throws Exception {
        FabrikChain3D chain = solveChain(2);

        File file = new File("out/serialized/fabrikchain-" + 2 + ".bin");
        file.getParentFile().mkdirs();
        SerializationUtil.serializeChain(chain, file);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream("/serialized/fabrikchain-" + 2 + ".bin");
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    @Test
    public void solveRotor90degConstrained3dChain() throws Exception {
        FabrikChain3D chain = solveChain(3);

        File file = new File("out/serialized/fabrikchain-" + 3 + ".bin");
        file.getParentFile().mkdirs();
        SerializationUtil.serializeChain(chain, file);

        InputStream is = FabrikChain3DTests.class.getResourceAsStream("/serialized/fabrikchain-" + 3 + ".bin");
        FabrikChain3D expectedChain = SerializationUtil.deserializeChain(is, FabrikChain3D.class);

        assertEquals(expectedChain, chain);
    }

    private FabrikChain3D solveChain(int testNumber) {
        // Set a fixed random seed for repeatability across cycles
        Utils.setSeed(123);

        String testDescription = switch (testNumber) {
            case 1 -> "Test 1: Unconstrained 3D chain.";
            case 2 -> "Test 2: Rotor constrained 3D chain - 45 degrees.";
            case 3 -> "Test 3: Rotor constrained 3D chain - 90 degrees.";
            default -> "";
        };

        System.out.println("----- " + testDescription + " -----");

        Vec3f RIGHT = new Vec3f(1f, 0f, 0f);
        float boneLength = 10f;

        // initial chain setup requires a base bone
        FabrikChain3D chain = new FabrikChain3D();
        FabrikBone3D baseBone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0f, 0f));
        chain.addBone(baseBone);

        for (int i = 1; i < bonesToAdd; i++) {
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
        System.out.println("Cycle 1 - " + chain.getNumBones() + " bones.");
        double averageMS = solveChain(chain, iterationsPerCycle);
        System.out.println("Average solve duration (Milliseconds): " + averageMS);

        // Cycles 1 onward...
        for (int cycle = 1; cycle < totalCycles; cycle++) {
            for (int i = 0; i < bonesToAdd; i++) {
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

            // Run test
            System.out.println("Cycle " + (cycle + 1) + " - " + chain.getNumBones() + " bones.");
            averageMS = solveChain(chain, iterationsPerCycle);
            System.out.println("Average solve duration (Milliseconds): " + averageMS);
        }

	    return chain;
    }

    private double solveChain(FabrikChain3D chain, int iterations) {
        float averageSolveDistance = 0f;

        // Get half the length of the chain (to ensure target can be reached)
        float length = chain.getChainLength() / 2f;

        Vec3f target = new Vec3f();

        long totalMicroseconds = 0L;
        double averageMilliseconds;

        for (int i = 0; i < iterations; i++) {
            target.set(Utils.randRange(-length, length), Utils.randRange(-length, length), Utils.randRange(-length, length));

            long startTime = System.nanoTime();

            averageSolveDistance += chain.solveForTarget(target);

            long elapsedTime = System.nanoTime() - startTime;

            // Increment total time for this cycle
            totalMicroseconds += elapsedTime / 1000;
        }

        // Calculate and display average solve duration for this chain across all iterations
        long averageMicrosecondsPerIteration = totalMicroseconds / (long) iterations;
        averageMilliseconds = (double) averageMicrosecondsPerIteration / 1000f;

        averageSolveDistance /= iterations;
        System.out.println("Average solve distance: " + averageSolveDistance);

        return averageMilliseconds;
    }

}
