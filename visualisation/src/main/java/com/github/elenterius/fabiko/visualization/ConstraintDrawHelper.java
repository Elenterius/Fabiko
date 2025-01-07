/// The MIT License (MIT)
///
/// Copyright (c) 2016-2020 Alastair Lansley / Federation University Australia
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.


package com.github.elenterius.fabiko.visualization;

import com.github.elenterius.fabiko.core.*;
import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Math;
import org.joml.*;

/// Refactored copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/FabrikConstraint3D.java">FabrikConstraint3D</a>
public class ConstraintDrawHelper {

	private static final Color4fc ROTOR_COLOUR = Color4fc.ORANGE_RED;
	private static final Color4fc GLOBAL_HINGE_COLOUR = Color4fc.YELLOW;
	private static final Color4fc LOCAL_HINGE_COLOUR = Color4fc.CYAN;
	private static final Color4fc REFERENCE_AXIS_COLOUR = Color4fc.MAGENTA;
	private static final Color4fc CLOCKWISE_CONSTRAINT_COLOUR = Color4fc.BLUE;
	private static final Color4fc ANTICLOCKWISE_CONSTRAINT_COLOUR = Color4fc.RED;

	// rotor cone and the radius of the cone and circle describing the hinge axes
	private static final float CONE_LENGTH_FACTOR = 0.3f;
	private static final float RADIUS_FACTOR = 0.25f;
	private static final int NUM_CONE_LINES = 12;

	private static final float ROTATION_STEP_ANGLE = Math.PI_TIMES_2_f / NUM_CONE_LINES;

	private final CircleDrawHelper circleDrawHelper;
	private final LineDrawHelper lineDrawHelper;

	/**
	 * Default constructor.
	 */
	public ConstraintDrawHelper() {
		circleDrawHelper = new CircleDrawHelper();
		lineDrawHelper = new LineDrawHelper();
	}

