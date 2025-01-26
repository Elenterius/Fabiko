package com.github.elenterius.fabiko.demo;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/// An example application to demonstrate the Caliko library in both 2D and 3D modes.
///
/// Use up/down cursors to change between 2D/3D mode and left/right cursors to change demos.
/// In 2D mode clicking using the left mouse button (LMB) changes the target location, and you can click and drag.
/// In 3D mode, use W/S/A/D to move the camera and the mouse with LMB held down to look.
///
/// @see [../docs/Demo-Application.md]
public class Application {

	static int unsafeSceneIndex = 0;

	static boolean fixedBaseMode = true;
	static boolean rotateBasesMode = false;

	static boolean drawLines = true;
	static boolean drawAxes = false;
	static boolean drawModels = true;
	static boolean drawConstraints = true;

	static boolean leftMouseButtonDown = false;
	static boolean paused = true;

	static int windowWidth = 800;
	static int windowHeight = 600;

	static OpenGLWindow window = new OpenGLWindow(Application.windowWidth, Application.windowHeight);

	@Nullable
	static SceneHandler sceneHandler = null;

	public static void main(final String[] args) {
		try {
			sceneHandler = new SceneHandler(0);
			run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			window.cleanup();
		}
	}

	private static void run() {
		while (!GLFW.glfwWindowShouldClose(window.windowId)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			sceneHandler.draw();
			window.swapBuffers();
			GLFW.glfwPollEvents();
		}
	}

}
