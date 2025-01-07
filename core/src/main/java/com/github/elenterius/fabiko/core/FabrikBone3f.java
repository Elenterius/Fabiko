package com.github.elenterius.fabiko.core;

import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class FabrikBone3f {

	private final Vector3f startLocation;
	private final Vector3f endLocation;
	private final Vector3f direction;
	private final Quaternionf orientation;

	private float length;
	private FabrikJoint3f joint;
	private BoneConnectionPoint boneConnectionPoint = BoneConnectionPoint.END;

	public FabrikBone3f(Vector3fc startLocation, Vector3fc direction, float length) {
		this(startLocation, direction, length, new FabrikJoint3f.LocalRotor());
	}

	public FabrikBone3f(Vector3fc startLocation, Vector3fc direction, float length, FabrikJoint3f joint) {
		if (!JomlMath.isUnitVector(direction)) {
			throw new IllegalArgumentException("Direction is not a unit vector");
		}

		this.startLocation = new Vector3f(startLocation);
		endLocation = direction.mul(length, new Vector3f()).add(startLocation);

		this.length = length;

		this.direction = new Vector3f(direction);
		orientation = new Quaternionf().rotationTo(FabrikWorld.FORWARDS, direction);

		this.joint = joint;
	}

	public FabrikBone3f(Vector3fc startLocation, Vector3fc endLocation) {
		this(startLocation, endLocation, new FabrikJoint3f.LocalRotor());
	}

	public FabrikBone3f(Vector3fc startLocation, Vector3fc endLocation, FabrikJoint3f joint) {
		this.startLocation = new Vector3f(startLocation);
		this.endLocation = new Vector3f(endLocation);

		length = startLocation.distance(endLocation);

		direction = endLocation.sub(startLocation, new Vector3f()).normalize();
		orientation = new Quaternionf().rotationTo(FabrikWorld.FORWARDS, direction);

		this.joint = joint;
	}

	public FabrikBone3f(FabrikBone3f bone) {
		startLocation = new Vector3f(bone.startLocation);
		endLocation = new Vector3f(bone.endLocation);
		direction = new Vector3f(bone.direction);
		orientation = new Quaternionf(bone.orientation);
		boneConnectionPoint = bone.boneConnectionPoint;
		length = bone.length;
		joint = bone.joint.copy();
	}

	public void setFrom(FabrikBone3f bone) {
		startLocation.set(bone.startLocation);
		endLocation.set(bone.endLocation);
		direction.set(bone.direction);
		orientation.set(bone.orientation);
		length = bone.length;
		joint = bone.joint.copy();
	}

	public Vector3fc getStartLocation() {
		return startLocation;
	}

	public Vector3f getStartLocationRaw() {
		return startLocation;
	}

	void setStartLocation(Vector3fc location) {
		startLocation.set(location);
		direction.set(endLocation).sub(startLocation).normalize();
		orientation.rotationTo(FabrikWorld.FORWARDS, direction);
	}

	public Vector3fc getEndLocation() {
		return endLocation;
	}

	public Vector3f getEndLocationUnsafe() {
		return endLocation;
	}

	void setEndLocation(Vector3fc location) {
		endLocation.set(location);
		direction.set(endLocation).sub(startLocation).normalize();
		orientation.rotationTo(FabrikWorld.FORWARDS, direction);
	}

	public float length() {
		return length;
	}

	/// @return `unit` quaternion
	public Quaternionfc getOrientation() {
		return orientation;
	}

	/// @return `unit` vector
	public Vector3fc getDirection() {
		return direction;
		//return endLocation.sub(startLocation, new Vector3f()).normalize();
		//return orientation.transform(FabrikWorld.AWAY_FROM_SCREEN, new Vector3f());
	}

	public FabrikJoint3f getJoint() {
		return joint;
	}

	public void setJoint(FabrikJoint3f joint) {
		this.joint = joint;
	}

	/**
	 * Return the bone connection point for THIS bone, which will be either BoneConnectionPoint.START or BoneConnectionPoint.END.
	 * <p>
	 * This connection point property controls whether, when THIS bone connects to another bone in another chain, it does so at
	 * the start or the end of the bone we connect to.
	 *
	 * @return The bone connection point for this bone.
	 */
	public BoneConnectionPoint getBoneConnectionPoint() {
		return boneConnectionPoint;
	}

	/**
	 * Specify the bone connection point of this bone.
	 * <p>
	 * This connection point property controls whether, when THIS bone connects to another bone in another chain, it does so at
	 * the start or the end of the bone we connect to.
	 * <p>
	 * The default is BoneConnectionPoint3D.END.
	 *
	 * @param connectionPoint The bone connection point to use (BoneConnectionPoint3.START or BoneConnectionPoint.END).
	 */
	public void setBoneConnectionPoint(BoneConnectionPoint connectionPoint) {
		boneConnectionPoint = connectionPoint;
	}

	public Vector3f getBoneConnectionPointLocation() {
		return boneConnectionPoint == BoneConnectionPoint.START ? startLocation : endLocation;
	}

}
