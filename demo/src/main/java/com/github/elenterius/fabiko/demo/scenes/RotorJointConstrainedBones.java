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

/// @author jsalvo
/// @author Elenterius
public class RotorJointConstrainedBones extends DemoScene {

	public RotorJointConstrainedBones() {
		title = "Rotor Joint Constrained Bones";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		final int numChains = 3;
		float rotStep = Math.PI_TIMES_2_f / numChains;
		float constraintAngle = Math.toRadians(45f);

		for (int i = 0; i < numChains; i++) {

			Color4fc color = switch (i % numChains) {
				case 0 -> Color4fc.MID_RED;
				case 1 -> Color4fc.MID_GREEN;
				case 2 -> Color4fc.MID_BLUE;
				default -> new Color4f();
			};

			Vector3f startLoc = new Vector3f(0f, 0f, -40f);
			startLoc.rotateY(rotStep * i);

			Vector3f temp = new Vector3f(startLoc).sub(0, 0, DEFAULT_BONE_LENGTH);
			float boneLength = temp.distance(startLoc);
			Vector3f direction = temp.sub(startLoc).normalize();

			FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
					.addFreelyRotatingRotorBaseBone(startLoc, direction, boneLength, true);

			for (int j = 0; j < 7; j++) {
				chainBuilder.addRotorConstrainedBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, constraintAngle, true);
			}

			FabrikChain3f chain = chainBuilder.build();
			structure.addChain(chain);
			structureDrawInfo
					.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(color, 0.4f))
					.getBaseBone().setColorFrom(color);
		}
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
