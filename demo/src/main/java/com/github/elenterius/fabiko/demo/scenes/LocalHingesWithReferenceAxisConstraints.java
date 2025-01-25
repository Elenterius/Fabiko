package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/// @author jsalvo
/// @author Elenterius
public class LocalHingesWithReferenceAxisConstraints extends DemoScene {

	public LocalHingesWithReferenceAxisConstraints() {
		title = "Local Hinges with Reference Axis Constraints";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		// We'll create a circular arrangement of 3 chains with alternate bones each constrained about different local axes.
		// Note: Local hinge rotation axes are relative to the rotation matrix of the previous bone in the chain.

		Vector3fc hingeRotationAxis = new Vector3f();
		Vector3fc hingeReferenceAxis = new Vector3f();
		Color4fc color = Color4fc.WHITE;

		int numChains = 3;
		float rotStep = Math.PI_TIMES_2_f / numChains;

		for (int i = 0; i < numChains; i++) {

			switch (i % 3) {
				case 0 -> {
					hingeRotationAxis = X_AXIS;
					hingeReferenceAxis = Y_AXIS;
					color = Color4fc.RED;
				}
				case 1 -> {
					hingeRotationAxis = Y_AXIS;
					hingeReferenceAxis = X_AXIS;
					color = Color4fc.GREEN;
				}
				case 2 -> {
					hingeRotationAxis = X_AXIS;
					hingeReferenceAxis = Y_AXIS;
					color = Color4fc.BLUE;
				}
			}

			Vector3f startLoc = new Vector3f(0f, 0f, -40f);
			startLoc.rotateY(rotStep * i);

			FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
					.addFreelyRotatingRotorBaseBone(startLoc, DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);

			float constraintAngle = Math.toRadians(90f);
			for (int j = 0; j < 6; j++) {
				if (j % 2 == 0) {
					chainBuilder.addHingeConstrainedBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, hingeRotationAxis, constraintAngle, constraintAngle, hingeReferenceAxis, true);
				}
				else {
					chainBuilder.addFreelyRotatingRotorBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);
				}
			}

			FabrikChain3f chain = chainBuilder.build();
			structure.addChain(chain);
			structureDrawInfo
					.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(Color4fc.GREY, color))
					.getBaseBone().setColorFrom(color);
		}
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
