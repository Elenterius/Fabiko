package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.BoneConnectionPoint;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.ChainDrawInfoImpl;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.Color4fc;
import com.github.elenterius.fabiko.visualization.PointDrawHelper;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author jsalvo
 * @author Elenterius
 */
public class ConnectedChainsWithEmbeddedTargets extends DemoScene {

	public static final float Y_ROTATION = Math.toRadians(1f);
	public static final float X_ROTATION = Math.toRadians(0.5f);

	private final PointDrawHelper pointDrawHelper = new PointDrawHelper();

	private final Vector3fc targetOrigin = new Vector3f(20f, 0f, 20f);
	private final Vector3f targetPos = new Vector3f();
	private final Vector3f targetOffset = new Vector3f(20f, 0f, 0f);

	public ConnectedChainsWithEmbeddedTargets() {
		title = "Connected chains with embedded targets";
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

		float clockwiseAngle = Math.toRadians(90f);
		float anticlockwiseAngle = Math.toRadians(45f);

		FabrikChain3f secondChain = FabrikChain3f.builder()
				.addHingeConstrainedBaseBone(new Vector3f(), new Vector3f(1, 0, 0), 15f, Y_AXIS, clockwiseAngle, anticlockwiseAngle, X_AXIS, false)
				.addFreelyRotatingRotorBone(X_AXIS, 20f, true)
				.addFreelyRotatingRotorBone(X_AXIS, 20f, true)
				.addFreelyRotatingRotorBone(X_AXIS, 20f, true)
				.build();

		secondChain.setUseEmbeddedTarget(true);

		// Other potential options for basebone constraint types
		//.addFreelyRotatingHingeBaseBone;
		//.setFreelyRotatingLocalHingedBasebone(Y_AXIS);
		//.setHingeBaseboneConstraint(GLOBAL_HINGE, Y_AXIS, 90.0f, 45.0f, X_AXIS);
		//.setRotorBaseboneConstraint(GLOBAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);
		//.setRotorBaseboneConstraint(LOCAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);

		structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
		structureDrawInfo.addChainDrawInfo(secondChain, Color4fc.GREY);
	}

	@Override
	public void draw(Matrix4fc modelViewProjection) {
		targetOffset.rotateY(Y_ROTATION);
		targetOffset.rotateX(X_ROTATION);
		targetOrigin.add(targetOffset, targetPos);

		structure.getChain(1).setEmbeddedTargetFrom(targetPos);
		pointDrawHelper.draw(targetPos, Color4fc.ORANGE, 4f, modelViewProjection);
	}

}
