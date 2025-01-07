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
public class FreelyRotatingGlobalHinges extends DemoScene {

	public FreelyRotatingGlobalHinges() {
		title = "Freely Rotating Global Hinges";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		// We'll create a circular arrangement of 3 chains which are each constrained about different global axes.
		// Note: Although I've used the cardinal X/Y/Z axes here, any axis can be used.

		Vector3fc globalHingeAxis = new Vector3f();
		Color4fc color = Color4fc.WHITE;

		int numChains = 3;
		float rotStep = Math.PI_TIMES_2_f / numChains;

		for (int i = 0; i < numChains; i++) {

			switch (i % numChains) {
				case 0 -> {
					color = Color4fc.RED;
					globalHingeAxis = X_AXIS;
				}
				case 1 -> {
					color = Color4fc.GREEN;
					globalHingeAxis = Y_AXIS;
				}
				case 2 -> {
					color = Color4fc.BLUE;
					globalHingeAxis = Z_AXIS;
				}
			}

			Vector3f startLoc = new Vector3f(0f, 0f, -40f);
			startLoc.rotateY(rotStep * i);

			FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
					.addFreelyRotatingRotorBaseBone(startLoc, DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);

			for (int boneLoop = 0; boneLoop < 7; boneLoop++) {
				if (boneLoop % 2 == 0) {
					chainBuilder.addFreelyRotatingHingeBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, globalHingeAxis, false);
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
