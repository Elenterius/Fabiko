package com.github.elenterius.fabiko.core;

import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FabrikChain3f {

	protected FabrikBone3f[] bones;
	protected float length;

	/// Is a [unit][Vector3f#normalize()] vector.
	protected Vector3f baseboneConstraint = new Vector3f();

	/// Is a [unit][Vector3f#normalize()] vector.
	protected Vector3f baseboneRelativeConstraint = new Vector3f();

	/// Is a [unit][Vector3f#normalize()] vector.
	protected Vector3f baseboneRelativeReferenceConstraint = new Vector3f();

	protected Vector3f lastBaseLocation = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	protected Vector3f lastTargetLocation = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

	protected float currentSolveDistance = Float.MAX_VALUE;
	protected float solveDistanceThreshold = 1f;
	protected int maxIterationAttempts = 20;
	protected float minIterationChange = 0.01f;

	protected boolean fixedBaseMode = true;
	protected Vector3fc fixedBaseLocation = new Vector3f();

	private final Vector3f embeddedTarget = new Vector3f();
	private boolean useEmbeddedTarget = false;

	private int connectedToChainId = -1;
	private int connectedToBoneIndex = -1;

	public FabrikChain3f(Collection<FabrikBone3f> bones) {
		this.bones = bones.toArray(FabrikBone3f[]::new);

		float length = 0;
		for (FabrikBone3f bone : bones) {
			length += bone.length();
		}
		this.length = length;
	}

	public FabrikChain3f(FabrikBone3f... bones) {
		this.bones = Arrays.copyOf(bones, bones.length);

		float length = 0;
		for (FabrikBone3f bone : bones) {
			length += bone.length();
		}
		this.length = length;
	}

	public FabrikChain3f(FabrikChain3f chain) {
		bones = Arrays.copyOf(chain.bones, chain.bones.length);
		length = chain.length;
		baseboneConstraint.set(chain.baseboneConstraint);
		baseboneRelativeConstraint.set(chain.baseboneRelativeConstraint);
		baseboneRelativeReferenceConstraint.set(chain.baseboneRelativeReferenceConstraint);
		lastBaseLocation.set(chain.lastBaseLocation);
		lastTargetLocation.set(chain.lastTargetLocation);
		currentSolveDistance = chain.currentSolveDistance;
		solveDistanceThreshold = chain.solveDistanceThreshold;
		maxIterationAttempts = chain.maxIterationAttempts;
		minIterationChange = chain.minIterationChange;
		fixedBaseMode = chain.fixedBaseMode;
		fixedBaseLocation = chain.fixedBaseLocation;
		embeddedTarget.set(chain.embeddedTarget);
		embeddedTarget.set(chain.embeddedTarget);
		useEmbeddedTarget = chain.useEmbeddedTarget;
		connectedToChainId = chain.connectedToChainId;
		connectedToBoneIndex = chain.connectedToBoneIndex;
	}

	public static BaseBoneBuilder builder() {
		return new BaseBoneBuilder();
	}

	public boolean isEmpty() {
		return bones.length == 0;
	}

	public Vector3fc getBaseLocation() {
		return bones[0].getStartLocation();
	}

	public boolean isSolved(Vector3fc target) {
		return lastTargetLocation.equals(target, 0.001f) && lastBaseLocation.equals(getBaseLocation(), 0.001f);
	}

	public FabrikBone3f getBone(int index) {
		return bones[index];
	}

	public FabrikBone3f getBaseBone() {
		return bones[0];
	}

	public FabrikBone3f getEndEffectorBone() {
		return bones[bones.length - 1];
	}

	public FabrikBone3f[] getBones() {
		return bones;
	}

	public int getBoneCount() {
		return bones.length;
	}

	public float getLength() {
		return length;
	}

	public Vector3fc getLastTargetLocation() {
		return lastTargetLocation;
	}

	public int getMaxIterationAttempts() {
		return maxIterationAttempts;
	}

	public float getMinIterationChange() {
		return minIterationChange;
	}

	public float getSolveDistanceThreshold() {
		return solveDistanceThreshold;
	}

	public void setFixedBaseMode(boolean flag) {
		// Enforce that a chain connected to another chain stays in fixed base mode (i.e. it moves with the chain it's connected to instead of independently)
		if (!flag && connectedToChainId != -1) {
			throw new RuntimeException("This chain is connected to another chain so must remain in fixed base mode.");
		}

		// We cannot have a freely moving base location AND constrain the basebone to an absolute direction
		if (!flag && getBaseBone().getJoint() instanceof FabrikJoint3f.GlobalRotor) {
			throw new RuntimeException("Cannot set a non-fixed base mode when the chain's constraint type is a Global Rotor.");
		}

		fixedBaseMode = flag;
	}

	public void setBaseLocation(Vector3fc baseLocation) {
		fixedBaseLocation = baseLocation;
	}

	public int getConnectedToChainId() {
		return connectedToChainId;
	}

	public int getConnectedToBoneIndex() {
		return connectedToBoneIndex;
	}

	public boolean isEmbeddedTargetEnabled() {
		return useEmbeddedTarget;
	}

	public void setUseEmbeddedTarget(boolean enable) {
		useEmbeddedTarget = enable;
	}

	public void setEmbeddedTargetFrom(Vector3fc target) {
		embeddedTarget.set(target);
	}

	public Vector3fc getEmbeddedTarget() {
		return embeddedTarget;
	}

	public Vector3fc getBaseboneConstraint() {
		return baseboneConstraint;
	}

	/// @param relativeConstraint must be a `unit` vector
	public void setBaseboneRelativeConstraintFrom(Vector3fc relativeConstraint) {
		baseboneRelativeConstraint.set(relativeConstraint);
	}

	public Vector3fc getBaseboneRelativeConstraint() {
		return baseboneRelativeConstraint;
	}

	public void setBaseboneRelativeReferenceConstraintFrom(Vector3fc relativeReferenceConstraint) {
		baseboneRelativeReferenceConstraint.set(relativeReferenceConstraint);
	}

	public void connectToStructure(FabrikStructure3f structure, int existingChainNumber, int existingBoneNumber) {
		int numChains = structure.getChainCount();

		if (existingChainNumber > numChains) {
			throw new IllegalArgumentException("Structure does not contain a chain " + existingChainNumber + " - it has " + numChains + " chains.");
		}

		int numBones = structure.getChain(existingChainNumber).getBoneCount();
		if (existingBoneNumber > numBones) {
			throw new IllegalArgumentException("Chain does not contain a bone " + existingBoneNumber + " - it has " + numBones + " bones.");
		}

		connectedToChainId = existingChainNumber;
		connectedToBoneIndex = existingBoneNumber;
	}

	private static class InternalBuilder {
		protected final List<FabrikBone3f> bones = new ArrayList<>();
		protected final Vector3f fixedBaseLocation = new Vector3f();
		protected final Vector3f baseboneConstraintUV = new Vector3f();
		protected final Vector3f baseboneRelativeConstraintUV = new Vector3f();

		private InternalBuilder() {
		}

		private FabrikChain3f build() {
			FabrikChain3f chain = new FabrikChain3f(bones);
			chain.fixedBaseLocation = fixedBaseLocation;
			chain.baseboneConstraint.set(baseboneConstraintUV);
			chain.baseboneRelativeConstraint.set(baseboneRelativeConstraintUV);
			return chain;
		}
	}

	public static class BaseBoneBuilder {
		InternalBuilder internalBuilder = new InternalBuilder();
		ConsecutiveBoneBuilder internalConsecutiveBuilder = new ConsecutiveBoneBuilder(internalBuilder);

		private BaseBoneBuilder() {
		}

		/// This method only exists to document that an unconstrained bone is a freely rotating rotor bone
		///
		/// @deprecated use [#addFreelyRotatingRotorBaseBone] instead
		@Deprecated
		public ConsecutiveBoneBuilder addUnconstrainedBaseBone(Vector3fc location, Vector3fc direction, float length) {
			return addFreelyRotatingRotorBaseBone(location, direction, length, true);
		}

		public ConsecutiveBoneBuilder addFreelyRotatingRotorBaseBone(Vector3fc location, Vector3fc direction, float length, boolean isLocalRotor) {
			return addRotorConstrainedBaseBone(location, direction, length, direction, Math.PI_f, isLocalRotor);
		}

		/// @param location        base location
		/// @param direction       where the bone is pointing towards
		/// @param length          bone length
		/// @param constraintAngle in radians
		/// @param isLocalRotor    when false it's a GlobalRotor
		/// @return ConsecutiveBoneBuilder
		public ConsecutiveBoneBuilder addRotorConstrainedBaseBone(Vector3fc location, Vector3fc direction, float length, float constraintAngle, boolean isLocalRotor) {
			return addRotorConstrainedBaseBone(location, direction, length, direction, constraintAngle, isLocalRotor);
		}

		public ConsecutiveBoneBuilder addRotorConstrainedBaseBone(Vector3fc location, Vector3fc direction, float length, Vector3fc constraintAxis, float constraintAngle, boolean isLocalRotor) {
			if (!JomlMath.isUnitVector(direction)) {
				throw new IllegalArgumentException("Direction is not a unit vector");
			}
			if (length < 0f) {
				throw new IllegalArgumentException("Length must be greater than or equal to zero.");
			}
			if (!JomlMath.isUnitVector(constraintAxis)) {
				throw new IllegalArgumentException("Constraint axis is not a unit vector.");
			}

			FabrikJoint3f.Rotor joint = isLocalRotor ? new FabrikJoint3f.LocalRotor() : new FabrikJoint3f.GlobalRotor();
			joint.setConstraintAngle(constraintAngle);

			FabrikBone3f baseBone = new FabrikBone3f(location, direction, length, joint);
			internalBuilder.bones.add(baseBone);

			internalBuilder.fixedBaseLocation.set(location);
			internalBuilder.baseboneConstraintUV.set(constraintAxis);
			internalBuilder.baseboneRelativeConstraintUV.set(internalBuilder.baseboneConstraintUV);

			return internalConsecutiveBuilder;
		}

		public ConsecutiveBoneBuilder addFreelyRotatingHingeBaseBone(Vector3fc location, Vector3fc direction, float length, Vector3fc rotationAxis, boolean isLocalHinge) {
			Vector3f referenceAxis = JomlMath.perpendicular(rotationAxis, new Vector3f());
			return addHingeConstrainedBaseBone(location, direction, length, rotationAxis, Math.PI_f, Math.PI_f, referenceAxis, isLocalHinge);
		}

		public ConsecutiveBoneBuilder addHingeConstrainedBaseBone(Vector3fc location, Vector3fc direction, float length, Vector3fc rotationAxis, float clockwiseAngle, float anticlockwiseAngle, boolean isLocalHinge) {
			return addHingeConstrainedBaseBone(location, direction, length, rotationAxis, clockwiseAngle, anticlockwiseAngle, direction, isLocalHinge);
		}

		public ConsecutiveBoneBuilder addHingeConstrainedBaseBone(Vector3fc location, Vector3fc direction, float length, Vector3fc rotationAxis, float clockwiseAngle, float anticlockwiseAngle, Vector3fc referenceAxis, boolean isLocalHinge) {
			if (!JomlMath.isUnitVector(direction)) {
				throw new IllegalArgumentException("Direction is not a unit vector.");
			}
			if (length < 0f) {
				throw new IllegalArgumentException("Length must be a greater than or equal to zero.");
			}
			if (!JomlMath.isUnitVector(rotationAxis)) {
				throw new IllegalArgumentException("Rotation axis is not a unit vector.");
			}
			if (!JomlMath.isUnitVector(referenceAxis)) {
				throw new IllegalArgumentException("Reference axis is not a unit vector.");
			}
			if (!(JomlMath.isPerpendicular(rotationAxis, referenceAxis))) {
				throw new IllegalArgumentException("The reference axis must be in the plane of the rotation axis, i.e. they must be perpendicular.");
			}

			FabrikJoint3f.Hinge hinge = isLocalHinge ? new FabrikJoint3f.LocalHinge() : new FabrikJoint3f.GlobalHinge();
			hinge.setConstraintAngles(clockwiseAngle, anticlockwiseAngle);
			hinge.setRotationAxis(rotationAxis);
			hinge.setReferenceAxis(referenceAxis);

			FabrikBone3f baseBone = new FabrikBone3f(location, direction, length, hinge);
			internalBuilder.bones.add(baseBone);

			internalBuilder.fixedBaseLocation.set(location);
			internalBuilder.baseboneConstraintUV.set(rotationAxis);
			internalBuilder.baseboneRelativeConstraintUV.set(internalBuilder.baseboneConstraintUV);

			return internalConsecutiveBuilder;
		}

	}

	public static class ConsecutiveBoneBuilder {
		InternalBuilder internalBuilder;

		private ConsecutiveBoneBuilder(InternalBuilder internalBuilder) {
			this.internalBuilder = internalBuilder;
		}

		/// This method only exists to document that an unconstrained bone is a freely rotating rotor bone
		///
		/// @deprecated use [#addFreelyRotatingRotorBone] instead
		@Deprecated
		public ConsecutiveBoneBuilder addUnconstrainedBone(Vector3fc direction, float length) {
			addFreelyRotatingRotorBone(direction, length, true);
			return this;
		}

		public ConsecutiveBoneBuilder addFreelyRotatingRotorBone(Vector3fc direction, float length, boolean isLocalRotor) {
			addRotorConstrainedBone(direction, length, Math.PI_f, isLocalRotor);
			return this;
		}

		public ConsecutiveBoneBuilder addRotorConstrainedBone(Vector3fc direction, float length, float constraintAngle, boolean isLocalRotor) {
			if (!JomlMath.isUnitVector(direction)) {
				throw new IllegalArgumentException("Direction is not a unit vector.");
			}
			if (length < 0f) {
				throw new IllegalArgumentException("Length must be a greater than or equal to zero.");
			}

			FabrikJoint3f.Rotor rotor = isLocalRotor ? new FabrikJoint3f.LocalRotor() : new FabrikJoint3f.GlobalRotor();
			rotor.setConstraintAngle(constraintAngle);

			FabrikBone3f bone = new FabrikBone3f(internalBuilder.bones.getLast().getEndLocation(), direction, length, rotor);
			internalBuilder.bones.add(bone);

			return this;
		}

		public ConsecutiveBoneBuilder addFreelyRotatingHingeBone(Vector3fc direction, float length, Vector3fc rotationAxis, boolean isLocalHinge) {
			// Because we aren't constraining this bone to a reference axis within the hinge rotation axis we don't care about the
			// constraint reference axis and just generate an axis perpendicular to the rotation axis
			Vector3f referenceAxis = JomlMath.perpendicular(rotationAxis, new Vector3f());

			addHingeConstrainedBone(direction, length, rotationAxis, Math.PI_f, Math.PI_f, referenceAxis, isLocalHinge);

			return this;
		}

		public ConsecutiveBoneBuilder addHingeConstrainedBone(Vector3fc direction, float length, Vector3fc rotationAxis, float clockwiseAngle, float anticlockwiseAngle, Vector3fc referenceAxis, boolean isLocalHinge) {
			if (!JomlMath.isUnitVector(direction)) {
				throw new IllegalArgumentException("Direction is not a unit vector.");
			}
			if (length < 0f) {
				throw new IllegalArgumentException("Length must be a greater than or equal to zero.");
			}
			if (!JomlMath.isUnitVector(rotationAxis)) {
				throw new IllegalArgumentException("Rotation axis is not a unit vector.");
			}
			if (!JomlMath.isUnitVector(referenceAxis)) {
				throw new IllegalArgumentException("Reference axis is not a unit vector.");
			}
			if (!(JomlMath.isPerpendicular(rotationAxis, referenceAxis))) {
				throw new IllegalArgumentException("The reference axis must be in the plane of the rotation axis, i.e. they must be perpendicular.");
			}

			FabrikJoint3f.Hinge hinge = isLocalHinge ? new FabrikJoint3f.LocalHinge() : new FabrikJoint3f.GlobalHinge();
			hinge.setConstraintAngles(clockwiseAngle, anticlockwiseAngle);
			hinge.setRotationAxis(rotationAxis);
			hinge.setReferenceAxis(referenceAxis);

			FabrikBone3f bone = new FabrikBone3f(internalBuilder.bones.getLast().getEndLocation(), direction, length, hinge);
			internalBuilder.bones.add(bone);

			return this;
		}

		public FabrikChain3f build() {
			return internalBuilder.build();
		}

	}

}