	/// Hinge joints are drawn as circles aligned to the hinge rotation axis,
	/// with an optional reference axis within the plane of the circle if required.
	///
	/// @param referenceOrientation As bones are constrained about the orientation relative to the previous bone in the chain, this is the orientation of the previous bone.
	private void drawLocalHingeConstraints(FabrikBone3f bone, FabrikJoint3f.LocalHinge localHinge, Quaternionfc referenceOrientation, float lineWidth, Matrix4fc modelViewProjection) {
		float boneLength = bone.length();
		float radius = boneLength * RADIUS_FACTOR;
		Vector3fc lineStart = bone.getStartLocation();

		// transform the hinge rotation axis into the previous bone's frame of reference
		Vector3fc relativeRotationAxis = referenceOrientation.transform(localHinge.getRotationAxis(), new Vector3f());

		// draw the circle describing the hinge rotation axis
		circleDrawHelper.draw(lineStart, relativeRotationAxis, radius, LOCAL_HINGE_COLOUR, lineWidth, modelViewProjection);

		// draw the hinge reference and clockwise/anticlockwise constraints if necessary
		if (localHinge.isConstrained()) {
			// reference axis in local space
			Vector3fc relativeReferenceAxis = referenceOrientation.transform(localHinge.getReferenceAxis(), new Vector3f());

			Vector3f temp = new Vector3f();

			lineDrawHelper.draw(lineStart, relativeReferenceAxis.mul(radius, temp).add(lineStart), REFERENCE_AXIS_COLOUR, lineWidth, modelViewProjection);

			float anticlockwiseAngle = localHinge.getAntiClockwiseConstraintAngle();
			float clockwiseAngle = -localHinge.getClockwiseConstraintAngle();

			Vector3f anticlockwiseDirection = relativeReferenceAxis.rotateAxis(anticlockwiseAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), temp);
			Vector3fc anticlockwisePoint = anticlockwiseDirection.mul(radius).add(lineStart);
			lineDrawHelper.draw(lineStart, anticlockwisePoint, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, modelViewProjection);

			Vector3f clockwiseDirection = relativeReferenceAxis.rotateAxis(clockwiseAngle, relativeRotationAxis.x(), relativeRotationAxis.y(), relativeRotationAxis.z(), temp);
			Vector3fc clockwisePoint = clockwiseDirection.mul(radius).add(lineStart);
			lineDrawHelper.draw(lineStart, clockwisePoint, CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, modelViewProjection);
		}
	}

	/// Hinge joints are drawn as circles aligned to the hinge rotation axis,
	/// with an optional reference axis within the plane of the circle if required.
	private void drawGlobalHingeConstraints(FabrikBone3f bone, FabrikJoint3f.GlobalHinge globalHinge, float lineWidth, Matrix4fc modelViewProjection) {
		float boneLength = bone.length();
		float radius = boneLength * RADIUS_FACTOR;
		Vector3fc lineStart = bone.getStartLocation();
		Vector3fc rotationAxis = globalHinge.getRotationAxis();

		// draw the circle describing the hinge rotation axis
		circleDrawHelper.draw(lineStart, rotationAxis, radius, GLOBAL_HINGE_COLOUR, lineWidth, modelViewProjection);

		if (globalHinge.isConstrained()) {
			Vector3fc referenceAxis = globalHinge.getReferenceAxis();

			float anticlockwiseAngle = globalHinge.getAntiClockwiseConstraintAngle();
			float clockwiseAngle = -globalHinge.getClockwiseConstraintAngle();

			Vector3f tmp = new Vector3f();

			lineDrawHelper.draw(lineStart, referenceAxis.mul(boneLength * RADIUS_FACTOR, tmp).add(lineStart), REFERENCE_AXIS_COLOUR, lineWidth, modelViewProjection);

			Vector3f anticlockwiseDirection = referenceAxis.rotateAxis(anticlockwiseAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), tmp);
			Vector3fc anticlockwisePoint = anticlockwiseDirection.mul(radius).add(lineStart);
			lineDrawHelper.draw(lineStart, anticlockwisePoint, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, modelViewProjection);

			Vector3f clockwiseDirection = referenceAxis.rotateAxis(clockwiseAngle, rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), tmp);
			Vector3fc clockwisePoint = clockwiseDirection.mul(radius).add(lineStart);
			lineDrawHelper.draw(lineStart, clockwisePoint, CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, modelViewProjection);
		}
	}

	/// Rotor joints (also known as ball Joints) are drawn as a series of lines forming a cone.
	///
	/// @param referenceDirection As bones are constrained about the direction relative to the previous bone in the chain, this is the direction of the previous bone.
	private void drawRotorConstraints(FabrikBone3f bone, FabrikJoint3f.Rotor rotor, Vector3fc referenceDirection, float lineWidth, Matrix4fc modelViewProjection) {
		if (!rotor.isConstrained()) return;

		Vector3fc lineStart = bone.getStartLocation();
		float boneLength = bone.length();
		float constraintAngle = rotor.getConstraintAngle();

		// The constraint direction is the direction of the previous bone rotated about a perpendicular axis by the constraint angle of this bone
		Vector3f tmp = new Vector3f();
		Vector3fc perpendicularAxis = JomlMath.perpendicularQuick(referenceDirection, tmp);
		Vector3f constraintDirection = referenceDirection.rotateAxis(constraintAngle, perpendicularAxis.x(), perpendicularAxis.y(), perpendicularAxis.z(), tmp).normalize();

		// Draw the lines about the bone (relative to the reference direction)
		Vector3f tmp2 = new Vector3f();
		for (int i = 0; i < NUM_CONE_LINES; i++) {
			Vector3f lineEnd = constraintDirection.mul(boneLength * CONE_LENGTH_FACTOR, tmp2).add(lineStart);
			constraintDirection.rotateAxis(ROTATION_STEP_ANGLE, referenceDirection.x(), referenceDirection.y(), referenceDirection.z()).normalize();
			lineDrawHelper.draw(lineStart, lineEnd, ROTOR_COLOUR, lineWidth, modelViewProjection);
		}

		// Draw the circle at the top of the cone
		float pushDistance = Math.cos(constraintAngle) * boneLength;
		float radius = Math.sin(constraintAngle) * boneLength;
		Vector3f circleCentre = referenceDirection.mul(pushDistance * CONE_LENGTH_FACTOR, tmp).add(lineStart);
		circleDrawHelper.draw(circleCentre, referenceDirection, radius * CONE_LENGTH_FACTOR, ROTOR_COLOUR, lineWidth, modelViewProjection);
	}

	/// Draw the constraints on all bones in a FabrikChain3D object.
	///
	/// @param chain               the chain to use
	/// @param lineWidth           the width of the lines pixels, must be between `1.0f and 32.0f` inclusive
	/// @param modelViewProjection the ModelViewProjection matrix to use
	public void draw(FabrikChain3f chain, float lineWidth, Matrix4fc modelViewProjection) {
		int boneCount = chain.getBoneCount();

		if (boneCount <= 0) return;

		FabrikBone3f baseBone = chain.getBaseBone();

		switch (baseBone.getJoint()) {
			case FabrikJoint3f.GlobalRotor globalRotor -> drawRotorConstraints(baseBone, globalRotor, chain.getBaseboneConstraint(), lineWidth, modelViewProjection);
			case FabrikJoint3f.GlobalHinge globalHinge -> drawGlobalHingeConstraints(baseBone, globalHinge, lineWidth, modelViewProjection); //chain.getBaseboneConstraintUV()
			case FabrikJoint3f.LocalRotor localRotor -> {
				// If the structure hasn't been solved yet, then we won't have a relative basebone constraint
				if (chain.getBaseboneRelativeConstraint().length() > 0f) {
					drawRotorConstraints(baseBone, localRotor, chain.getBaseboneRelativeConstraint(), lineWidth, modelViewProjection);
				}
			}
			case FabrikJoint3f.LocalHinge localHinge -> {
				if (chain.getBaseboneRelativeConstraint().length() > 0f) {
					Quaternionf relativeOrientation = new Quaternionf().rotationTo(FabrikWorld.FORWARDS, chain.getBaseboneRelativeConstraint());
					drawLocalHingeConstraints(baseBone, localHinge, relativeOrientation, lineWidth, modelViewProjection);
				}
			}
			default -> throw new IllegalStateException("Unexpected value: " + chain.getBaseBone().getJoint());
		}

		for (int i = 1; i < boneCount; i++) {
			FabrikBone3f bone = chain.getBone(i);
			FabrikBone3f prevBone = chain.getBone(i - 1);
			switch (bone.getJoint()) {
				case FabrikJoint3f.GlobalRotor globalRotor -> drawRotorConstraints(bone, globalRotor, prevBone.getDirection(), lineWidth, modelViewProjection);
				case FabrikJoint3f.LocalRotor localRotor -> drawRotorConstraints(bone, localRotor, prevBone.getDirection(), lineWidth, modelViewProjection);
				case FabrikJoint3f.GlobalHinge globalHinge -> drawGlobalHingeConstraints(bone, globalHinge, lineWidth, modelViewProjection);
				case FabrikJoint3f.LocalHinge localHinge -> drawLocalHingeConstraints(bone, localHinge, prevBone.getOrientation(), lineWidth, modelViewProjection);
				default -> throw new IllegalStateException("Unexpected value: " + bone.getJoint());
			}
		}
	}

	/// Draw the constraints on all bones in a FabrikChain3D object using the default line width.
	///
	/// @param chain               the chain to use
	/// @param modelViewProjection the ModelViewProjection matrix to use
	public void draw(FabrikChain3f chain, Matrix4fc modelViewProjection) {
		draw(chain, 1f, modelViewProjection);
	}

	/// Draw the constraints on all chains and all bones in each chain of a FabrikStructure3D object.
	///
	/// Line widths may commonly be between 1.0f and 32.0f, values outside of this range may result in unspecified behaviour.
	///
	/// @param structure           the structure to use
	/// @param lineWidth           the width of the lines pixels, must be between `1.0f and 32.0f` inclusive
	/// @param modelViewProjection the ModelViewProjection matrix to use
	public void draw(FabrikStructure3f structure, float lineWidth, Matrix4fc modelViewProjection) {
		for (FabrikChain3f chain : structure.getChains()) {
			draw(chain, lineWidth, modelViewProjection);
		}
	}

	/// Draw the constraints on all chains and all bones in each chain of a FabrikStructure3D object using the default line width.
	///
	/// @param structure           the structure to use
	/// @param modelViewProjection the ModelViewProjection matrix to use
	public void draw(FabrikStructure3f structure, Matrix4fc modelViewProjection) {
		draw(structure, 1f, modelViewProjection);
	}

}
