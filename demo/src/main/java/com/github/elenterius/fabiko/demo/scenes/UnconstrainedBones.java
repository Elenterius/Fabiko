package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/// @author jsalvo
/// @author Elenterius
public class UnconstrainedBones extends DemoScene {

	public UnconstrainedBones() {
		title = "Unconstrained bones";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
				.addFreelyRotatingRotorBaseBone(new Vector3f(0f, 0f, 40f), DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);

		for (int i = 0; i < 7; i++) {
			chainBuilder.addFreelyRotatingRotorBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);
		}

		FabrikChain3f chain = chainBuilder.build();
		structure.addChain(chain);
		structureDrawInfo
				.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(Color4fc.GREEN, 0.4f))
				.getBaseBone().setColorFrom(Color4fc.GREEN);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
