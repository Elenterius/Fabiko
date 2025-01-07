/// The MIT License (MIT)
///
/// Copyright (c) 2016-2020 Alastair Lansley / Federation University Australia
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.

package com.github.elenterius.fabiko.visualization;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Random;

/**
 * Copied from the Caliko library.
 * <p>
 * Class to move a target around in 3D.
 * <p>
 * The location can be used as the to solve FabrikChain3D / FabrikStructure3D objects.
 *
 * @author Al Lansley
 */
public class MovingTarget {

	private static final Vector3f LINE_TOP = new Vector3f();
	private static final Vector3f LINE_BOTTOM = new Vector3f();

	/**
	 * Current location of the target.
	 */
	private final Vector3f currentLocation;

	/**
	 * Location of the waypoint this target is moving towards.
	 */
	private final Vector3f waypointLocation;

	/**
	 * How far to 'step' each frame to reach our destination.
	 */
	private final Vector3f stepValue;

	/**
	 * The center of the X/Y/Z extent about which waypoints may be chosen,
	 */
	private final Vector3f center;

	/**
	 * The Point3D used to draw the current location of this MovingTarget3D.
	 */
	private final PointDrawHelper point;

	/**
	 * A Line3D used so we can draw a vertical line through the current target position to assist in depth perception.
	 */
	private final LineDrawHelper depthLineDrawHelper;

	/**
	 * The extent of the vertical line drawn through the current target location in world-space units.
	 */
	private final float depthLineHeight;

	/**
	 * How far +/- the mCenter can our next waypoint be i.e. (50, 100, 150) would mean
	 * a range of 100 on the x (-50 to +50), a range of 200 on the y (-100 to + 100)
	 * and a range of 300 on the z (-150 to + 150) - all these values are mRanges around
	 * our mCenter point.
	 */
	private final Vector3f ranges;

	/**
	 * How many steps it should take to move from one waypoint to the next waypoint.
	 */
	private final int numSteps;

	/**
	 * Whether the movement between waypoints is paused or not.
	 *
	 * @default false
	 */
	private boolean isPaused = false;

	private final Random random;

	/**
	 * @param center          The center point about which new waypoints are generated.
	 * @param ranges          The X/Y/Z range about which new waypoints are generated.
	 * @param numSteps        The number of steps we should use to traverse from one waypoint to the next.
	 * @param depthLineHeight The height (on the Y-axis) of the vertical line passing through the target.
	 */
	public MovingTarget(Vector3f center, Vector3f ranges, int numSteps, float depthLineHeight, Random random) {
		this.center = center;
		this.ranges = ranges;
		this.numSteps = numSteps;
		this.depthLineHeight = depthLineHeight;

		this.random = random;

		currentLocation = new Vector3f();
		currentLocation.x = random.nextFloat(this.center.x - this.ranges.x, this.center.x + this.ranges.x);
		currentLocation.y = random.nextFloat(this.center.y - this.ranges.y, this.center.y + this.ranges.y);
		currentLocation.z = random.nextFloat(this.center.z - this.ranges.z, this.center.z + this.ranges.z);

		waypointLocation = new Vector3f();
		waypointLocation.x = random.nextFloat(this.center.x - this.ranges.x, this.center.x + this.ranges.x);
		waypointLocation.y = random.nextFloat(this.center.y - this.ranges.y, this.center.y + this.ranges.y);
		waypointLocation.z = random.nextFloat(this.center.z - this.ranges.z, this.center.z + this.ranges.z);

		// Calculate the step value to get to our waypoint in 'numSteps' steps
		stepValue = new Vector3f();
		stepValue.x = (waypointLocation.x - currentLocation.x) / numSteps;
		stepValue.y = (waypointLocation.y - currentLocation.y) / numSteps;
		stepValue.z = (waypointLocation.z - currentLocation.z) / numSteps;

		point = new PointDrawHelper();
		depthLineDrawHelper = new LineDrawHelper();
	}

	/**
	 * Step from our current location to our waypoint location.
	 * <p>
	 * Does nothing if the mPaused property is true.
	 *
	 * @see #pause()
	 * @see #resume()
	 */
	public void step() {
		if (isPaused) return;

		currentLocation.add(stepValue);

		// Generate a new waypoint when we're within one unit of the current waypoint
		float arrivalDistance = 1f;
		if ((Math.abs(currentLocation.x - waypointLocation.x) < arrivalDistance) && (Math.abs(currentLocation.y - waypointLocation.y) < arrivalDistance) && (Math.abs(currentLocation.z - waypointLocation.z) < arrivalDistance)) {
			waypointLocation.x = random.nextFloat(center.x - ranges.x, center.x + ranges.x);
			waypointLocation.y = random.nextFloat(center.y - ranges.y, center.y + ranges.y);
			waypointLocation.z = random.nextFloat(center.z - ranges.z, center.z + ranges.z);

			// Recalculate our step value for the new waypoint location
			stepValue.x = (waypointLocation.x - currentLocation.x) / numSteps;
			stepValue.y = (waypointLocation.y - currentLocation.y) / numSteps;
			stepValue.z = (waypointLocation.z - currentLocation.z) / numSteps;
		}
	}

	/**
	 * Draw this target.
	 *
	 * @param colour              The colour to draw this target.
	 * @param pointSize           The size of the point used to draw this target in pixels.
	 * @param modelViewProjection The ModelViewProjection matrix used to draw this target.
	 */
	public void draw(Color4fc colour, float pointSize, Matrix4fc modelViewProjection) {
		point.draw(currentLocation, colour, pointSize, modelViewProjection);

		// ...and the line that assists in recognising the depth of the target location.
		LINE_TOP.set(currentLocation.x, depthLineHeight, currentLocation.z);
		LINE_BOTTOM.set(currentLocation.x, -depthLineHeight, currentLocation.z);
		depthLineDrawHelper.draw(LINE_TOP, LINE_BOTTOM, 1f, modelViewProjection);
	}

	/**
	 * Return the current location of this target.
	 *
	 * @return the current location of this target
	 */
	public Vector3fc getCurrentLocation() {
		return currentLocation;
	}

	/**
	 * Pause movement of the target.
	 *
	 * @see #resume()
	 */
	public void pause() {
		isPaused = true;
	}

	/**
	 * Resume movement of the target after it has been paused.
	 *
	 * @see #pause()
	 */
	public void resume() {
		isPaused = false;
	}

	/**
	 * Toggle pause flag between false and true or vice versa.
	 */
	public void togglePause() {
		isPaused = !isPaused;
	}

}