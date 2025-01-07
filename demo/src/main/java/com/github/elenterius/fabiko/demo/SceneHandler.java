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

import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikSolver3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.scenes.DemoScene;
import com.github.elenterius.fabiko.visualization.*;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.Random;

/// @author Al Lansley
/// @author Elenterius
public class SceneHandler {

	static Random random = new Random();

	static float boneLineWidth = 5f;
	static float constraintLineWidth = 2f;
	static float baseRotationAmount = Math.toRadians(0.3f);

	static Camera camera = new Camera(new Vector3f(0f, 0f, -150f), new Vector3f(0f, 0f, 0f), Application.windowWidth, Application.windowHeight);

	static float extent = 1000f;
	static float gridLevel = 100f;
	static int subdivisions = 20;
	static GridDrawHelper lowerGrid = new GridDrawHelper(extent, extent, -gridLevel, subdivisions);
	static GridDrawHelper upperGrid = new GridDrawHelper(extent, extent, gridLevel, subdivisions);

	static AxisDrawHelper axisDrawHelper = new AxisDrawHelper(3f, 1f);
	static ConstraintDrawHelper constraintDrawHelper = new ConstraintDrawHelper();

	static ModelDrawHelper model = new ModelDrawHelper("/assets/pyramid.obj", 1f);

	static MovingTarget movingTarget = new MovingTarget(new Vector3f(), new Vector3f(60), 200, gridLevel, random);

	private FabrikSolver3f solver = new FabrikSolver3f();
	private FabrikStructure3f structure;
	private StructureDrawInfo structureDrawInfo;

	private DemoScene demoScene;

	public SceneHandler(int sceneIndex) {
		setup(sceneIndex);
	}

	/// Set up a demo consisting of an arrangement of 3D IK chains with a given configuration.
	public void setup(int unsafeSceneIndex) {
		int n = SceneFactory.getNumberOfScenes();
		int safeIndex = unsafeSceneIndex >= 0 ? unsafeSceneIndex % n : (unsafeSceneIndex % n) + (n - 1);

		demoScene = SceneFactory.crate(safeIndex);
		demoScene.setup();
		structure = demoScene.getStructure();
		structureDrawInfo = demoScene.getStructureDrawInfo();

		Application.window.setWindowTitle("Demo %d - %s".formatted(safeIndex + 1, demoScene.getTitle()));

		//structure.updateTarget(target.getCurrentLocation());
	}

	/**
	 * Set all chains in the structure to be in fixed-base mode whereby the base locations cannot move.
	 */
	public void setFixedBaseMode(boolean value) {
		structure.setFixedBaseMode(value);
	}

	public void handleCameraMovement(int key, int action) {
		camera.handleKeyPress(key, action);
	}

	public void draw() {
		// Move the camera based on key presses and mouse movement
		camera.move(1f / 60f);

		Matrix4fc modelViewProjection = Application.window.getIdentityModelViewProjectionMatrix();

		lowerGrid.draw(modelViewProjection);
		upperGrid.draw(modelViewProjection);

		// If we're not paused then step the target and solve the structure for the new target location
		if (!Application.paused) {
			movingTarget.step();
			demoScene.draw(modelViewProjection);

			// Solve the structure (chains with embedded targets will use those, otherwise the provided target is used)
			solver.solveForTarget(structure, movingTarget.getCurrentLocation());
		}

		// If we're in rotate base mode then rotate the base location(s) of all chains in the structure
		if (Application.rotateBasesMode) {
			for (FabrikChain3f chain : structure.getChains()) {
				Vector3f base = chain.getBaseLocation().rotateAxis(baseRotationAmount, DemoScene.Y_AXIS.x(), DemoScene.Y_AXIS.y(), DemoScene.Y_AXIS.z(), new Vector3f());
				chain.setBaseLocation(base);
			}
		}

		movingTarget.draw(Color4fc.YELLOW, 8f, modelViewProjection);

		if (Application.drawLines) {
			BoneDrawHelper.draw(structureDrawInfo, boneLineWidth, modelViewProjection);
		}
		if (Application.drawModels) {
			model.drawStructure(structure, camera.getViewMatrix(), Application.window.projectionMatrix);
		}
		if (Application.drawAxes) {
			axisDrawHelper.draw(structure, camera.getViewMatrix(), Application.window.projectionMatrix);
		}
		if (Application.drawConstraints) {
			constraintDrawHelper.draw(structure, constraintLineWidth, modelViewProjection);
		}

	}

}
