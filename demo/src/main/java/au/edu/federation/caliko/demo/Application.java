package au.edu.federation.caliko.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * An example application to demonstrate the Caliko library in both 2D and 3D modes.
 * <p>
 * Use up/down cursors to change between 2D/3D mode and left/right cursors to change demos.
 * In 2D mode clicking using the left mouse button (LMB) changes the target location, and you can click and drag.
 * In 3D mode, use W/S/A/D to move the camera and the mouse with LMB held down to look.
 * <p>
 * See the README.txt for further documentation and controls.
 *
 * @version 1.0 - 31/01/2016
 * @author Al Lansley
 */
public class Application {

	static boolean use3dDemo = false;
	static int demoNumber = 1;
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

	static CalikoDemo demo;

	public static void main(final String[] args) {
		try {
			Application.demo = Application.use3dDemo ? new CalikoDemo3D(Application.demoNumber) : new CalikoDemo2D(Application.demoNumber);
			Application.mainLoop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			Application.window.cleanup();
		}
	}

	private static void mainLoop() {
		// Run the rendering loop until the user closes the window or presses Escape
		while (!GLFW.glfwWindowShouldClose(Application.window.mWindowId)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			Application.demo.draw();
			Application.window.swapBuffers();
			GLFW.glfwPollEvents();
		}
	}

}
