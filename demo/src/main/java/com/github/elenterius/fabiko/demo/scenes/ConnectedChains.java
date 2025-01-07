package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.BoneConnectionPoint;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * @author jsalvo
 * @author Elenterius
 */
public class ConnectedChains extends DemoScene {

	public ConnectedChains() {
		title = "Connected Chains";
	}

	@Override
	public void setup() {
		structure = new FabrikStructure3f();
		structureDrawInfo = new StructureDrawInfoImpl();

		FabrikChain3f.ConsecutiveBoneBuilder chainBuilder = FabrikChain3f.builder()
				.addFreelyRotatingRotorBaseBone(new Vector3f(0f, 0f, 40f), DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);

		for (int i = 0; i < 5; i++) {
			chainBuilder.addFreelyRotatingRotorBone(DEFAULT_BONE_DIRECTION, DEFAULT_BONE_LENGTH, true);
		}

		FabrikChain3f chain = chainBuilder.build();
		structure.addChain(chain);
		structureDrawInfo
				.addChainDrawInfo(chain, new ChainDrawInfoImpl.AlternatingColorFunction(Color4fc.GREEN, 0.4f))
				.getBaseBone().setColorFrom(Color4fc.GREEN);

		Vector3f direction = new Vector3f(1f).normalize();

		FabrikChain3f secondChain = FabrikChain3f.builder()
				.addFreelyRotatingRotorBaseBone(new Vector3f(100f), direction, DEFAULT_BONE_LENGTH, true)
				.addFreelyRotatingRotorBone(X_AXIS, 20f, true)
				.addFreelyRotatingRotorBone(Y_AXIS, 20f, true)
				.addFreelyRotatingRotorBone(Z_AXIS, 20f, true)
				.build();

		structure.connectChain(secondChain, 0, 0, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(secondChain, Color4fc.RED);

		FabrikChain3f thirdChain = new FabrikChain3f(secondChain);
		structure.connectChain(thirdChain, 0, 2, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(thirdChain, Color4fc.WHITE);

		FabrikChain3f fourthChain = new FabrikChain3f(secondChain);
		structure.connectChain(fourthChain, 0, 4, BoneConnectionPoint.END);
		structureDrawInfo.addChainDrawInfo(fourthChain, Color4fc.BLUE);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		// Do nothing
	}

}
