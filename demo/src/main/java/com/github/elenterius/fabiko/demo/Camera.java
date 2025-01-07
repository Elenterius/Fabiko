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

package com.github.elenterius.fabiko.demo;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/// Refactored copy from Caliko Library.
///
/// Original Author: Al Lansley
public class Camera {

	private static final Vector3f movement = new Vector3f();

	private final Matrix4f viewMatrix = new Matrix4f();

	/// How quickly the camera moves.
	private float movementSpeedFactor = 80f;

	/// How sensitive mouse movements affect looking up and down.
	private double pitchSensitivity = 0.01d;

	/// How sensitive mouse movements affect looking left and right.
	private double yawSensitivity = 0.01d;

	// Current location and orientation of the camera
	private final Vector3f location = new Vector3f();
	private final Vector3f rotationAngleRadians = new Vector3f();

	// Movement flags
	private boolean mHoldingForward = false;
	private boolean mHoldingBackward = false;
	private boolean mHoldingLeftStrafe = false;
	private boolean mHoldingRightStrafe = false;

	// The middle of the screen in window coordinates
	private double windowCenterX;
	private double windowCenterY;

	// Location of mouse cursor as of last update
	private double prevMouseX, prevMouseY;

	public Camera(Vector3f location, Vector3f rotationDegrees, int windowWidth, int windowHeight) {
		this.location.set(location);
		rotationAngleRadians.set(
				Math.toRadians(rotationDegrees.x),
				Math.toRadians(rotationDegrees.y),
				Math.toRadians(rotationDegrees.z)
		);

		windowCenterX = prevMouseX = windowWidth / 2d;
		windowCenterY = prevMouseY = windowHeight / 2d;
	}

	public Matrix4fc getViewMatrix() {
		viewMatrix.identity();

		// rotate to the orientation of the camera
		viewMatrix.rotateX(rotationAngleRadians.x);
		viewMatrix.rotateY(rotationAngleRadians.y);

		// Only rotate around Z if we have any Z-axis rotation - in FPS camera controls we do not.
		if (rotationAngleRadians.z > 0f) {
			viewMatrix.rotateZ(rotationAngleRadians.z);
		}

		viewMatrix.translate(location);

		return viewMatrix;
	}

	public void updateWindowSize(double windowWidth, double windowHeight) {
		// When using the mouse to look around, we place the mouse cursor at the centre of the window each frame
		windowCenterX = prevMouseX = windowWidth / 2d;
		windowCenterY = prevMouseY = windowHeight / 2d;
	}

	// Method called when the LMB is released - this stops the camera 'jumping' on first mouse movement after LMB down
	public void resetLastCursorPosition() {
		prevMouseX = windowCenterX;
		prevMouseY = windowCenterY;
	}

	/// Set the movement flags depending on which keys are being pressed or released
	public void handleKeyPress(int key, int action) {
		switch (key) {
			case GLFW_KEY_W -> mHoldingForward = action == GLFW_PRESS || action == GLFW_REPEAT;
			case GLFW_KEY_S -> mHoldingBackward = action == GLFW_PRESS || action == GLFW_REPEAT;
			case GLFW_KEY_A -> mHoldingLeftStrafe = action == GLFW_PRESS || action == GLFW_REPEAT;
			case GLFW_KEY_D -> mHoldingRightStrafe = action == GLFW_PRESS || action == GLFW_REPEAT;
		}
	}

	/**
	 * Method to deal with mouse position changes.
	 * <p>
	 * The pitch (up and down) and yaw (left and right) sensitivity of the camera can be modified via the setSensitivity method.
	 *
	 * @param mouseX The x location of the mouse cursor.
	 * @param mouseY The y location of the mouse cursor.
	 */
	public void handleMouseMove(double mouseX, double mouseY) {
		// Calculate our horizontal and vertical mouse movement
		// Note: Swap the mouseX/Y and lastMouseX/Y to invert the direction of movement.
		double horizontalMouseMovement = (mouseX - prevMouseX) * yawSensitivity;
		double verticalMouseMovement = (mouseY - prevMouseY) * pitchSensitivity;

		// Keep the last mouse cursor location so we can tell the relative movement of the mouse
		// cursor the next time this method is called.
		prevMouseX = mouseX;
		prevMouseY = mouseY;

		// Apply the mouse movement to our rotation Vec3. The vertical (look up and down) movement is applied on
		// the X axis, and the horizontal (look left and right) movement is applied on the Y Axis
		rotationAngleRadians.x += (float) verticalMouseMovement;
		rotationAngleRadians.y += (float) horizontalMouseMovement;

		// Limit looking up and down to vertically up and down
		if (rotationAngleRadians.x < -Math.PI_OVER_2_f) {
			rotationAngleRadians.x = -Math.PI_OVER_2_f;
		}
		if (rotationAngleRadians.x > Math.PI_OVER_2_f) {
			rotationAngleRadians.x = Math.PI_OVER_2_f;
		}

		// Looking left and right - keep angles in the range 0.0 to pi
		// 0 degrees is looking directly down the negative Z axis "North", 90 degrees is "East", 180 degrees is "South", 270 degrees is "West"
		if (rotationAngleRadians.y < 0f) {
			rotationAngleRadians.y += Math.PI_TIMES_2_f;
		}
		if (rotationAngleRadians.y > Math.PI_TIMES_2_f) {
			rotationAngleRadians.y -= Math.PI_TIMES_2_f;
		}
	}

	/**
	 * Calculate which direction we need to move the camera and by what amount.
	 * <p>
	 * If you are not calculating frame duration then you may want to pass it 1.0f divided by the refresh rate of the monitor in use.
	 *
	 * @param deltaTime The frame duration in milliseconds.
	 */
	public void move(float deltaTime) {
		movement.zero();

		// Get the Sine and Cosine of our x and y axes
		float sinXRot = Math.sin(rotationAngleRadians.x);
		float cosXRot = Math.cos(rotationAngleRadians.x);

		float sinYRot = Math.sin(rotationAngleRadians.y);
		float cosYRot = Math.cos(rotationAngleRadians.y);

		final float pitchLimitFactor = cosXRot; // This cancels out moving on the Z axis when we're looking up or down

		// Move appropriately depending on which key(s) are currently being held
		if (mHoldingForward) {
			movement.add(-sinYRot * pitchLimitFactor, sinXRot, cosYRot * pitchLimitFactor);
		}
		if (mHoldingBackward) {
			movement.add(sinYRot * pitchLimitFactor, -sinXRot, -cosYRot * pitchLimitFactor);
		}
		if (mHoldingLeftStrafe) {
			movement.add(cosYRot, 0f, sinYRot);
		}
		if (mHoldingRightStrafe) {
			movement.sub(cosYRot, 0f, sinYRot);
		}

		// If we have any movement at all, then normalise our movement vector
		if (movement.length() > 0f) {
			movement.normalize();
		}

		// Apply our framerate-independent factor to our movement vector so that we move at the same speed
		// regardless of our framerate (assuming a correct frame duration is provided to this method).
		movement.mul(movementSpeedFactor * deltaTime);

		location.add(movement);
	}

}
