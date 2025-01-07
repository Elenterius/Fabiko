package com.github.elenterius.fabiko.core;

import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface FabrikJoint3f {

	Space getSpace();

	FabrikJoint3f copy();

	abstract class Hinge implements FabrikJoint3f {

		/// The minimum valid constraint angle for a hinge is 0 radians
		/// - this will fully constrain the bone.
		public static final float MIN_CONSTRAINT_ANGLE = 0f;

		/// The maximum valid constraint angle for a hinge is [pi][Math#PI_f] radians (180 degrees)
		/// - this will allow the bone complete freedom to rotate.
		public static final float MAX_CONSTRAINT_ANGLE = Math.PI_f;

		/// The axis ({@link Vector3f#normalize() unit} vector) about which the hinged joint may rotate
		private final Vector3f rotationAxis = new Vector3f(FabrikWorld.X_AXIS);

		/// The axis ({@link Vector3f#normalize() unit} vector) used as a point of reference for rotation
		///
		/// (This is **NOT** the axis about which the hinge rotates)
		private final Vector3f referenceAxis = new Vector3f(FabrikWorld.X_AXIS);

		/// The angle (specified in radians) up to which the hinge is allowed to rotate
		/// in a clockwise direction with regard to its present orientation about its hinge axis.
		///
		/// The valid range of this property is [0.0f][#MIN_CONSTRAINT_ANGLE] to [pi][#MAX_CONSTRAINT_ANGLE]
		/// - [0.0f][#MIN_CONSTRAINT_ANGLE] means that the joint cannot rotate in a clockwise direction at all
		/// - [pi][#MAX_CONSTRAINT_ANGLE] means that the joint is unconstrained with regard to anti-clockwise rotation
		///
		/// The default is [pi][#MAX_CONSTRAINT_ANGLE] (no constraint).
		private float clockwiseConstraintAngle;

		/// The angle (specified in radians) up to which the hinge is allowed to rotate
		/// in an anti-clockwise direction with regard to its present orientation.
		///
		/// The valid range of this property is [0.0f][#MIN_CONSTRAINT_ANGLE] to [pi][#MAX_CONSTRAINT_ANGLE]
		/// - [0.0f][#MIN_CONSTRAINT_ANGLE] means that the joint cannot rotate in an anti-clockwise direction at all
		/// - [pi][#MAX_CONSTRAINT_ANGLE] means that the joint is unconstrained with regard to anti-clockwise rotation
		///
		/// The default is [pi][#MAX_CONSTRAINT_ANGLE] (no constraint).
		private float antiClockwiseConstraintAngle;

		private boolean isConstrained;

		protected Hinge() {
			clockwiseConstraintAngle = MAX_CONSTRAINT_ANGLE;
			antiClockwiseConstraintAngle = MAX_CONSTRAINT_ANGLE;
			isConstrained = false;
		}

		protected Hinge(float clockwiseConstraintAngle, float antiClockwiseConstraintAngle) {
			setConstraintAngles(clockwiseConstraintAngle, antiClockwiseConstraintAngle);
		}

		protected Hinge(Hinge hinge) {
			rotationAxis.set(hinge.rotationAxis);
			referenceAxis.set(hinge.referenceAxis);
			clockwiseConstraintAngle = hinge.clockwiseConstraintAngle;
			antiClockwiseConstraintAngle = hinge.antiClockwiseConstraintAngle;
			isConstrained = hinge.isConstrained;
		}

		public static void validateConstraintAngles(float clockwiseConstraintAngle, float antiClockwiseConstraintAngle) {
			validateClockwiseConstraintAngle(clockwiseConstraintAngle);
			validateAntiClockwiseConstraintAngle(antiClockwiseConstraintAngle);
		}

		public static void validateClockwiseConstraintAngle(float clockwiseConstraintAngle) {
			if (clockwiseConstraintAngle < MIN_CONSTRAINT_ANGLE || clockwiseConstraintAngle > MAX_CONSTRAINT_ANGLE) {
				throw new IllegalArgumentException("Clockwise constraint angle must be between 0.0f..PI_f inclusive.");
			}
		}

		public static void validateAntiClockwiseConstraintAngle(float antiClockwiseConstraintAngle) {
			if (antiClockwiseConstraintAngle < MIN_CONSTRAINT_ANGLE || antiClockwiseConstraintAngle > MAX_CONSTRAINT_ANGLE) {
				throw new IllegalArgumentException("AntiClockwise constraint angle must be between 0.0f..PI_f inclusive.");
			}
		}

		protected static boolean isConstrained(float clockwiseConstraintAngle, float antiClockwiseConstraintAngle) {
			return !JomlMath.floatsAreEqual(clockwiseConstraintAngle, MAX_CONSTRAINT_ANGLE, 0.001f) || !JomlMath.floatsAreEqual(antiClockwiseConstraintAngle, MAX_CONSTRAINT_ANGLE, 0.001f);
		}

		public boolean isConstrained() {
			return isConstrained;
		}

		public Vector3fc getRotationAxis() {
			return rotationAxis;
		}

		public Vector3fc getReferenceAxis() {
			return referenceAxis;
		}

		public float getClockwiseConstraintAngle() {
			return clockwiseConstraintAngle;
		}

		public float getAntiClockwiseConstraintAngle() {
			return antiClockwiseConstraintAngle;
		}

		public void setClockwiseConstraintAngle(float clockwiseConstraintAngle) {
			validateClockwiseConstraintAngle(clockwiseConstraintAngle);
			this.clockwiseConstraintAngle = clockwiseConstraintAngle;
			isConstrained = isConstrained(clockwiseConstraintAngle, antiClockwiseConstraintAngle);
		}

		public void setAntiClockwiseConstraintAngle(float antiClockwiseConstraintAngle) {
			validateAntiClockwiseConstraintAngle(antiClockwiseConstraintAngle);
			this.antiClockwiseConstraintAngle = antiClockwiseConstraintAngle;
			isConstrained = isConstrained(clockwiseConstraintAngle, antiClockwiseConstraintAngle);
		}

		public void setConstraintAngles(float clockwiseConstraintAngle, float antiClockwiseConstraintAngle) {
			validateConstraintAngles(clockwiseConstraintAngle, antiClockwiseConstraintAngle);
			this.clockwiseConstraintAngle = clockwiseConstraintAngle;
			this.antiClockwiseConstraintAngle = antiClockwiseConstraintAngle;
			isConstrained = isConstrained(clockwiseConstraintAngle, antiClockwiseConstraintAngle);
		}

		public void setRotationAxis(Vector3fc rotationAxis) {
			this.rotationAxis.set(rotationAxis);
		}

		public void setReferenceAxis(Vector3fc referenceAxis) {
			this.referenceAxis.set(referenceAxis);
		}

	}

	/// Hinge constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
	class LocalHinge extends Hinge {
		public LocalHinge() {
		}

		public LocalHinge(LocalHinge hinge) {
			super(hinge);
		}

		@Override
		public Space getSpace() {
			return Space.LOCAL;
		}

		@Override
		public LocalHinge copy() {
			return new LocalHinge(this);
		}
	}

	/// World-space hinge constraint
	class GlobalHinge extends Hinge {
		public GlobalHinge() {
		}

		public GlobalHinge(GlobalHinge hinge) {
			super(hinge);
		}

		@Override
		public Space getSpace() {
			return Space.GLOBAL;
		}

		@Override
		public GlobalHinge copy() {
			return new GlobalHinge(this);
		}
	}

	/// ball joint
	abstract class Rotor implements FabrikJoint3f {
		public static final float MIN_CONSTRAINT_ANGLE = 0f;
		public static final float MAX_CONSTRAINT_ANGLE = Math.PI_f;

		/// rotor constraint angle in radians
		private float constraintAngle;
		private boolean isConstrained;

		protected Rotor() {
			constraintAngle = MAX_CONSTRAINT_ANGLE;
			isConstrained = false;
		}

		protected Rotor(float constraintAngle) {
			validateConstraintAngle(constraintAngle);
			this.constraintAngle = constraintAngle;
			isConstrained = isConstrained(constraintAngle);
		}

		public static void validateConstraintAngle(float constraintAngle) {
			if (constraintAngle < MIN_CONSTRAINT_ANGLE || constraintAngle > MAX_CONSTRAINT_ANGLE) {
				throw new IllegalArgumentException("Constraint angle must be between 0.0f..PI_f inclusive.");
			}
		}

		protected static boolean isConstrained(float constraintAngle) {
			return !JomlMath.floatsAreEqual(constraintAngle, MAX_CONSTRAINT_ANGLE, 0.001f);
		}

		public float getConstraintAngle() {
			return constraintAngle;
		}

		public void setConstraintAngle(float constraintAngle) {
			validateConstraintAngle(constraintAngle);
			this.constraintAngle = constraintAngle;
			isConstrained = isConstrained(constraintAngle);
		}

		public boolean isConstrained() {
			return isConstrained;
		}

	}

	/// Rotor constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
	class LocalRotor extends Rotor {
		public LocalRotor() {
		}

		public LocalRotor(LocalRotor rotor) {
			super(rotor.getConstraintAngle());
		}

		@Override
		public Space getSpace() {
			return Space.LOCAL;
		}

		@Override
		public LocalRotor copy() {
			return new LocalRotor(this);
		}
	}

	/// World-space rotor constraint
	class GlobalRotor extends Rotor {
		public GlobalRotor() {
		}

		public GlobalRotor(GlobalRotor rotor) {
			super(rotor.getConstraintAngle());
		}

		@Override
		public Space getSpace() {
			return Space.GLOBAL;
		}

		@Override
		public GlobalRotor copy() {
			return new GlobalRotor(this);
		}
	}

}

