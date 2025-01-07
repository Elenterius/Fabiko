package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/// @author jsalvo
/// @author Elenterius
public class GlobalHingesWithReferenceAxisConstraints extends DemoScene {

	public GlobalHingesWithReferenceAxisConstraints() {
		title = "Global Hinges With Reference Axis Constraints";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
				.addFreelyRotatingRotorBaseBone(new Vector3f(0f, 30f, -40f), new Vector3f(0f, -1f, 0f), DEFAULT_BONE_LENGTH, true);

		float clockwiseAngle = Math.toRadians(120f);
		float antiClockwiseAngle = Math.toRadians(120f);

		for (int i = 0; i < 8; i++) {
			if (i % 2 == 0) {
				chainBuilder.addHingeConstrainedBone(NEG_Y_AXIS, DEFAULT_BONE_LENGTH, Z_AXIS, clockwiseAngle, antiClockwiseAngle, NEG_Y_AXIS, false);
			}
			else {
				chainBuilder.addFreelyRotatingRotorBone(NEG_Y_AXIS, DEFAULT_BONE_LENGTH, true);
			}
		}

		FabrikChain3f chain = chainBuilder.build();
		structure.addChain(chain);
		structureDrawInfo
				.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(Color4fc.GREY, Color4fc.MID_GREEN))
				.getBaseBone().setColorFrom(Color4fc.YELLOW);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
