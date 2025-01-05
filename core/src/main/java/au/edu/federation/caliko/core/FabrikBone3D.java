package au.edu.federation.caliko.core;

import au.edu.federation.caliko.math.Vec3f;
import au.edu.federation.caliko.utils.Colour4f;
import au.edu.federation.caliko.utils.Utils;

import java.io.Serial;
import java.io.Serializable;

/**
 * A class to represent a FabrikBone3D object.
 * <p>
 * A FabrikBone3D consists of a start location, an end location and a FabrikJoint3D which can constrain
 * the rotation of the bone with regard to a previous bone in an IK chain either as a ball joint or as
 * a hinge joint constrained about a local or global axis.
 *
 * @version 0.3.3 - 19/06/2019
 * @see FabrikJoint3D
 */
public class FabrikBone3D implements FabrikBone<Vec3f, FabrikJoint3D>, Serializable {

	@Serial
	private static final long serialVersionUID = 2L;

	/**
	 * If this chain is connected to a bone in another chain, should this chain connect to the start or the end of that bone?
	 * <p>
	 * The default is to connect to the end of the specified bone.
	 * <p>
	 * This property can be set via the {#link #setBoneConnectionPoint(BoneConnectionPoint)} method, or when attaching this chain
	 * to another chain via the {@link FabrikStructure3D#connectChain(FabrikChain3D, int, int, BoneConnectionPoint)} method.
	 */
	private BoneConnectionPoint boneConnectionPoint = BoneConnectionPoint.END;

	/**
	 * mJoint	The joint attached to this FabrikBone3D.
	 * <p>
	 * Each bone has a single FabrikJoint3D which controls the angle to which the bone is
	 * constrained with regard to the previous (i.e. earlier / closer to the base) bone in its chain.
	 * <p>
	 * By default, a joint is not constrained (that is, it is free to rotate up to 180
	 * degrees in a clockwise or anticlockwise direction), however a joint may be
	 * constrained by specifying constraint angles via the
	 * {@link #setBallJointConstraintDegs(float)}, {@link #setHingeJointClockwiseConstraintDegs(float)},
	 * and {@link #setHingeJointAnticlockwiseConstraintDegs(float)} methods.
	 * <p>
	 * You might think that surely a bone has two joints, one at the beginning and one at
	 * the end - however, consider an empty chain to which you add a single bone: It has
	 * a joint at its start, around which the bone may rotate (and which it may optionally
	 * be constrained).
	 * <p>
	 * When a second bone is added to the chain, the joint at the start of this second bone
	 * controls the rotational constraints with regard to the first ('base') bone, and so on.
	 * <p>
	 * During the forward (tip-to-base) pass of the FABRIK algorithm, the end effector is
	 * unconstrained and snapped to the target. As we then work from tip-to-base each
	 * previous bone is constrained by the joint in the outer bone until we reach the base,
	 * at which point, if we have a fixed base location, then we snap the base bone start
	 * location to it, or if we do not have a fixed base location we project the new start
	 * location along the reverse direction of the bone by its length.
	 * <p>
	 * During the backward (base-to-tip) pass, each bone is constrained by the joint angles
	 * relative to the bone before it, ensuring that all constraints are enforced.
	 */
	private final FabrikJoint3D joint = new FabrikJoint3D();

	/**
	 * mStartLocation	The start location of this FabrikBone3D object.
	 * <p>
	 * The start location of a bone may only be set through a constructor or via an 'addBone'
	 * method provided by the {@link FabrikChain3D} class.
	 */
	private final Vec3f startLocation = new Vec3f();

	/**
	 * mEndLocation	The end location of this FabrikBone3D object.
	 * <p>
	 * The end location of a bone may only be set through a constructor or indirectly via an
	 * 'addBone' method provided by the {@link FabrikChain3D} class.
	 */
	private final Vec3f endLocation = new Vec3f();

