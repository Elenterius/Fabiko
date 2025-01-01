package au.edu.federation.caliko.utils;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Class  : A series of static utility / helper methods to perform common operations.
 * Version: 0.4
 * Date   : 04/12/2015
 */
public final class Utils {

	// Define a private static DecimalFormat to be used by our toString() method.
	// Note: '0' means put a 0 there if it's zero, '#' means omit if zero.
	public static final DecimalFormat df = new DecimalFormat("0.000");

	/**
	 * Newline character for this system.
	 */
	public static final String NEW_LINE = System.lineSeparator();

	/**
	 * Some colours with which to draw things.
	 * <p>
	 * These colours are neither final nor 'constant', so they can be user modified at runtime if desired.
	 */
	public static final Colour4f RED = new Colour4f(1.0f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f GREEN = new Colour4f(0.0f, 1.0f, 0.0f, 1.0f);
	public static final Colour4f BLUE = new Colour4f(0.0f, 0.0f, 1.0f, 1.0f);
	public static final Colour4f MID_RED = new Colour4f(0.6f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f MID_GREEN = new Colour4f(0.0f, 0.6f, 0.0f, 1.0f);
	public static final Colour4f MID_BLUE = new Colour4f(0.0f, 0.0f, 0.6f, 1.0f);
	public static final Colour4f BLACK = new Colour4f(0.0f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f GREY = new Colour4f(0.5f, 0.5f, 0.5f, 1.0f);
	public static final Colour4f WHITE = new Colour4f(1.0f, 1.0f, 1.0f, 1.0f);
	public static final Colour4f YELLOW = new Colour4f(1.0f, 1.0f, 0.0f, 1.0f);
	public static final Colour4f CYAN = new Colour4f(0.0f, 1.0f, 1.0f, 1.0f);
	public static final Colour4f MAGENTA = new Colour4f(1.0f, 0.0f, 1.0f, 1.0f);

	/**
	 * The maximum length in characters of any names which may be used for bones, chains or structures.
	 */
	public static final int MAX_NAME_LENGTH = 100;

	/**
	 * A Random object used to generate random numbers in the randRange methods.
	 * <p>
	 * If you want a reproducable sequence of events, then set a seed value using Utils.setRandomSeed(someValue).
	 *
	 * @see #setRandomSeed(int)
	 * @see #randRange(float, float)
	 * @see #randRange(int, int)
	 */
	public static Random random = new Random();

	private Utils() {
	}

	/**
	 * Set a fixed seed value - call this with any value before starting the inverse kinematics runs to get a repeatable sequence of events.
	 *
	 * @param seedValue The seed value to set.
	 */
	public static void setRandomSeed(int seedValue) {
		random = new Random(seedValue);
	}

	/**
	 * Return a random floating point value between the half-open range [min..max).
	 * <p>
	 * This means that, for example, a call to {@code randRange(-5.0f, 5.0f)} may return a value between -5.0f up to a maximum of 4.999999f.
	 *
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return A random float within the specified half-open range.
	 */
	public static float randRange(float min, float max) {
		return random.nextFloat() * (max - min) + min;
	}

	/**
	 * Return a random integer value between the half-open range (min..max]
	 * <p>
	 * This means that, for example, a call to {@code randRange(-5, 5)} will return a value between -5 up to a maximum of 4.
	 *
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return A random int within the specified half-open range.
	 */
	public static int randRange(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	/**
	 * Method to set a provided seed value to be used for random number generation.
	 * <p>
	 * This allows you to have a repoducable sequence of pseudo-random numbers which are used by
	 * the visualisation MovingTarget class.
	 *
	 * @param seed The seed value
	 */
	public static void setSeed(int seed) {
		random = new Random(seed);
	}

	/**
	 * Return the given name capped at 100 characters, if necessary.
	 *
	 * @param name The name to validate.
	 * @return The given name capped at 100 characters, if necessary.
	 */
	public static String getValidatedName(String name) {
		if (name.length() < Utils.MAX_NAME_LENGTH) {
			return name;
		}
		return name.substring(0, Utils.MAX_NAME_LENGTH);
	}

}
