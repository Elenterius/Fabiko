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

/// Refactored copy from Caliko Library.
///
/// Original Author: Al Lansley
public class Camera {

	private static final Vector3f movement = new Vector3f();

	private final Matrix4f viewMatrix = new Matrix4f();

	private float moveSpeed = 80f;

	/// How sensitive mouse movements affect looking up and down.
	private double pitchSensitivity = 0.01d;

	/// How sensitive mouse movements affect looking left and right.
	private double yawSensitivity = 0.01d;

	// Current location and orientation of the camera
	private final Vector3f location = new Vector3f();
	private final Vector3f rotation = new Vector3f();

	private boolean moveForward = false;
	private boolean moveBackward = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	private boolean moveUp = false;
	private boolean moveDown = false;

	private double windowCenterX;
	private double windowCenterY;

	private double prevMouseX, prevMouseY;

	public Camera(Vector3f location, Vector3f rotationDegrees, int windowWidth, int windowHeight) {
		this.location.set(location);
		rotation.set(
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
		viewMatrix.rotateX(rotation.x);
		viewMatrix.rotateY(rotation.y);

		// Only rotate around Z if we have any Z-axis rotation - in FPS camera controls we do not.
		if (rotation.z > 0f) {
			viewMatrix.rotateZ(rotation.z);
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

	public void setMoveForward(boolean flag) {
		moveForward = flag;
	}

	public void setMoveBackward(boolean flag) {
		moveBackward = flag;
	}

	public void setMoveLeft(boolean flag) {
		moveLeft = flag;
	}

	public void setMoveRight(boolean flag) {
		moveRight = flag;
	}

	public void setMoveUp(boolean flag) {
		moveUp = flag;
	}

	public void setMoveDown(boolean flag) {
		moveDown = flag;
	}

	/// Method to deal with mouse position changes.
	///
	/// The pitch (up and down) and yaw (left and right) sensitivity of the camera can be modified via the setSensitivity method.
	///
	/// @param mouseX The x location of the mouse cursor.
	/// @param mouseY The y location of the mouse cursor.
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
		rotation.x += (float) verticalMouseMovement;
		rotation.y += (float) horizontalMouseMovement;

		// Limit looking up and down to vertically up and down
		if (rotation.x < -Math.PI_OVER_2_f) {
			rotation.x = -Math.PI_OVER_2_f;
		}
		if (rotation.x > Math.PI_OVER_2_f) {
			rotation.x = Math.PI_OVER_2_f;
		}

		// Looking left and right - keep angles in the range 0.0 to pi
		// 0 degrees is looking directly down the negative Z axis "North", 90 degrees is "East", 180 degrees is "South", 270 degrees is "West"
		if (rotation.y < 0f) {
			rotation.y += Math.PI_TIMES_2_f;
		}
		if (rotation.y > Math.PI_TIMES_2_f) {
			rotation.y -= Math.PI_TIMES_2_f;
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
		float sinXRot = Math.sin(rotation.x);
		float cosXRot = Math.cos(rotation.x);
		float sinYRot = Math.sin(rotation.y);
		float cosYRot = Math.cos(rotation.y);

		final float pitchLimitFactor = cosXRot; // This cancels out moving on the Z axis when we're looking up or down

		if (moveForward && !moveBackward) {
			movement.add(-sinYRot * pitchLimitFactor, sinXRot, cosYRot * pitchLimitFactor);
		}
		if (moveBackward && !moveForward) {
			movement.add(sinYRot * pitchLimitFactor, -sinXRot, -cosYRot * pitchLimitFactor);
		}

		if (moveLeft && !moveRight) {
			movement.add(cosYRot, 0f, sinYRot);
		}
		if (moveRight && !moveLeft) {
			movement.sub(cosYRot, 0f, sinYRot);
		}

		if (moveUp && !moveDown) {
			movement.sub(0f, 1f, 0f);
		}
		if (moveDown && !moveUp) {
			movement.add(0, 1f, 0);
		}

		if (movement.length() > 0f) {
			movement.normalize();
		}

		movement.mul(moveSpeed * deltaTime);

		location.add(movement);
	}

	@Override
	public String toString() {
		return "Camera{" +
				"position=" + location +
				", rotation=" + rotation.mul(180f / Math.PI_f, new Vector3f()) +
				'}';
	}
}