	/**
	 * mName	The name of this FabrikBone3D object.
	 * <p>
	 * It is not necessary to use this property, but it is provided to allow for easy identification
	 * of a bone, such as when used in a map such as {@code Map<String, FabrikBone3D>}.
	 * <p>
	 * The maximum allowable length of the name String is 100 characters - names exceeding this length
	 * will be truncated.
	 *
	 * @see #setName(String)
	 * @see #FabrikBone3D(Vec3f, Vec3f, String)
	 * @see #FabrikBone3D(Vec3f, Vec3f, float, String)
	 */
	private String name;

	/**
	 * The length of this bone from its start location to its end location. This is is calculated
	 * in the constructor and remains constant for the lifetime of the object.
	 */
	private float length;

	/**
	 * The colour used to draw the bone as specified by a {@link Colour4f au.edu.federation.utils.Colour4f.class} object.
	 * <p>
	 * The default colour to draw a bone is white at full opacity i.e. Colour4f(1.0f, 1.0f, 1.0f, 1.0f).
	 */
	private final Colour4f colour = new Colour4f();

	/**
	 * Default constructor
	 */
	FabrikBone3D() {
	}

	/**
	 * Create a new FabrikBone3D from a start and end location as provided by a pair of Vec3fs.
	 * <p>
	 * The {@link #length} property is calculated and set from the provided locations. All other properties
	 * are set to their default values.
	 * <p>
	 * Instantiating a FabrikBone3D with the exact same start and end location, and hence a length of zero,
	 * will result in an IllegalArgumentException being thrown.
	 *
	 * @param    startLocation    The start location of this bone.
	 * @param    endLocation        The end location of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f endLocation) {
		this.startLocation.set(startLocation);
		this.endLocation.set(endLocation);

		// Set the length of the bone - if the length is not a positive value then an InvalidArgumentException is thrown
		setLength(Vec3f.distanceBetween(startLocation, endLocation));
	}

	/**
	 * Create a new FabrikBone3D from a start and end location and a String.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f)} constructor.
	 *
	 * @param    startLocation    The start location of this bone.
	 * @param    endLocation        The end location of this bone.
	 * @param    name            The name of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f endLocation, String name) {
		// Call the start/end location constructor - which also sets the length of the bone
		this(startLocation, endLocation);
		setName(name);
	}

	/**
	 * Create a new FabrikBone3D from a start location, a direction unit vector and a length.
	 * <p>
	 * The end location of the bone is calculated as the start location plus the direction unit
	 * vector multiplied by the length (which must be a positive value). All other properties
	 * are set to their default values.	 *
	 * <p>
	 * If this constructor is provided with a direction unit vector of magnitude zero, or with a
	 * length less than or equal to zero then an {@link IllegalArgumentException} is thrown.
	 *
	 * @param    startLocation    The start location of this bone.
	 * @param    directionUV        The direction unit vector of this bone.
	 * @param    length            The length of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length) {
		// Sanity checking
		setLength(length); // Throws IAE if < zero

		if (directionUV.length() <= 0.0f) {
			throw new IllegalArgumentException("Direction cannot be a zero vector");
		}

		// Set the length, start and end locations
		setLength(length);
		this.startLocation.set(startLocation);
		endLocation.set(this.startLocation.plus(directionUV.normalised().times(length)));
	}

	/**
	 * Create a named FabrikBone3D from a start location, a direction unit vector, a bone length and a name.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f, float)} constructor.
	 * <p>
	 * If the provided length argument is zero or if the direction is a zero vector then an IllegalArgumentException is thrown.
	 *
	 * @param    startLocation    The start location of this bone.
	 * @param    directionUV        The direction unit vector of this bone.
	 * @param    length            The length of this bone.
	 * @param    name            The name of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, String name) {
		this(startLocation, directionUV, length);
		setName(name);
	}

	/**
	 * Create a new FabrikBone3D from a start location, a direction unit vector, a bone length and a colour.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f, float)} constructor.
	 *
	 * @param    startLocation    The start location of this bone.
	 * @param    directionUV        The direction unit vector of this bone.
	 * @param    length            The length of this bone.
	 * @param    colour            The colour to draw this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, Colour4f colour) {
		this(startLocation, directionUV, length);
		setColour(colour);
	}

	/**
	 * Copy constructor.
	 * <p>
	 * Takes a source FabrikBone3D object and copies all properties into the new FabrikBone3D by value.
	 * Once this is done, there are no shared references between the source and the new object, and they are
	 * exact copies of each other.
	 *
	 * @param    source    The bone to use as the basis for this new bone.
	 */
	public FabrikBone3D(FabrikBone3D source) {
		startLocation.set(source.startLocation);
		endLocation.set(source.endLocation);
		joint.set(source.joint);
		colour.set(source.colour);

		name = source.name;
		length = source.length;
		boneConnectionPoint = source.boneConnectionPoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float length() {
		return length;
	}

	/**
	 * Set the length of the bone.
	 * <p>
	 * This method validates the length argument to ensure that it is greater than zero.
	 * <p>
	 * If the length argument is not a positive value then an {@link IllegalArgumentException} is thrown.
	 *
	 * @param length The value to set on the {@link #length} property.
	 */
	private void setLength(float length) {
		if (length <= 0.0f) {
			throw new IllegalArgumentException("Bone length must be a positive value.");
		}

		this.length = length;
	}

	/**
	 * Return the live (i.e. live calculated) length of this bone from its current start and end locations.
	 *
	 * @return The 'live' calculated distance between the start and end locations of this bone.
	 */
	public float liveLength() {
		return Vec3f.distanceBetween(startLocation, endLocation);
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
	 * @param    bcp    The bone connection point to use (BoneConnectionPoint3.START or BoneConnectionPoint.END).
	 */
	public void setBoneConnectionPoint(BoneConnectionPoint bcp) {
		boneConnectionPoint = bcp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f getStartLocation() {
		return startLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartLocation(Vec3f location) {
		startLocation.set(location);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f getEndLocation() {
		return endLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEndLocation(Vec3f location) {
		endLocation.set(location);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FabrikJoint3D getJoint() {
		return joint;
	}

	/**
	 * Set the FabrikJoint3D of this bone to match the properties of the provided FabrikJoint3D argument.
	 *
	 * @param joint The joint to use as the source for all joint properties on this bone.
	 */
	public void setJoint(FabrikJoint3D joint) {
		this.joint.set(joint);
	}

	/**
	 * Return the clockwise constraint angle of this bone's hinge joint in degrees.
	 *
	 * @return The clockwise constraint angle of this bone's hinge joint in degrees.
	 */
	public float getHingeJointClockwiseConstraintDegs() {
		return joint.getHingeClockwiseConstraintDegs();
	}

	/**
	 * Set the clockwise constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>
	 * If a constraint angle outside of this range is provided, then an IllegalArgumentException is
	 * thrown.
	 *
	 * @param angleDegs The relative clockwise constraint angle specified in degrees.
	 */
	public void setHingeJointClockwiseConstraintDegs(float angleDegs) {
		joint.setHingeJointClockwiseConstraintDegs(angleDegs);
	}

	/**
	 * Return the anticlockwise constraint angle of this bone's hinge joint in degrees.
	 *
	 * @return The anticlockwise constraint angle of this bone's hinge joint in degrees.
	 */
	public float getHingeJointAnticlockwiseConstraintDegs() {
		return joint.getHingeAnticlockwiseConstraintDegs();
	}

	/**
	 * Set the anticlockwise constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>
	 * If a constraint angle outside of this range is provided, then an {@link IllegalArgumentException}
	 * is thrown.
	 *
	 * @param angleDegs The relative anticlockwise constraint angle specified in degrees.
	 */
	public void setHingeJointAnticlockwiseConstraintDegs(float angleDegs) {
		joint.setHingeJointAnticlockwiseConstraintDegs(angleDegs);
	}

	/**
	 * Return the anticlockwise constraint angle of this bone's joint in degrees.
	 *
	 * @return The anticlockwise constraint angle of this bone's joint in degrees.
	 */
	public float getBallJointConstraintDegs() {
		return joint.getBallJointConstraintDegs();
	}

	/**
	 * Set the rotor constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>
	 * If a constraint angle outside of this range is provided, then an {@link IllegalArgumentException}
	 * is thrown.
	 *
	 * @param angleDegs The angle in degrees relative to the previous bone which this bone is constrained to.
	 */
	public void setBallJointConstraintDegs(float angleDegs) {
		if (angleDegs < 0.0f || angleDegs > 180.0f) {
			throw new IllegalArgumentException("Rotor constraints for ball joints must be in the range 0.0f to 180.0f degrees inclusive.");
		}

		joint.setBallJointConstraintDegs(angleDegs);
	}

	/**
	 * Get the direction unit vector between the start location and end location of this bone.
	 * <p>
	 * If the opposite (i.e. end to start) location is required then you can simply negate the provided direction.
	 *
	 * @return The direction unit vector of this bone.
	 * @see        Vec3f#negate()
	 * @see        Vec3f#negated()
	 */
	public Vec3f getDirectionUV() {
		return Vec3f.getDirectionUV(startLocation, endLocation);
	}

	/**
	 * Get the global pitch of this bone with regard to the X-Axis. Pitch returned is in the range -179.9f to 180.0f.
	 *
	 * @return The global pitch of this bone in degrees.
	 */
	public float getGlobalPitchDegs() {
		return Vec3f.getDirectionUV(startLocation, endLocation).getGlobalPitchDegs();
	}

	/**
	 * Get the global yaw of this bone with regard to the Y-Axis. Yaw returned is in the range -179.9f to 180.0f.
	 *
	 * @return The global yaw of this bone in degrees.
	 */
	public float getGlobalYawDegs() {
		return Vec3f.getDirectionUV(startLocation, endLocation).getGlobalYawDegs();
	}

	/**
	 * Get the name of this bone.
	 * <p>
	 * If the bone has not been specifically named through a constructor or by using the {@link #setName(String)} method,
	 * then the name will be the default of "UnnamedFabrikBone3D".
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this bone, capped to 100 characters if required.
	 *
	 * @param    name    The name to set.
	 */
	public void setName(String name) {
		this.name = Utils.getValidatedName(name);
	}

	/**
	 * Return the colour of this bone.
	 *
	 * @return The colour to draw this bone, as stored in the mColour property.
	 */
	public Colour4f getColour() {
		return colour;
	}

	/**
	 * Set the colour used to draw this bone.
	 *
	 * @param colour The colour (used to draw this bone) to set on the mColour property.
	 */
	public void setColour(Colour4f colour) {
		this.colour.set(colour);
	}

	/**
	 * Return a concise, human readable description of this FabrikBone3D as a String.
	 */
	@Override
	public String toString() {
		return "Start joint location : " + startLocation + Utils.NEW_LINE +
				"End   joint location : " + endLocation + Utils.NEW_LINE +
				"Bone length          : " + length + Utils.NEW_LINE +
				"Colour               : " + colour + Utils.NEW_LINE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boneConnectionPoint == null) ? 0 : boneConnectionPoint.hashCode());
		result = prime * result + colour.hashCode();
		result = prime * result + endLocation.hashCode();
		result = prime * result + joint.hashCode();
		result = prime * result + Float.floatToIntBits(length);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + startLocation.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FabrikBone3D other = (FabrikBone3D) obj;
		if (boneConnectionPoint != other.boneConnectionPoint) {
			return false;
		}
		if (!colour.equals(other.colour)) {
			return false;
		}
		if (!endLocation.equals(other.endLocation)) {
			return false;
		}
		if (!joint.equals(other.joint)) {
			return false;
		}
		if (Float.floatToIntBits(length) != Float.floatToIntBits(other.length)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return startLocation.equals(other.startLocation);
	}

}
