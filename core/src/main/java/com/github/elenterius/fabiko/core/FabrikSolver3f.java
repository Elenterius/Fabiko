package com.github.elenterius.fabiko.core;

import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public class FabrikSolver3f {

	public float[] solveForTarget(FabrikStructure3f structure, float targetX, float targetY, float targetZ) {
		return solveForTarget(structure, new Vector3f(targetX, targetY, targetZ));
	}

	/// Solve the [structure][FabrikStructure3f] for the given target location.
	///
	/// All chains in this structure are solved for the given target location EXCEPT those which have embedded targets
	/// enabled, which are solved for the target location embedded in the chain.
	///
	/// After this method has been executed, all chains attached to this structure will have been updated.
	public float[] solveForTarget(FabrikStructure3f structure, Vector3fc target) {
		List<FabrikChain3f> chains = structure.getChains();
		float[] solveDistances = new float[chains.size()];

		for (int i = 0; i < chains.size(); i++) {
			FabrikChain3f chain = chains.get(i);
			int connectedToChainId = chain.getConnectedToChainId();

			// If this chain isn't connected to another chain then update as normal...
			if (connectedToChainId == -1) {
				if (chain.isEmbeddedTargetEnabled()) {
					solveDistances[i] = solveForTarget(chain, chain.getEmbeddedTarget());
				}
				else {
					solveDistances[i] = solveForTarget(chain, target);
				}
				continue;
			}

			FabrikChain3f hostChain = structure.getChain(connectedToChainId);
			FabrikBone3f hostBone = hostChain.getBone(chain.getConnectedToBoneIndex());
			chain.setBaseLocation(hostBone.getBoneConnectionPointLocation());

			// Now that we've clamped the base location of this chain to the start or end point of the bone in the chain we are connected to, it's
			// time to deal with any base bone constraints...

			FabrikJoint3f baseBoneJoint = chain.getBaseBone().getJoint();
			switch (baseBoneJoint) {
				case FabrikJoint3f.GlobalHinge ignored -> {
					// Nothing to do, because these will be handled in FabrikChain3D.solveIK() as we do not need information from another chain to handle them.
				}
				case FabrikJoint3f.GlobalRotor ignored -> {
					// Nothing to do because the basebone constraint is not relative to bones in other chains in this structure
				}
				case FabrikJoint3f.LocalRotor localRotor -> {
					Vector3fc relativeBaseboneConstraint = hostBone.getOrientation().transform(chain.getBaseboneConstraint(), new Vector3f());
					chain.setBaseboneRelativeConstraintFrom(relativeBaseboneConstraint); //TODO: quaternions
				}
				case FabrikJoint3f.LocalHinge localHinge -> {
					Quaternionfc hostOrientation = hostBone.getOrientation();
					Vector3fc relativeBaseboneConstraint = hostOrientation.transform(chain.getBaseboneConstraint(), new Vector3f());
					chain.setBaseboneRelativeConstraintFrom(relativeBaseboneConstraint);  //TODO: quaternions
					chain.setBaseboneRelativeReferenceConstraintFrom(hostOrientation.transform(localHinge.getReferenceAxis(), new Vector3f()));
				}
				default -> throw new IllegalStateException("Unexpected value: " + baseBoneJoint);
			}

			if (chain.isEmbeddedTargetEnabled()) {
				solveDistances[i] = solveForTarget(chain, chain.getEmbeddedTarget());
			}
			else {
				solveDistances[i] = solveForTarget(chain, target);
			}
		}

		return solveDistances;
	}

	public float solveForTarget(FabrikChain3f chain, float targetX, float targetY, float targetZ) {
		return solveForTarget(chain, new Vector3f(targetX, targetY, targetZ));
	}

	public float solveForTarget(FabrikChain3f chain, Vector3fc target) {
		if (chain.isEmpty()) {
			throw new IllegalStateException("Can't solve FABRIK chain without any bones.");
		}

		if (chain.isSolved(target)) {
			return chain.currentSolveDistance;
		}

		FabrikBone3f[] bestSolution = new FabrikBone3f[chain.bones.length];
		//clone bones
		for (int i = 0; i < chain.bones.length; i++) {
			bestSolution[i] = new FabrikBone3f(chain.bones[i]);
		}

		float solveDistance;
		float bestSolveDistance = Float.MAX_VALUE;
		float prevSolveDistance = Float.MAX_VALUE;

		for (int iteration = 0; iteration < chain.maxIterationAttempts; iteration++) {

			solveDistance = solveIteration(chain, target);

			if (solveDistance < bestSolveDistance) {
				bestSolveDistance = solveDistance;

				//update bones
				for (int i = 0; i < chain.bones.length; i++) {
					bestSolution[i].setFrom(chain.bones[i]);
				}

				if (solveDistance <= chain.solveDistanceThreshold) {
					break;
				}
			}
			else {
				// Did we grind to a halt? If so break out of loop to set the best distance and solution that we have
				if (Math.abs(solveDistance - prevSolveDistance) < chain.minIterationChange) {
					//System.out.println("Ground to halt on iteration: " + i);
					break;
				}
			}

			prevSolveDistance = solveDistance;
		}

		chain.currentSolveDistance = bestSolveDistance;

		//update bones
		for (int i = 0; i < chain.bones.length; i++) {
			chain.bones[i].setFrom(bestSolution[i]);
		}
		//		chain.bones = bestSolution;

		chain.lastBaseLocation.set(chain.getBaseLocation());
		chain.lastTargetLocation.set(target);

		return bestSolveDistance;
	}

	private float solveIteration(FabrikChain3f chain, Vector3fc target) {

		// forward pass from end effector to base bone
		int endEffectorIndex = chain.bones.length - 1;
		forwardPassEndEffector(chain, chain.bones[endEffectorIndex], endEffectorIndex, target);
		for (int i = endEffectorIndex - 1; i >= 0; i--) {
			forwardPassBone(chain, chain.bones[i], i);
		}

		// backward pass from base bone to end effector
		backwardPassBaseBone(chain, chain.bones[0], 0);
		for (int i = 1; i < chain.bones.length; i++) {
			backwardPassBone(chain, chain.bones[i], i);
		}

		chain.lastTargetLocation.set(target);

		return chain.bones[endEffectorIndex].getEndLocation().distance(target);
	}

	private void backwardPassBaseBone(FabrikChain3f chain, FabrikBone3f bone, int boneIndex) {
		if (chain.fixedBaseMode) {
			// snap the start location of the base bone back to the fixed base
			bone.setStartLocation(chain.fixedBaseLocation);
		}
		else {
			// project it backwards from the end to the start by its length
			Vector3f scaledDirection = bone.getDirection().mul(bone.length(), new Vector3f());
			bone.setStartLocation(bone.getEndLocation().sub(scaledDirection, scaledDirection));
		}

		Vector3f boneDirection = new Vector3f(bone.getDirection());

		switch (bone.getJoint()) {
			case FabrikJoint3f.LocalRotor localRotor -> handleBackwardPassBaseBone(chain, localRotor, bone, boneIndex, boneDirection);
			case FabrikJoint3f.GlobalRotor globalRotor -> handleBackwardPassBaseBone(chain, globalRotor, bone, boneIndex, boneDirection);
			case FabrikJoint3f.LocalHinge localHinge -> handleBackwardPassBaseBone(chain, localHinge, bone, boneIndex, boneDirection);
			case FabrikJoint3f.GlobalHinge globalHinge -> handleBackwardPassBaseBone(chain, globalHinge, bone, boneIndex, boneDirection);
			default -> {
				//base bone has no constraint
			}
		}

		//		boneDirection.normalize();

		Vector3f newEndLocation = boneDirection.mul(bone.length()).add(bone.getStartLocation());
		bone.setEndLocation(newEndLocation);

		if (chain.bones.length > 1) {
			chain.bones[1].setStartLocation(newEndLocation);
		}
	}

	private void handleBackwardPassBaseBone(FabrikChain3f chain, FabrikJoint3f.GlobalRotor rotor, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		float angleBetween = chain.baseboneConstraint.angle(boneDirection);
		float constraintAngle = rotor.getConstraintAngle();
		if (angleBetween > constraintAngle) {
			// keep this bone direction constrained within the rotor about the previous bone direction
			Vector3f correctionAxis = chain.baseboneConstraint.cross(boneDirection, new Vector3f()).normalize();
			chain.baseboneConstraint.rotateAxis(constraintAngle, correctionAxis.x, correctionAxis.y, correctionAxis.z, boneDirection);
		}
	}

	private void handleBackwardPassBaseBone(FabrikChain3f chain, FabrikJoint3f.LocalRotor rotor, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		float angleBetween = chain.baseboneRelativeConstraint.angle(boneDirection);
		float constraintAngle = rotor.getConstraintAngle();
		if (angleBetween > constraintAngle) {
			// keep this bone direction constrained within the rotor about the previous bone direction
			Vector3f correctionAxis = chain.baseboneRelativeConstraint.cross(boneDirection, new Vector3f()).normalize();
			chain.baseboneRelativeConstraint.rotateAxis(constraintAngle, correctionAxis.x, correctionAxis.y, correctionAxis.z, boneDirection);
		}
	}

	private void backwardPassBone(FabrikChain3f chain, FabrikBone3f bone, int boneIndex) {
		Vector3f boneDirection = new Vector3f(bone.getDirection());

		switch (bone.getJoint()) {
			case FabrikJoint3f.Rotor rotor -> handleBackwardPass(chain, rotor, bone, boneIndex, boneDirection);
			case FabrikJoint3f.LocalHinge localHinge -> handleBackwardPass(chain, localHinge, bone, boneIndex, boneDirection);
			case FabrikJoint3f.GlobalHinge globalHinge -> handleBackwardPass(chain, globalHinge, bone, boneIndex, boneDirection);
			default -> throw new IllegalStateException("Unexpected value: " + bone.getJoint());
		}

		//		boneDirection.normalize();

		Vector3f newEndLocation = boneDirection.mul(bone.length()).add(bone.getStartLocation());
		bone.setEndLocation(newEndLocation);

		if (boneIndex < chain.bones.length - 1) {
			chain.bones[boneIndex + 1].setStartLocation(newEndLocation);
		}
	}

	private void handleBackwardPass(FabrikChain3f chain, FabrikJoint3f.Rotor rotor, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		Vector3fc prevBoneDirection = chain.bones[boneIndex - 1].getDirection();

		float angleBetween = prevBoneDirection.angle(boneDirection);
		float constraintAngle = rotor.getConstraintAngle();
		if (angleBetween > constraintAngle) {
			// keep this bone direction constrained within the rotor about the previous bone direction
			Vector3f correctionAxis = prevBoneDirection.cross(boneDirection, new Vector3f()).normalize();
			prevBoneDirection.rotateAxis(constraintAngle, correctionAxis.x, correctionAxis.y, correctionAxis.z, boneDirection);
		}
	}

	private void handleBackwardPass(FabrikChain3f chain, FabrikJoint3f.LocalHinge localHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		Quaternionfc prevBoneOrientation = chain.bones[boneIndex - 1].getOrientation();

		// transform the hinge rotation axis to be relative to the previous bone in the chain (i.e. previous bone's frame of reference)
		Vector3fc relativeRotationAxis = prevBoneOrientation.transform(localHinge.getRotationAxis(), new Vector3f());

		// project this bone direction onto the plane described by the hinge rotation axis
		JomlMath.projectOntoPlane(boneDirection, relativeRotationAxis).normalize();

		if (localHinge.isConstrained()) {
			// reference axis in local space
			Vector3fc relativeReferenceAxis = prevBoneOrientation.transform(localHinge.getReferenceAxis(), new Vector3f());

			// signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone direction
			float signedAngleBetween = relativeReferenceAxis.angleSigned(boneDirection, relativeRotationAxis); // in the range of -pi to pi

			float cwConstraintAngle = -localHinge.getClockwiseConstraintAngle();
			float acwConstraintAngle = localHinge.getAntiClockwiseConstraintAngle();

			if (signedAngleBetween > acwConstraintAngle) {
				relativeReferenceAxis.rotateAxis(acwConstraintAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), boneDirection);
			}
			else if (signedAngleBetween < cwConstraintAngle) {
				relativeReferenceAxis.rotateAxis(cwConstraintAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), boneDirection);
			}
		}
	}

	private void handleBackwardPass(FabrikChain3f chain, FabrikJoint3f.GlobalHinge globalHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		Vector3fc rotationAxis = globalHinge.getRotationAxis();

		// project this bone direction onto the plane described by the hinge rotation axis
		JomlMath.projectOntoPlane(boneDirection, rotationAxis).normalize();

		if (globalHinge.isConstrained()) {
			Vector3fc referenceAxis = globalHinge.getReferenceAxis();

			// signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone direction
			float signedAngleBetween = referenceAxis.angleSigned(boneDirection, rotationAxis); // in the range of -pi to pi

			float cwConstraintAngle = -globalHinge.getClockwiseConstraintAngle();
			float acwConstraintAngle = globalHinge.getAntiClockwiseConstraintAngle();

			// Make our bone inner-to-outer UV the hinge reference axis rotated by its maximum clockwise or anticlockwise rotation as required
			if (signedAngleBetween > acwConstraintAngle) {
				referenceAxis.rotateAxis(acwConstraintAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), boneDirection);
			}
			else if (signedAngleBetween < cwConstraintAngle) {
				referenceAxis.rotateAxis(cwConstraintAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), boneDirection);
			}
		}
	}

	private void handleBackwardPassBaseBone(FabrikChain3f chain, FabrikJoint3f.LocalHinge localHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		Vector3fc relativeRotationAxis = chain.baseboneRelativeConstraint;

		// project this bone direction onto the plane described by the hinge rotation axis
		JomlMath.projectOntoPlane(boneDirection, relativeRotationAxis).normalize();

		if (localHinge.isConstrained()) {
			// reference axis in local space
			Vector3fc relativeReferenceAxis = chain.baseboneRelativeReferenceConstraint;

			// signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone direction
			float signedAngleBetween = relativeReferenceAxis.angleSigned(boneDirection, relativeRotationAxis); // in the range of -pi to pi

			float cwConstraintAngle = -localHinge.getClockwiseConstraintAngle();
			float acwConstraintAngle = localHinge.getAntiClockwiseConstraintAngle();

			if (signedAngleBetween > acwConstraintAngle) {
				relativeReferenceAxis.rotateAxis(acwConstraintAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), boneDirection);
			}
			else if (signedAngleBetween < cwConstraintAngle) {
				relativeReferenceAxis.rotateAxis(cwConstraintAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), boneDirection);
			}
		}
	}

	private void handleBackwardPassBaseBone(FabrikChain3f chain, FabrikJoint3f.GlobalHinge globalHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirection) {
		Vector3fc rotationAxis = globalHinge.getRotationAxis();

		// project this bone direction onto the plane described by the hinge rotation axis
		JomlMath.projectOntoPlane(boneDirection, rotationAxis).normalize();

		if (globalHinge.isConstrained()) {
			Vector3fc referenceAxis = globalHinge.getReferenceAxis();

			// signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone direction
			float signedAngleBetween = referenceAxis.angleSigned(boneDirection, rotationAxis); // in the range of -pi to pi

			float cwConstraintAngle = -globalHinge.getClockwiseConstraintAngle();
			float acwConstraintAngle = globalHinge.getAntiClockwiseConstraintAngle();

			if (signedAngleBetween > acwConstraintAngle) {
				referenceAxis.rotateAxis(acwConstraintAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), boneDirection);
			}
			else if (signedAngleBetween < cwConstraintAngle) {
				referenceAxis.rotateAxis(cwConstraintAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), boneDirection);
			}
		}
	}

	private void forwardPassBone(FabrikChain3f chain, FabrikBone3f bone, int boneIndex) {
		Vector3f boneDirectionNegated = bone.getDirection().negate(new Vector3f());

		switch (bone.getJoint()) {
			case FabrikJoint3f.Rotor rotor -> handleForwardPass(chain, rotor, bone, boneIndex, boneDirectionNegated, boneIndex == 0);
			case FabrikJoint3f.LocalHinge localHinge -> handleForwardPass(chain, localHinge, bone, boneIndex, boneDirectionNegated, boneIndex == 0);
			case FabrikJoint3f.GlobalHinge globalHinge -> handleForwardPass(chain, globalHinge, bone, boneIndex, boneDirectionNegated, boneIndex == 0);
			default -> throw new IllegalStateException("Unexpected value: " + bone.getJoint());
		}

		//		boneDirectionNegated.normalize();

		Vector3f newStartLocation = boneDirectionNegated.mul(bone.length()).add(bone.getEndLocation());
		bone.setStartLocation(newStartLocation);

		if (boneIndex > 0) {
			chain.bones[boneIndex - 1].setEndLocation(newStartLocation);
		}
	}

	private void handleForwardPass(FabrikChain3f chain, FabrikJoint3f.Rotor rotor, FabrikBone3f bone, int boneIndex, Vector3f boneDirectionNegated, boolean isBasebone) {
		FabrikBone3f prevBone = chain.bones[boneIndex + 1];
		Vector3f prevBoneDirectionNegated = prevBone.getDirection().negate(new Vector3f());

		float angleBetween = prevBoneDirectionNegated.angle(boneDirectionNegated);
		float constraintAngle = rotor.getConstraintAngle();
		if (angleBetween > constraintAngle) {
			// the axis which we need to rotate around is the one perpendicular to the two vectors (cross-product of our two vectors)
			// Note: We do not have to worry about both vectors being the same or pointing in opposite directions
			// because if their bones are the same direction they will not have an angle greater than the angle limit,
			// and if they point opposite directions we shouldn't reach the precise max angle limit (PI)
			Vector3f correctionAxis = prevBoneDirectionNegated.cross(boneDirectionNegated, new Vector3f()).normalize();
			prevBoneDirectionNegated.rotateAxis(constraintAngle, correctionAxis.x, correctionAxis.y, correctionAxis.z, boneDirectionNegated);
		}
	}

	private void handleForwardPass(FabrikChain3f chain, FabrikJoint3f.LocalHinge localHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirectionNegated, boolean isBasebone) {
		Vector3f relativeRotationAxis;

		if (isBasebone) {
			relativeRotationAxis = chain.baseboneRelativeConstraint;
		}
		else {
			FabrikBone3f nextBone = chain.bones[boneIndex - 1];
			Quaternionfc orientation = nextBone.getOrientation();
			relativeRotationAxis = orientation.transform(localHinge.getRotationAxis(), new Vector3f());
		}

		JomlMath.projectOntoPlane(boneDirectionNegated, relativeRotationAxis).normalize();

		//NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.
	}

	private void handleForwardPass(FabrikChain3f chain, FabrikJoint3f.GlobalHinge globalHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirectionNegated, boolean isBasebone) {
		JomlMath.projectOntoPlane(boneDirectionNegated, globalHinge.getRotationAxis()).normalize();

		//NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.
	}

	private void forwardPassEndEffector(FabrikChain3f chain, FabrikBone3f bone, int boneIndex, Vector3fc target) {
		// snap the end effector's end location to the target
		bone.setEndLocation(target);

		// Get the UV between the target / end-location (which are now the same) and the start location of this bone
		Vector3f boneDirectionNegated = bone.getDirection().negate(new Vector3f());

		switch (bone.getJoint()) {
			case FabrikJoint3f.Rotor ignored -> {
				// Ball joints do not get constrained on this forward pass
			}
			case FabrikJoint3f.LocalHinge localHinge -> handleForwardPassEndEffector(chain, localHinge, bone, boneIndex, boneDirectionNegated, boneIndex == 0);
			case FabrikJoint3f.GlobalHinge globalHinge -> handleForwardPassEndEffector(chain, globalHinge, bone, boneIndex, boneDirectionNegated, boneIndex == 0);
			default -> throw new IllegalStateException("Unexpected value: " + bone.getJoint());
		}

		//		boneDirectionNegated.normalize();

		Vector3f newStartLocation = boneDirectionNegated.mul(bone.length()).add(bone.getEndLocation());
		bone.setStartLocation(newStartLocation);

		if (boneIndex > 0) {
			chain.bones[boneIndex - 1].setEndLocation(newStartLocation);
		}
	}

	private void handleForwardPassEndEffector(FabrikChain3f chain, FabrikJoint3f.LocalHinge localHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirectionNegated, boolean isBasebone) {
		Vector3f relativeRotationAxis;

		if (isBasebone) {
			relativeRotationAxis = chain.baseboneRelativeConstraint;
		}
		else {
			FabrikBone3f nextBone = chain.bones[boneIndex - 1];
			Quaternionfc orientation = nextBone.getOrientation();
			relativeRotationAxis = orientation.transform(localHinge.getRotationAxis(), new Vector3f());
		}

		JomlMath.projectOntoPlane(boneDirectionNegated, relativeRotationAxis).normalize();
	}

	private void handleForwardPassEndEffector(FabrikChain3f chain, FabrikJoint3f.GlobalHinge globalHinge, FabrikBone3f bone, int boneIndex, Vector3f boneDirectionNegated, boolean isBasebone) {
		// If the end effector is global hinged then we have to snap to it, then keep that
		// resulting outer-to-inner UV in the plane of the hinge rotation axis
		// Global hinges get constrained to the hinge rotation axis, but not the reference axis within the hinge plane
		JomlMath.projectOntoPlane(boneDirectionNegated, globalHinge.getRotationAxis()).normalize();
	}

}
