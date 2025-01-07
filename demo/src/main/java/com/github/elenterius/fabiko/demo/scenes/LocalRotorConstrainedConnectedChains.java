package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.BoneConnectionPoint;
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
public class LocalRotorConstrainedConnectedChains extends DemoScene {

	public LocalRotorConstrainedConnectedChains() {
		title = "Local Rotor Constrained Connected Chains";
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


		FabrikChain3f secondChain = FabrikChain3f.builder()
				.addRotorConstrainedBaseBone(new Vector3f(), new Vector3f(1, 0, 0), 15f, X_AXIS, Math.toRadians(45f), true)
				.addFreelyRotatingRotorBone(X_AXIS, 15f, true)
				.addFreelyRotatingRotorBone(X_AXIS, 15f, true)
				.addFreelyRotatingRotorBone(X_AXIS, 15f, true)
				.build();

		// Connect this second chain to the start point of bone 3 in chain 0 of the structure
		structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(secondChain, Color4fc.RED);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
