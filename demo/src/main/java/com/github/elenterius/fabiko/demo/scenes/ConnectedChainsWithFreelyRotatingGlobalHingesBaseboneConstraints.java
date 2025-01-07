package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.BoneConnectionPoint;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/// @author jsalvo
/// @author Elenterius
public class ConnectedChainsWithFreelyRotatingGlobalHingesBaseboneConstraints extends DemoScene {

	public ConnectedChainsWithFreelyRotatingGlobalHingesBaseboneConstraints() {
		title = "Connected Chains with Freely-Rotating Global Hinged Basebone Constraints";
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

		// Create a second chain which will have a relative hinge basebone constraint about the Y axis.
		FabrikChain3f.ConsecutiveBoneBuilder chainBuilderB = FabrikChain3f.builder()
				.addFreelyRotatingHingeBaseBone(new Vector3f(15f, 0f, 0f), DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, Y_AXIS, true);

		// Add some additional bones
		chainBuilderB.addFreelyRotatingRotorBone(X_AXIS, 15f, true);
		chainBuilderB.addFreelyRotatingRotorBone(X_AXIS, 15f, true);
		chainBuilderB.addFreelyRotatingRotorBone(X_AXIS, 15f, true);

		FabrikChain3f secondChain = chainBuilderB.build();

		// Connect this second chain to the start point of bone 3 in chain 0 of the structure
		structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(secondChain, Color4fc.GREY);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		// Do nothing
	}

}
