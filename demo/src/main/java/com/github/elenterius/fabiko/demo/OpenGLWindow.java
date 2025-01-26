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
import org.joml.Vector2f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/// @author Al Lansley
/// @author Elenterius
public class OpenGLWindow {

	public static Vector2f screenSpaceMousePos = new Vector2f(Application.windowWidth / 2f, Application.windowHeight / 2f);

	final Matrix4f projectionMatrix = new Matrix4f();
	final Matrix4f tmp = new Matrix4f();

	long windowId;
	int windowWidth;
	int windowHeight;
	float aspectRatio;

	boolean useOrthographicProjection;
	float verticalFoVRadians;
	float zNear;
	float zFar;
	float orthoExtent;

	// We need to strongly reference callback instances so that they don't get garbage collected.
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;
	private GLFWWindowSizeCallback windowSizeCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	private GLFWCursorPosCallback cursorPosCallback;

	public OpenGLWindow(int width, int height) {
		this(width, height, 35f, 1f, 5_000f, 120f); //sensible projection matrix values
	}

	public OpenGLWindow(int windowWidth, int windowHeight, float verticalFoVDegrees, float zNear, float zFar, float orthoExtent) {
		this.windowWidth = windowWidth <= 0 ? 1 : windowWidth;
		this.windowHeight = windowHeight <= 0 ? 1 : windowHeight;
		aspectRatio = (float) this.windowWidth / (float) this.windowHeight;

		verticalFoVRadians = Math.toRadians(verticalFoVDegrees);
		this.zNear = zNear;
		this.zFar = zFar;
		this.orthoExtent = orthoExtent;

		setPerspectiveProjection(true);

		errorCallback = GLFWErrorCallback.createPrint(System.err); //output errors to System.err
		glfwSetErrorCallback(errorCallback);

		if (glfwPlatformSupported(GLFW_PLATFORM_WAYLAND)) {
			glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11); //TODO: use X-Wayland instead until this issue is resolved: https://github.com/glfw/glfw/issues/2621
		}

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// ----- Specify window hints -----
		// Note: Window hints must be specified after glfwInit() (which resets them) and before glfwCreateWindow where the context is created.
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);          // Request OpenGL version 3.3 (the minimum we can get away with)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);  // We want a core profile without any deprecated functionality...
		//glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);          // ...however we do NOT want a forward compatible profile as they've removed line widths!
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);                      // We want the window to be resizable
		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);                        // We want the window to be visible (false makes it hidden after creation)
		glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);                        // We want the window to take focus on creation
		glfwWindowHint(GLFW_SAMPLES, 4);                        // Ask for 4x anti-aliasing (this doesn't mean we'll get it, though)

		windowId = glfwCreateWindow(this.windowWidth, this.windowHeight, "Initializing...", NULL, NULL);
		if (windowId == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		// Get the resolution of the primary monitor
		GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		int windowHorizOffset = (videoMode.width() - this.windowWidth) / 2;
		int windowVertOffset = (videoMode.height() - this.windowHeight) / 2;

		glfwSetWindowPos(windowId, windowHorizOffset, windowVertOffset); // Center our window
		glfwMakeContextCurrent(windowId);                                // Make the OpenGL context current
		glfwSwapInterval(1);                                             // Swap buffers every frame (i.e. enable vSync)

		// This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread, creates the ContextCapabilities instance and makes
		// the OpenGL bindings available for use.
		glfwMakeContextCurrent(windowId);

		// Enumerate the capabilities of the current OpenGL context, loading forward compatible capabilities
		GL.createCapabilities(true);

		setupUserInputCallbacks();
		setupResizeWindowCallbacks();

		// ---------- OpenGL settings -----------
		glClearColor(0f, 0f, 0f, 0f);
		glViewport(0, 0, this.windowWidth, this.windowHeight);

		// Enable depth testing
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_DEPTH_TEST);

		// When we clear the depth buffer, we'll clear the entire buffer
		glClearDepth(1f);

		// Enable blending to use alpha channels
		// Note: blending must be enabled to use transparency / alpha values in our fragment shaders.
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);

		glfwShowWindow(windowId); // make visible
	}

	private static void handleCameraControls(int key, int action) {
		Application.sceneHandler.handleCameraMovement(key, action);
	}

	/// Return a calculated ModelViewProjection matrix.
	///
	/// This MVP matrix is the result of multiplying the projection matrix by the view matrix obtained from the camera, and
	/// as such is really a ProjectionView matrix or 'identity MVP', however you'd like to term it.
	///
	/// If you want an MVP matrix specific to your model, simply multiply this matrix by your desired model matrix to create
	/// an MVP matrix specific to your model.
	///
	/// @return A calculate ModelViewProjection matrix.
	public Matrix4fc getIdentityModelViewProjectionMatrix() {
		return projectionMatrix.mul(SceneHandler.camera.getViewMatrix(), tmp);
	}

	/// @return The projection matrix.
	public Matrix4fc getProjectionMatrix() {
		return projectionMatrix;
	}

	/// Swap the front and back buffers to update the display.
	public void swapBuffers() {
		glfwSwapBuffers(windowId);
	}

	/// Set the window title to the specified String argument.
	///
	/// @param title The String that will be used as the title of the window.
	public void setWindowTitle(String title) {
		glfwSetWindowTitle(windowId, title);
	}

	/// Destroy the window, finish up glfw and release all callback methods.
	public void cleanup() {
		// Free the window callbacks and destroy the window
		//glfwFreeCallbacks(mWindowId);
		cursorPosCallback.close();
		mouseButtonCallback.close();
		windowSizeCallback.close();
		keyCallback.close();

		glfwDestroyWindow(windowId);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
		errorCallback = null;
	}

	private void setupUserInputCallbacks() {
		glfwSetKeyCallback(windowId, keyCallback = GLFWKeyCallback.create((long window, int key, int scancode, int action, int mods) -> {
			if (action == GLFW_PRESS) {
				switch (key) {
					case GLFW_KEY_RIGHT -> showNextDemo();
					case GLFW_KEY_LEFT -> showPreviousDemo();

					case GLFW_KEY_UP, GLFW_KEY_DOWN -> doNothing();

					case GLFW_KEY_F -> toggleFixedBaseMode();
					case GLFW_KEY_R -> toggleRotatingBases();

					case GLFW_KEY_C -> toggleDrawConstraints();
					case GLFW_KEY_L -> toggleDrawLines();
					case GLFW_KEY_M -> toggleDrawModels();
					case GLFW_KEY_X -> toggleDrawAxes();

					case GLFW_KEY_P -> switchBetweenOrthographicAndPerspectiveView();

					case GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_SPACE, GLFW_KEY_LEFT_SHIFT -> handleCameraControls(key, action);

					case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);

					case GLFW_KEY_ENTER -> togglePause();
				}
			}
			else if (action == GLFW_REPEAT || action == GLFW_RELEASE) {
				switch (key) {
					case GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_SPACE, GLFW_KEY_LEFT_SHIFT -> handleCameraControls(key, action);
				}
			}
		}));

		glfwSetCursorPosCallback(windowId, cursorPosCallback = GLFWCursorPosCallback.create((long windowId, double mouseX, double mouseY) -> {
			screenSpaceMousePos.set(mouseX, mouseY);

			if (Application.leftMouseButtonDown) {
				SceneHandler.camera.handleMouseMove(mouseX, mouseY);
			}
		}));

		glfwSetMouseButtonCallback(windowId, mouseButtonCallback = GLFWMouseButtonCallback.create((long windowId, int button, int action, int mods) -> {
			if (button == GLFW_MOUSE_BUTTON_1) {
				// left mouse button, set the LMB status flag
				// Note: We cannot simply toggle the flag here as double-clicking the title bar to fullscreen the window confuses it,
				// and we then end up mouse look-ing without the LMB being held down!
				Application.leftMouseButtonDown = action == GLFW_PRESS;

				// Immediately set the cursor position to the center of the screen so our view doesn't "jump" on first cursor position change
				glfwSetCursorPos(windowId, windowWidth / 2d, windowHeight / 2d);

				switch (action) {
					case GLFW_PRESS ->
							glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // Make the mouse cursor hidden and put it into a 'virtual' mode where its values are not limited
					case GLFW_RELEASE -> {
						// Restore the mouse cursor to normal and reset the camera last cursor position to be the middle of the window
						glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
						SceneHandler.camera.resetLastCursorPosition();
					}
				}
			}
		}));
	}

	private void setupResizeWindowCallbacks() {
		glfwSetWindowSizeCallback(windowId, windowSizeCallback = GLFWWindowSizeCallback.create((long windowId, int newWidth, int newHeight) -> {
			if (newWidth <= 0) {
				newWidth = 1;
			}
			if (newHeight <= 0) {
				newHeight = 1;
			}
			windowWidth = newWidth;
			windowHeight = newHeight;
			aspectRatio = (float) windowWidth / (float) windowHeight;

			// Let our camera know about the new size so it can correctly recenter the mouse cursor
			SceneHandler.camera.updateWindowSize(newWidth, newHeight);

			glViewport(0, 0, windowWidth, windowHeight);

			setPerspectiveProjection(!useOrthographicProjection);
		}));
	}

	private void togglePause() {
		Application.paused = !Application.paused;
	}

	private void toggleDrawAxes() {
		Application.drawAxes = !Application.drawAxes;
	}

	private void switchBetweenOrthographicAndPerspectiveView() {
		useOrthographicProjection = !useOrthographicProjection;
		setOrthographicProjection(useOrthographicProjection);
	}

	private void toggleDrawModels() {
		Application.drawModels = !Application.drawModels;
	}

	private void toggleDrawLines() {
		Application.drawLines = !Application.drawLines;
	}

	private void toggleDrawConstraints() {
		Application.drawConstraints = !Application.drawConstraints;
	}

	private void showNextDemo() {
		Application.unsafeSceneIndex++;
		Application.sceneHandler.setup(Application.unsafeSceneIndex);
	}

	private void showPreviousDemo() {
		Application.unsafeSceneIndex--;
		Application.sceneHandler.setup(Application.unsafeSceneIndex);
	}

	private void setOrthographicProjection(boolean flag) {
		setPerspectiveProjection(!flag);
	}

	private void setPerspectiveProjection(boolean flag) {
		if (flag) {
			useOrthographicProjection = false;
			projectionMatrix.setPerspective(verticalFoVRadians, aspectRatio, zNear, zFar);

			if (Application.sceneHandler != null) {
				Application.sceneHandler.printCameraInfo();
			}
		}
		else {
			useOrthographicProjection = true;
			projectionMatrix.setOrtho(-orthoExtent, orthoExtent, -orthoExtent, orthoExtent, zNear, zFar);
		}
	}

	private void toggleRotatingBases() {
		Application.rotateBasesMode = !Application.rotateBasesMode;
	}

	private void toggleFixedBaseMode() {
		Application.fixedBaseMode = !Application.fixedBaseMode;
		Application.sceneHandler.setFixedBaseMode(Application.fixedBaseMode);
	}

	private void doNothing() {
		//do nothing
	}

}