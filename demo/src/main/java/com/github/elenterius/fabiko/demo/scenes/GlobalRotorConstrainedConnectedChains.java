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
public class GlobalRotorConstrainedConnectedChains extends DemoScene {

	public GlobalRotorConstrainedConnectedChains() {
		title = "Global Rotor Constrained Connected Chains";
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


		FabrikChain3f.ConsecutiveBoneBuilder secondChainBuilder = FabrikChain3f.builder()
				.addRotorConstrainedBaseBone(new Vector3f(), new Vector3f(1f, 0f, 0f), 15f, X_AXIS, Math.toRadians(45f), false);

		secondChainBuilder.addFreelyRotatingRotorBone(X_AXIS, 15f, true);
		secondChainBuilder.addFreelyRotatingRotorBone(X_AXIS, 15f, true);
		secondChainBuilder.addFreelyRotatingRotorBone(X_AXIS, 15f, true);

		FabrikChain3f secondChain = secondChainBuilder.build();
		structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(secondChain, Color4fc.RED);


		FabrikChain3f.ConsecutiveBoneBuilder thirdChainBuilder = FabrikChain3f.builder()
				.addRotorConstrainedBaseBone(new Vector3f(), new Vector3f(0f, 1f, 0f), 15f, Y_AXIS, Math.toRadians(45f), false);

		thirdChainBuilder.addFreelyRotatingRotorBone(Y_AXIS, 15f, true);
		thirdChainBuilder.addFreelyRotatingRotorBone(Y_AXIS, 15f, true);
		thirdChainBuilder.addFreelyRotatingRotorBone(Y_AXIS, 15f, true);

		FabrikChain3f thirdChain = thirdChainBuilder.build();
		structure.connectChain(thirdChain, 0, 6, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(thirdChain, Color4fc.BLUE);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		//do nothing
	}

}
