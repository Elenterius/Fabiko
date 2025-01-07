package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4f;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/// @author jsalvo
/// @author Elenterius
public class RotorConstrainedBaseBones extends DemoScene {

	public RotorConstrainedBaseBones() {
		title = "Rotor Constrained Base Bones";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		int numChains = 3;
		float rotStep = Math.PI_TIMES_2_f / numChains;
		float baseBoneConstraintAngle = Math.toRadians(20f);

		for (int i = 0; i < numChains; i++) {

			Color4fc boneColour = new Color4f();
			Color4fc baseBoneColour = new Color4f();
			Vector3fc baseBoneConstraintAxis = new Vector3f();

			switch (i % numChains) {
				case 0 -> {
					boneColour = Color4fc.MID_RED;
					baseBoneColour = Color4fc.RED;
					baseBoneConstraintAxis = X_AXIS;
				}
				case 1 -> {
					boneColour = Color4fc.MID_GREEN;
					baseBoneColour = Color4fc.MID_GREEN;
					baseBoneConstraintAxis = Y_AXIS;
				}
				case 2 -> {
					boneColour = Color4fc.MID_BLUE;
					baseBoneColour = Color4fc.BLUE;
					baseBoneConstraintAxis = NEG_Z_AXIS;
				}
			}

			Vector3f startLoc = new Vector3f(0f, 0f, -40f);
			startLoc.rotateY(rotStep * i);

			FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
					.addRotorConstrainedBaseBone(startLoc, baseBoneConstraintAxis, DEFAULT_BONE_LENGTH * 2f, baseBoneConstraintAngle, false);

			for (int j = 0; j < 7; j++) {
				chainBuilder.addFreelyRotatingRotorBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);
			}

			FabrikChain3f chain = chainBuilder.build();
			structure.addChain(chain);
			structureDrawInfo
					.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(boneColour, 0.4f))
					.getBaseBone().setColorFrom(baseBoneColour);
		}
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
