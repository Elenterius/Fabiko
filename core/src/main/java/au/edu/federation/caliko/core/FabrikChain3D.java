package au.edu.federation.caliko.core;

import au.edu.federation.caliko.core.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.caliko.core.FabrikJoint3D.JointType;
import au.edu.federation.caliko.math.Mat3f;
import au.edu.federation.caliko.math.Vec3f;
import au.edu.federation.caliko.utils.Colour4f;
import au.edu.federation.caliko.utils.MathUtil;
import au.edu.federation.caliko.utils.Utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to represent a 3D Inverse Kinematics (IK) chain that can be solved for a given target using the FABRIK algorithm.
 * <p>
 * A FabrikChain3D consists primarily of a list of connected {@link FabrikBone3D} objects, and a number of parameters which
 * keep track of settings related to how we go about solving the IK chain.
 *
 * @author Al Lansley
 * @version 0.5.3 - 19/06/2019
 */
public class FabrikChain3D implements FabrikChain<FabrikBone3D, Vec3f, FabrikJoint3D, BaseboneConstraintType3D>, Serializable {

	@Serial
	private static final long serialVersionUID = 2L;
	/**
	 * The last target location for the end effector of this IK chain.
	 * <p>
	 * The target location can be updated via the {@link #solveForTarget(Vec3f)} or {@link #solveForTarget(float, float, float)} methods, which in turn
	 * will call the solveIK(Vec3f) method to attempt to solve the IK chain, resulting in an updated chain configuration.
	 * <p>
	 * The default is Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
	 */
	private final Vec3f lastTargetLocation = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	/**
	 * The previous location of the start joint of the first bone added to the chain.
	 * <p>
	 * We keep track of the previous base location in order to use it to determine if the current base location and
	 * previous base location are the same, i.e. has the base location moved between the last run to this run? If
	 * the base location has moved, then we MUST solve the IK chain for this new base location - even if the target
	 * location has remained the same between runs.
	 * <p>
	 * The default is Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE).
	 * <p>
	 * See {@link #setFixedBaseMode(boolean)}
	 */
	private final Vec3f lastBaseLocation = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	/**
	 * An embedded target location which can be used to solve this chain.
	 * <p>
	 * Embedded target locations allow structures to be solved for multiple targets (one per chain in the structure)
	 * rather than all chains being solved for the same target. To use embedded targets, the mUseEmbeddedTargets flag
	 * must be true (which is not the default) - this flag can be set via a call to setEmbeddedTargetMode(true).
	 * <p>
	 * See (@link #setEmbeddedTargetMode(boolean) }
	 */
	private final Vec3f embeddedTarget = new Vec3f();
	/**
	 * The core of a FabrikChain3D is a list of FabrikBone3D objects. It is this chain that we attempt to solve for a specified
	 * target location via the solveForTarget method(s).
	 */
	private List<FabrikBone3D> ikChain = new ArrayList<>();
	/**
	 * The name of this FabrikChain3D object.
	 * <p>
	 * Although entirely optional, it may be used to uniquely identify a specific FabrikChain3D in an an array/list/map
	 * or such of FabrikChain3D objects.
	 *
	 * @see #setName
	 * @see #getName
	 */
	private String name;
	/**
	 * The distance threshold we must meet in order to consider this FabrikChain3D to be successfully solved for distance.
	 * <p>
	 * When we solve a chain so that the distance between the end effector and target is less than or equal to the distance
	 * threshold, then we consider the chain to be solved and will dynamically abort any further attempts to solve the chain.
	 * <p>
	 * The default solve distance threshold is <strong>1.0f</strong>.
	 * <p>
	 * The minimum valid distance threshold is 0.0f, however a slightly higher value should be used to avoid forcing the IK
	 * chain solve process to run repeatedly when an <strong>acceptable</strong> (but not necessarily <em>perfect</em>)
	 * solution is found. Setting a very low solve distance threshold may result in significantly increased processor usage and
	 * hence increased processing time to solve a given IK chain.
	 * <p>
	 * Although this property is the main criteria used to establish whether we have solved a given IK chain, it works
	 * in combination with the {@link #maxIterationAttempts} and {@link #minIterationChange} fields to improve the
	 * performance of the algorithm in situations where we may not be able to solve a given IK chain. Such situations may arise
	 * when bones in the chain are highly constrained, or when the target is further away than the length of a chain which has
	 * a fixed base location.
	 * <p>
	 * See {@link #setSolveDistanceThreshold(float) }
	 * See {@link #maxIterationAttempts }
	 * See {@link #minIterationChange }
	 */
	private float solveDistanceThreshold = 1f;
	/**
	 * Specifies the maximum number of attempts that will be performed in order to solve the IK chain.
	 * If we have not solved the chain to within the solve distance threshold after this many attempts then we accept the best
	 * solution we have best on solve distance to target.
	 * <p>
	 * The default is 20 iteration attempts.
	 */
	private int maxIterationAttempts = 20;
	/**
	 * Specifies the minimum distance improvement which must be made per solve attempt in order for us to believe it
	 * worthwhile to continue making attempts to solve the IK chain. If this iteration change is not exceeded then we abort any further solve
	 * attempts and accept the best solution we have based on solve distance to target.
	 * <p>
	 * The default is 0.01f.
	 */
	private float minIterationChange = 0.01f;
	/**
	 * The chainLength is the combined length of all bones in this FabrikChain3D object.
	 * <p>
	 * When a FabrikBone3D is added or removed from the chain using the addBone, addConsecutiveBone or removeBone methods, then
	 * the chainLength is updated to reflect this.
	 * <p>
	 * See {@link #addBone(FabrikBone3D)}
	 * See {@link #addConsecutiveBone(FabrikBone3D)}
	 * See {@link #removeBone(int)}
	 */
	private float chainLength;
	/**
	 * The location of the start joint of the first bone in the IK chain.
	 * <p>
	 * By default, FabrikChain3D objects are created with a fixed base location, that is the start joint
	 * of the first bone in the chain is not moved during the solving process. A user may still move this
	 * base location by calling setFixedBaseMode(boolean) and the FABRIK algorithm will then
	 * honour this new location as the 'fixed' base location.
	 * <p>
	 * The default is Vec3f(0.f, 0.0f).
	 * <p>
	 * See {@link #setFixedBaseMode(boolean)}
	 */
	private Vec3f fixedBaseLocation = new Vec3f();
	/**
	 * Whether this FabrikChain3D has a fixed (i.e. immovable) base location.
	 * <p>
	 * By default, the location of the start joint of the first bone added to the IK chain is considered fixed. This
	 * 'anchors' the base of the chain in place. Optionally, a user may toggle this behaviour by calling
	 * {@link #setFixedBaseMode(boolean)} to enable or disable locking the basebone to a fixed starting location.
	 * <p>
	 * See {@link #setFixedBaseMode(boolean)}
	 */
	private boolean fixedBaseMode = true;
	/**
	 * Each chain has a BaseboneConstraintType3D - this may be either:
	 * <p>
	 * - {@link BaseboneConstraintType3D#NONE}, No constraint - base bone may rotate freely<br>
	 * - GLOBAL_ROTOR, World-space rotor (i.e. ball joint) constraint<br>
	 * - LOCAL_ROTOR,  Rotor constraint which is relative to the coordinate space of the connected bone<br>
	 * - GLOBAL_HINGE, World-space hinge constraint, or<br>
	 * - LOCAL_HINGE   Hinge constraint which is relative to the coordinate space of the connected bone
	 */
	private BaseboneConstraintType3D baseboneConstraintType = BaseboneConstraintType3D.NONE;
	/**
	 * The direction around which we should constrain the base bone.
	 * <p>
	 * To ensure correct operation, the provided Vec3f is normalised inside the {@link #setBaseboneConstraintUV(Vec3f)} method. Passing a Vec3f
	 * with a magnitude of zero will result in the constraint not being set.
	 */
	private Vec3f baseboneConstraintUV = new Vec3f();
	/**
	 * The base bone direction constraint in the coordinate space of the bone in another chain
	 * that this chain is connected to.
	 */
	private Vec3f baseboneRelativeConstraintUV = new Vec3f();
	/**
	 * The base bone reference constraint in the coordinate space of the bone in another chain
	 * that this chain is connected to.
	 */
	private Vec3f baseboneRelativeReferenceConstraintUV = new Vec3f();
	/**
	 * The width in pixels of the line used to draw constraints for this chain.
	 * <p>
	 * The valid range is 1.0f to 32.0f inclusive.
	 * <p>
	 * The default is 2.0f pixels.
	 */
	private float constraintLineWidth = 2.0f;
	/**
	 * mCurrentSolveDistance	The current distance between the end effector and the target location for this IK chain.
	 * <p>
	 * The current solve distance is updated when an attempt is made to solve the IK chain as triggered by a call to the
	 * {@link #solveForTarget(Vec3f)} or (@link #solveForTarget(float, float, float) methods.
	 */
	private float currentSolveDistance = Float.MAX_VALUE;
	/**
	 * The zero-indexed number of the chain this chain is connected to in a FabrikStructure3D.
	 * <p>
	 * If the value is -1 then it's not connected to another bone or chain.
	 * <p>
	 * The default is -1.
	 */
	private int connectedChainNumber = -1;
	/**
	 * The zero-indexed number of the bone that this chain is connected to, if it's connected to another chain at all.
	 * <p>
	 * If the value is -1 then it's not connected to another bone or chain.
	 * <p>
	 * The default is -1.
	 */
	private int connectedBoneNumber = -1;
	/**
	 * Whether to use the mEmbeddedTarget location when solving this chain.
	 * <p>
	 * This flag may be toggled by calling the setEmbeddedTargetMode(true) on the chain.
	 * <p>
	 * The default is false.
	 * <p>
	 * See {@link #setEmbeddedTargetMode(boolean) }
	 */
	private boolean useEmbeddedTarget = false;

	public FabrikChain3D() {
	}

	/**
	 * Copy constructor.
	 *
	 * @param source The chain to duplicate.
	 */
	public FabrikChain3D(FabrikChain3D source) {
		// Force copy by value
		ikChain = source.cloneIkChain();

		fixedBaseLocation.set(source.getBaseLocation());
		lastTargetLocation.set(source.lastTargetLocation);
		lastBaseLocation.set(source.lastBaseLocation);
		embeddedTarget.set(source.embeddedTarget);

		// Copy the base bone constraint UV if there is one to copy
		if (source.baseboneConstraintType != BaseboneConstraintType3D.NONE) {
			baseboneConstraintUV.set(source.baseboneConstraintUV);
			baseboneRelativeConstraintUV.set(source.baseboneRelativeConstraintUV);
		}

		// Native copy by value for primitive members
		chainLength = source.chainLength;
		currentSolveDistance = source.currentSolveDistance;
		connectedChainNumber = source.connectedChainNumber;
		connectedBoneNumber = source.connectedBoneNumber;
		baseboneConstraintType = source.baseboneConstraintType;
		name = source.name;
		constraintLineWidth = source.constraintLineWidth;
		useEmbeddedTarget = source.useEmbeddedTarget;
	}

	/**
	 * Naming constructor.
	 *
	 * @param name The name to set for this chain.
	 */
	public FabrikChain3D(String name) {
		this.name = name;
	}

	/**
	 * Add a bone to the end of this IK chain of this FabrikChain3D object.
	 * <p>
	 * This chain's {@link #chainLength} property is updated to take into account the length of the
	 * new bone added to the chain.
	 * <p>
	 * In addition, if the bone being added is the very first bone, then this chain's
	 * {@link #fixedBaseLocation} property is set from the start joint location of the bone.
	 *
	 * @param bone The FabrikBone3D object to add to this FabrikChain3D.
	 * @see #chainLength
	 * @see #fixedBaseLocation
	 */
	@Override
	public void addBone(FabrikBone3D bone) {
		// Add the new bone to the end of the ArrayList of bones
		ikChain.add(bone);

		// If this is the basebone...
		if (ikChain.size() == 1) {
			// ...then keep a copy of the fixed start location...
			fixedBaseLocation.set(bone.getStartLocation());

			// ...and set the basebone constraint UV to be around the initial bone direction
			baseboneConstraintUV = bone.getDirectionUV();
		}

		updateChainLength();
	}

	/**
	 * Add a bone to the end of this IK chain given the direction unit vector and length of the new bone to add.
	 * <p>
	 * The bone added does not have any rotational constraints enforced, and will be drawn with a default colour
	 * of white at full opacity.
	 * <p>
	 * This method can only be used when the IK chain contains a base bone, as without it, we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a base bone then a {@link RuntimeException}
	 * is thrown.
	 * <p>
	 * If this method is provided with a direction unit vector of zero, or a bone length of zero then an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param directionUV The initial direction of the new bone
	 * @param length      The length of the new bone
	 */
	@Override
	public void addConsecutiveBone(Vec3f directionUV, float length) {
		addConsecutiveBone(directionUV, length, new Colour4f());
	}

	/**
	 * Add a consecutive bone to the end of this IK chain given the direction unit vector and length of the new bone to add.
	 * <p>
	 * The bone added does not have any rotational constraints enforced, and will be drawn with a default colour
	 * of white at full opacity.
	 * <p>
	 * This method can only be used when the IK chain contains a base bone, as without it, we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a base bone then a {@link RuntimeException}
	 * is thrown.
	 * <p>
	 * If this method is provided with a direction unit vector of zero, or a bone length of zero then an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param directionUV The initial direction of the new bone
	 * @param length      The length of the new bone
	 * @param colour      The colour with which to draw the bone
	 */
	public void addConsecutiveBone(Vec3f directionUV, float length, Colour4f colour) {
		// Validate the direction unit vector - throws an IllegalArgumentException if it has a magnitude of zero
		MathUtil.validateDirectionUV(directionUV);

		// Validate the length of the bone - throws an IllegalArgumentException if it is not a positive value
		MathUtil.validateLength(length);

		if (ikChain.isEmpty()) {
			// Attempting to add a relative bone when there is no basebone for it to be relative to?
			throw new RuntimeException("You cannot add the basebone as a consecutive bone as it does not provide a start location. Use the addBone() method instead.");
		}

		// Get the end location of the last bone, which will be used as the start location of the new bone
		Vec3f prevBoneEnd = ikChain.getLast().getEndLocation();

		// Add a bone to the end of this IK chain
		// Note: We use a normalised version of the bone direction
		addBone(new FabrikBone3D(prevBoneEnd, directionUV.normalised(), length, colour));
	}

	/**
	 * Add a pre-created consecutive bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it, we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a {@link RuntimeException}
	 * is thrown.
	 * <p>
	 * If this method is provided with a direction unit vector of zero, or a bone length of zero then an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param bone The bone to add to the end of the chain.
	 */
	public void addConsecutiveBone(FabrikBone3D bone) {
		// Validate the direction unit vector - throws an IllegalArgumentException if it has a magnitude of zero
		Vec3f dir = bone.getDirectionUV();
		MathUtil.validateDirectionUV(dir);

		// Validate the length of the bone - throws an IllegalArgumentException if it is not a positive value
		float len = bone.liveLength();
		MathUtil.validateLength(len);

		if (ikChain.isEmpty()) {
			// Attempting to add a relative bone when there is no base bone for it to be relative to?
			throw new RuntimeException("You cannot add the base bone to a chain using this method as it does not provide a start location.");
		}

		// Get the end location of the last bone, which will be used as the start location of the new bone
		Vec3f prevBoneEnd = ikChain.getLast().getEndLocation();

		bone.setStartLocation(prevBoneEnd);
		bone.setEndLocation(prevBoneEnd.plus(dir.times(len)));

		// Add a bone to the end of this IK chain
		addBone(bone);
	}

	/**
	 * Add a consecutive hinge constrained bone to the end of this chain. The bone may rotate freely about the hinge axis.
	 * <p>
	 * The bone will be drawn with a default colour of white.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with a direction unit vector of zero, then an IllegalArgumentException is thrown.
	 * If the joint type requested is not JointType.LOCAL_HINGE or JointType.GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * If this method is provided with a hinge rotation axis unit vector of zero, then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV       The initial direction of the new bone.
	 * @param length            The length of the new bone.
	 * @param jointType         The type of hinge joint to be used - either JointType.LOCAL or JointType.GLOBAL.
	 * @param hingeRotationAxis The axis about which the hinge joint freely rotates.
	 */
	public void addConsecutiveFreelyRotatingHingedBone(Vec3f directionUV, float length, JointType jointType, Vec3f hingeRotationAxis) {
		// Because we aren't constraining this bone to a reference axis within the hinge rotation axis we don't care about the hinge constraint
		// reference axis (7th param) so we'll just generate an axis perpendicular to the hinge rotation axis and use that.
		addConsecutiveHingedBone(directionUV, length, jointType, hingeRotationAxis, 180f, 180f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis), new Colour4f());
	}

	/**
	 * Add a consecutive hinge constrained bone to the end of this chain. The bone may rotate freely about the hinge axis.
	 * <p>
	 * The bone will be drawn with a default colour of white.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with a direction unit vector of zero, then an IllegalArgumentException is thrown.
	 * If the joint type requested is not JointType.LOCAL_HINGE or JointType.GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * If this method is provided with a hinge rotation axis unit vector of zero, then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV       The initial direction of the new bone.
	 * @param length            The length of the new bone.
	 * @param jointType         The type of hinge joint to be used - either JointType.LOCAL or JointType.GLOBAL.
	 * @param hingeRotationAxis The axis about which the hinge joint freely rotates.
	 * @param colour            The colour to draw the bone.
	 */
	public void addConsecutiveFreelyRotatingHingedBone(Vec3f directionUV, float length, JointType jointType, Vec3f hingeRotationAxis, Colour4f colour) {
		// Because we aren't constraining this bone to a reference axis within the hinge rotation axis we don't care about the hinge constraint
		// reference axis (7th param) so we'll just generate an axis perpendicular to the hinge rotation axis and use that.
		addConsecutiveHingedBone(directionUV, length, jointType, hingeRotationAxis, 180f, 180f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis), colour);
	}

	/**
	 * Add a consecutive hinge constrained bone to the end of this IK chain.
	 * <p>
	 * The hinge type may be a global hinge where the rotation axis is specified in world-space, or
	 * a local hinge, where the rotation axis is relative to the previous bone in the chain.
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV        The initial direction of the new bone.
	 * @param length             The length of the new bone.
	 * @param jointType          The joint type of the new bone.
	 * @param hingeRotationAxis  The axis about which the hinge rotates.
	 * @param clockwiseDegs      The clockwise constraint angle in degrees.
	 * @param anticlockwiseDegs  The anticlockwise constraint angle in degrees.
	 * @param hingeReferenceAxis The axis about which any clockwise/anticlockwise rotation constraints are enforced.
	 * @param colour             The colour to draw the bone.
	 */
	public void addConsecutiveHingedBone(Vec3f directionUV, float length, JointType jointType, Vec3f hingeRotationAxis, float clockwiseDegs, float anticlockwiseDegs, Vec3f hingeReferenceAxis, Colour4f colour) {
		// Validate the direction and rotation axis unit vectors, and the length of the bone.
		MathUtil.validateDirectionUV(directionUV);
		MathUtil.validateDirectionUV(hingeRotationAxis);
		MathUtil.validateLength(length);

		// Cannot add a consectuive bone of any kind if the there is no basebone
		if (ikChain.isEmpty()) {
			throw new RuntimeException("You must add a basebone before adding a consectutive bone.");
		}

		// Normalise the direction and hinge rotation axis
		directionUV.normalise();
		hingeRotationAxis.normalise();

		// Get the end location of the last bone, which will be used as the start location of the new bone
		Vec3f prevBoneEnd = ikChain.getLast().getEndLocation();

		// Create a bone and set the draw colour...
		FabrikBone3D bone = new FabrikBone3D(prevBoneEnd, directionUV, length);
		bone.setColour(colour);

		// ...then create and set up a joint which we'll apply to that bone.
		FabrikJoint3D joint = new FabrikJoint3D();
		switch (jointType) {
			case GLOBAL_HINGE:
				joint.setAsGlobalHinge(hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeReferenceAxis);
				break;
			case LOCAL_HINGE:
				joint.setAsLocalHinge(hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeReferenceAxis);
				break;
			default:
				throw new IllegalArgumentException("Hinge joint types may be only JointType.GLOBAL_HINGE or JointType.LOCAL_HINGE.");
		}

		// Set the joint we just set up on the new bone we just created
		bone.setJoint(joint);

		// Finally, add the bone to this chain
		addBone(bone);
	}

	/**
	 * Add a consecutive hinge constrained bone to the end of this IK chain.
	 * <p>
	 * The hinge type may be a global hinge where the rotation axis is specified in world-space, or
	 * a local hinge, where the rotation axis is relative to the previous bone in the chain.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 *
	 * @param directionUV                  The initial direction of the new bone.
	 * @param length                       The length of the new bone.
	 * @param type                         The joint type of the new bone.
	 * @param hingeRotationAxis            The axis about which the hinge rotates.
	 * @param clockwiseDegs                The clockwise constraint angle in degrees.
	 * @param anticlockwiseDegs            The anticlockwise constraint angle in degrees.
	 * @param hingeConstraintReferenceAxis The reference axis about which any clockwise/anticlockwise rotation constraints are enforced.
	 */
	public void addConsecutiveHingedBone(Vec3f directionUV, float length, JointType type, Vec3f hingeRotationAxis, float clockwiseDegs, float anticlockwiseDegs, Vec3f hingeConstraintReferenceAxis) {
		addConsecutiveHingedBone(directionUV, length, type, hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeConstraintReferenceAxis, new Colour4f());
	}

	/**
	 * Add a consecutive rotor (i.e. ball joint) constrained bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 *
	 * @param boneDirectionUV     The initial direction unit vector of the new bone.
	 * @param boneLength          The length of the new bone.
	 * @param constraintAngleDegs The rotor constraint angle of the new bone.
	 * @param colour              The colour to draw the bone.
	 */
	public void addConsecutiveRotorConstrainedBone(Vec3f boneDirectionUV, float boneLength, float constraintAngleDegs, Colour4f colour) {
		// Validate the bone direction and length and that we have a basebone
		MathUtil.validateDirectionUV(boneDirectionUV);
		MathUtil.validateLength(boneLength);
		if (ikChain.isEmpty()) {
			throw new RuntimeException("Add a basebone before attempting to add consectuive bones.");
		}

		// Create the bone starting at the end of the previous bone, set its direction, constraint angle and colour
		// then add it to the chain. Note: The default joint type of a new FabrikBone3D is JointType.BALL.
		FabrikBone3D bone = new FabrikBone3D(ikChain.getLast().getEndLocation(), boneDirectionUV.normalise(), boneLength, colour);
		bone.setBallJointConstraintDegs(constraintAngleDegs);
		addBone(bone);
	}

	/**
	 * Add a consecutive rotor (i.e. ball joint) constrained bone to the end of this IK chain.
	 * <p>
	 * The bone will be drawn in white at full opacity by default. This method can only be used when the IK chain contains
	 * a basebone, as without it we do not have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 *
	 * @param boneDirectionUV     The initial direction unit vector of the new bone.
	 * @param boneLength          The length of the new bone.
	 * @param constraintAngleDegs The rotor constraint angle for of the new bone.
	 */
	public void addConsecutiveRotorConstrainedBone(Vec3f boneDirectionUV, float boneLength, float constraintAngleDegs) {
		addConsecutiveRotorConstrainedBone(boneDirectionUV, boneLength, constraintAngleDegs, new Colour4f());
	}

	/**
	 * Return the basebone relative unit vector of this chain.
	 * <p>
	 * This direction is updated by the FabrikStructure3D when this chain is connected to another chain. There is
	 * no other possible way of doing it as we have no knowledge of other chains, but the structure does, allowing
	 * us to calculate this relative constraint UV.
	 *
	 * @return The basebone relative constraint UV as updated (on solve) by the structure containing this chain.
	 */
	@Override
	public Vec3f getBaseboneRelativeConstraintUV() {
		return baseboneRelativeConstraintUV;
	}

	/**
	 * Set the relative basebone constraint UV - this direction should be relative to the coordinate space of the basebone.
	 * <p>
	 * This function is deliberately made package-private as it should not be used by the end user - instead, the
	 * FabrikStructure3D.solveForTarget() method will update this mBaseboneRelativeConstraintUV property FOR USE BY this
	 * chain as required.
	 * <p>
	 * The reason for this is that this chain on its own cannot calculate the relative constraint
	 * direction, because it relies on direction of the connected / 'host' bone in the chain that this chain is connected
	 * to - only we have no knowledge of that other chain! But, the FabrikStructure3D DOES have knowledge of that other
	 * chain, and is hence able to calculate and update this relative basebone constraint direction for us.
	 **/
	void setBaseboneRelativeConstraintUV(Vec3f constraintUV) {
		baseboneRelativeConstraintUV = constraintUV;
	}

	/**
	 * Return the basebone constraint type of this chain.
	 *
	 * @return The basebone constraint type of this chain.
	 */
	@Override
	public BaseboneConstraintType3D getBaseboneConstraintType() {
		return baseboneConstraintType;
	}

	/**
	 * Method to set the line width (in pixels) with which to draw any constraint lines.
	 * <p>
	 * Valid values are 1.0f to 32.0f inclusive, although the OpenGL standard specifies that only line widths of 1.0f are guaranteed to work.
	 * Values outside of this range will result in an IllegalArgumentException being thrown.
	 *
	 * @param lineWidth The width of the line used to draw constraint lines.
	 */
	public void setConstraintLineWidth(float lineWidth) {
		MathUtil.validateLineWidth(lineWidth);
		constraintLineWidth = lineWidth;
	}

	/**
	 * Get the directional constraint of the basebone.
	 * <p>
	 * If the basebone is not constrained then a RuntimeException is thrown. If you wish to check whether the
	 * basebone of this IK chain is constrained you may use the {@link #getBaseboneConstraintType()} method.
	 *
	 * @return The global directional constraint unit vector of the basebone of this IK chain.
	 */
	@Override
	public Vec3f getBaseboneConstraintUV() {
		if (baseboneConstraintType == BaseboneConstraintType3D.NONE) {
			throw new RuntimeException("Cannot return the basebone constraint when the basebone constraint type is NONE.");
		}

		return baseboneConstraintUV;
	}

	/**
	 * Set a directional constraint for the basebone.
	 * <p>
	 * This method constrains the <strong>basebone</strong> (<em>only</em>) to a global direction unit vector.
	 * <p>
	 * Attempting to set the basebone constraint when the bone has a basebone constraint type of NONE or providing
	 * a constraint vector of zero will result will result in an IllegalArgumentException being thrown.
	 *
	 * @param constraintUV The direction unit vector to constrain the basebone to.
	 * @see FabrikJoint3D#setBallJointConstraintDegs(float angleDegs)
	 * @see FabrikJoint3D#setHingeJointClockwiseConstraintDegs(float)
	 * @see FabrikJoint3D#setHingeJointAnticlockwiseConstraintDegs(float)
	 */
	@Override
	public void setBaseboneConstraintUV(Vec3f constraintUV) {
		if (baseboneConstraintType == BaseboneConstraintType3D.NONE) {
			throw new IllegalArgumentException("Specify the basebone constraint type with setBaseboneConstraintTypeCannot specify a basebone constraint when the current constraint type is BaseboneConstraint.NONE.");
		}

		// Validate the constraint direction unit vector
		MathUtil.validateDirectionUV(constraintUV);

		// All good? Then normalise the constraint direction and set it
		constraintUV.normalise();
		baseboneConstraintUV.set(constraintUV);
	}

	/**
	 * Return the base location of the IK chain.
	 * <p>
	 * Regardless of how many bones are contained in the chain, the base location is always the start location of the
	 * first bone in the chain.
	 * <p>
	 * This method does not return the mBaseLocation property of this chain because the start location of the basebone
	 * may be more up-to-date due to a moving 'fixed' location.
	 *
	 * @return The location of the start joint of the first bone in this chain.
	 */
	@Override
	public Vec3f getBaseLocation() {
		return ikChain.getFirst().getStartLocation();
	}

	/**
	 * Method used to move the base location of a chain relative to its connection point.
	 * <p>
	 * The assignment is made by reference so that this base location and the location where
	 * we attach to the other chain are the same Vec3f object.
	 * <p>
	 * Note: If this chain is attached to another chain then this 'fixed' base location will be updated
	 * as and when the connection point in the chain we are attached to moves.
	 *
	 * @param baseLocation The fixed base location for this chain.
	 */
	@Override
	public void setBaseLocation(Vec3f baseLocation) {
		fixedBaseLocation = baseLocation;
	}

	/**
	 * Return a bone by its zero-indexed location in the IK chain.
	 *
	 * @param boneNumber The number of the bone to return from the Vector of FabrikBone3D objects.
	 * @return The specified bone.
	 */
	@Override
	public FabrikBone3D getBone(int boneNumber) {
		return ikChain.get(boneNumber);
	}

	/**
	 * Return the List%lt;FabrikBone3D%gt; which comprises the actual IK chain of this FabrikChain3D object.
	 *
	 * @return The List%lt;FabrikBone3D%gt; which comprises the actual IK chain of this FabrikChain3D object.
	 */
	@Override
	public List<FabrikBone3D> getChain() {
		return ikChain;
	}

	/**
	 * Return the current length of the IK chain.
	 * <p>
	 * This method does not dynamically re-calculate the length of the chain - it merely returns the previously
	 * calculated chain length, which gets updated each time a bone is added or removed from the chain. However,
	 * as the chain length is updated whenever necessary this should be fine.
	 * <p>
	 * If you need a calculated-on-the-fly value for the chain length, then use the getLiveChainLength() method.
	 *
	 * @return The pre-calculated length of the IK chain as stored in the mChainLength property.
	 */
	@Override
	public float getChainLength() {
		return chainLength;
	}

	/**
	 * Return the index of the bone in another chain that this this chain is connected to.
	 * <p>
	 * Returns -1 (default) if this chain is not connected to another chain.
	 *
	 * @return The zero-indexed number of the bone we are connected to in the chain we are connected to.
	 */
	@Override
	public int getConnectedBoneNumber() {
		return connectedBoneNumber;
	}

	/**
	 * Return the index of the chain in a FabrikStructure3D that this this chain is connected to.
	 * <p>
	 * Returns -1 (default) if this chain is not connected to another chain.
	 *
	 * @return The zero-index number of the chain we are connected to.
	 */
	@Override
	public int getConnectedChainNumber() {
		return connectedChainNumber;
	}

	/**
	 * Return the location of the end effector in the IK chain.
	 * <p>
	 * Regardless of how many bones are contained in the chain, the end effector is always the end location
	 * of the final bone in the chain.
	 *
	 * @return The location of this chain's end effector.
	 */
	@Override
	public Vec3f getEffectorLocation() {
		return ikChain.getLast().getEndLocation();
	}

	/**
	 * Return whether or not this chain uses an embedded target.
	 * <p>
	 * Embedded target mode may be enabled or disabled using setEmbeddededTargetMode(boolean).
	 *
	 * @return whether or not this chain uses an embedded target.
	 */
	@Override
	public boolean getEmbeddedTargetMode() {
		return useEmbeddedTarget;
	}

	/**
	 * Specify whether we should use the embedded target location when solving the IK chain.
	 *
	 * @param value Whether we should use the embedded target location when solving the IK chain.
	 */
	@Override
	public void setEmbeddedTargetMode(boolean value) {
		useEmbeddedTarget = value;
	}

	/**
	 * Return the embedded target location.
	 *
	 * @return the embedded target location.
	 */
	@Override
	public Vec3f getEmbeddedTarget() {
		return embeddedTarget;
	}

	/**
	 * Return the target of the last solve attempt.
	 * <p>
	 * The target location and the effector location are not necessarily at the same location unless the chain has been solved
	 * for distance, and even then they are still likely to be <i>similar</i> rather than <b>identical</b> values.
	 *
	 * @return The target location of the last solve attempt.
	 */
	@Override
	public Vec3f getLastTargetLocation() {
		return lastTargetLocation;
	}

	/**
	 * Return the live calculated length of the chain.
	 * <p>
	 * Typically, the getChainLength() can be called which returns the length of the chain as updated /
	 * recalculated when a bone is added or removed from the chain (which is significantly faster as it
	 * doesn't require recalculation), but sometimes it may be useful to get the definitive most
	 * up-to-date chain length so you can check if operations being performed have altered the chain
	 * length - hence this method.
	 *
	 * @return The 'live' (i.e. calculated from scratch) length of the chain.
	 */
	public float getLiveChainLength() {
		float length = 0.0f;
		for (FabrikBone3D bone : ikChain) {
			length += bone.liveLength();
		}
		return length;
	}

	/**
	 * Return the name of this IK chain.
	 *
	 * @return The name of this IK chain.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this chain, capped to 100 characters if required.
	 *
	 * @param name The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = Utils.getValidatedName(name);
	}

	/**
	 * Return the number of bones in this IK chain.
	 *
	 * @return The number of bones in this IK chain.
	 */
	@Override
	public int getNumBones() {
		return ikChain.size();
	}

	/**
	 * Remove a bone from this IK chain by its zero-indexed location in the chain.
	 * <p>
	 * This chain's {@link #chainLength} property is updated to take into account the new chain length.
	 * <p>
	 * If the bone number to be removed does not exist in the chain then an IllegalArgumentException is thrown.
	 *
	 * @param boneNumber The zero-indexed bone to remove from this IK chain.
	 */
	@Override
	public void removeBone(int boneNumber) {
		if (boneNumber >= ikChain.size()) {
			throw new IllegalArgumentException("Bone " + boneNumber + " does not exist to be removed from the chain. Bones are zero indexed.");
		}

		ikChain.remove(boneNumber);
		updateChainLength();
	}

	/**
	 * Return the relative basebone reference constraint unit vector.
	 *
	 * @return The relative basebone reference constraint unit vector.
	 */
	public Vec3f getBaseboneRelativeReferenceConstraintUV() {
		return baseboneRelativeReferenceConstraintUV;
	}

	/**
	 * Set the relative basebone reference constraint UV - this direction should be relative to the coordinate space of the basebone.
	 * <p>
	 * This function is deliberately made package-private as it should not be used by the end user - instead, the
	 * FabrikStructure3D.solveForTarget() method will update this mBaseboneRelativeConstraintUV property FOR USE BY this
	 * chain as required.
	 * <p>
	 * This property is required when we have a LOCAL_HINGE basebone constraint with reference axes - we must maintain the
	 * hinge's own rotation and reference axes, and then the FabrikStructure3D.solveForTarget() method updates the
	 * mBaseboneRelativeConstraintUV and mBaseboneRelativeReferenceConstraintUV as required.
	 **/
	void setBaseboneRelativeReferenceConstraintUV(Vec3f constraintUV) {
		baseboneRelativeReferenceConstraintUV = constraintUV;
	}

	/**
	 * Set this chain to have a rotor basebone constraint.
	 * <p>
	 * Depending on whether the constraint type is GLOBAL_ROTOR or LOCAL_ROTOR the constraint will be applied
	 * about global space or about the local coordinate system of a bone in another chain that this chain is
	 * attached to.
	 * <p>
	 * The angle provided should be between the range of 0.0f (completely constrained) to 180.0f (completely free to
	 * rotate). Values outside of this range will be clamped to the relevant minimum or maximum.
	 * <p>
	 * If this chain does not contain a basebone then a RuntimeException is thrown.
	 * If the constraint axis is a zero vector or the rotor type is not GLOBAL_ROTOR or LOCAL_ROTOR then then an
	 * IllegalArgumentException is thrown.
	 *
	 * @param rotorType      The type of constraint to apply, this may be GLOBAL_ROTOR or LOCAL_ROTOR.
	 * @param constraintAxis The axis about which the rotor applies.
	 * @param angleDegs      The angle about the constraint axis to limit movement in degrees.
	 */
	public void setRotorBaseboneConstraint(BaseboneConstraintType3D rotorType, Vec3f constraintAxis, float angleDegs) {
		// Sanity checking
		if (ikChain.isEmpty()) {
			throw new RuntimeException("Chain must contain a basebone before we can specify the basebone constraint type.");
		}
		if (constraintAxis.length() <= 0.0f) {
			throw new IllegalArgumentException("Constraint axis cannot be zero.");
		}

		if (angleDegs < 0.0f) {
			angleDegs = 0.0f;
		}
		if (angleDegs > 180.0f) {
			angleDegs = 180.0f;
		}
		if (!(rotorType == BaseboneConstraintType3D.GLOBAL_ROTOR || rotorType == BaseboneConstraintType3D.LOCAL_ROTOR)) {
			throw new IllegalArgumentException("The only valid rotor types for this method are GLOBAL_ROTOR and LOCAL_ROTOR.");
		}

		// Set the constraint type, axis and angle
		baseboneConstraintType = rotorType;
		baseboneConstraintUV = constraintAxis.normalised();
		baseboneRelativeConstraintUV.set(baseboneConstraintUV);
		getBone(0).getJoint().setAsBallJoint(angleDegs);
	}

	/**
	 * Set this chain to have a hinged basebone constraint.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation or reference axes are zero vectors then an IllegalArgumentException is thrown.
	 * If the hinge reference axis does not lie in the plane of the hinge rotation axis (that is, they are not perpendicular)
	 * then an IllegalArgumentException is thrown.
	 *
	 * @param hingeType          The type of constraint to apply, this may be GLOBAL_HINGE or LOCAL_HINGE.
	 * @param hingeRotationAxis  The axis about which the global hinge rotates.
	 * @param cwConstraintDegs   The clockwise constraint angle about the hinge reference axis in degrees.
	 * @param acwConstraintDegs  The clockwise constraint angle about the hinge reference axis in degrees.
	 * @param hingeReferenceAxis The axis (perpendicular to the hinge rotation axis) about which the constraint angles apply.
	 */
	public void setHingeBaseboneConstraint(BaseboneConstraintType3D hingeType, Vec3f hingeRotationAxis, float cwConstraintDegs, float acwConstraintDegs, Vec3f hingeReferenceAxis) {
		// Sanity checking
		if (ikChain.isEmpty()) {
			throw new RuntimeException("Chain must contain a basebone before we can specify the basebone constraint type.");
		}
		if (hingeRotationAxis.length() <= 0.0f) {
			throw new IllegalArgumentException("Hinge rotation axis cannot be zero.");
		}
		if (hingeReferenceAxis.length() <= 0.0f) {
			throw new IllegalArgumentException("Hinge reference axis cannot be zero.");
		}
		if (!(Vec3f.perpendicular(hingeRotationAxis, hingeReferenceAxis))) {
			throw new IllegalArgumentException("The hinge reference axis must be in the plane of the hinge rotation axis, that is, they must be perpendicular.");
		}
		if (!(hingeType == BaseboneConstraintType3D.GLOBAL_HINGE || hingeType == BaseboneConstraintType3D.LOCAL_HINGE)) {
			throw new IllegalArgumentException("The only valid hinge types for this method are GLOBAL_HINGE and LOCAL_HINGE.");
		}

		// Set the constraint type, axis and angle
		baseboneConstraintType = hingeType;
		baseboneConstraintUV.set(hingeRotationAxis.normalised());
		baseboneRelativeConstraintUV.set(baseboneConstraintUV);

		FabrikJoint3D hinge = new FabrikJoint3D();

		if (hingeType == BaseboneConstraintType3D.GLOBAL_HINGE) {
			hinge.setHinge(JointType.GLOBAL_HINGE, hingeRotationAxis, cwConstraintDegs, acwConstraintDegs, hingeReferenceAxis);
		}
		else {
			hinge.setHinge(JointType.LOCAL_HINGE, hingeRotationAxis, cwConstraintDegs, acwConstraintDegs, hingeReferenceAxis);
		}
		getBone(0).setJoint(hinge);
	}

	/**
	 * Set this chain to have a freely rotating globally hinged basebone.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are automatically set to 180 degrees and the hinge reference axis
	 * is generated to be any vector perpendicular to the hinge rotation axis.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation axis are zero vectors then an IllegalArgumentException is thrown.
	 *
	 * @param hingeRotationAxis The world-space axis about which the global hinge rotates.
	 */
	public void setFreelyRotatingGlobalHingedBasebone(Vec3f hingeRotationAxis) {
		setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, hingeRotationAxis, 180.0f, 180.0f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis));
	}

	/**
	 * Set this chain to have a freely rotating globally hinged basebone.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are automatically set to 180 degrees and the hinge reference axis
	 * is generated to be any vector perpendicular to the hinge rotation axis.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation axis are zero vectors then an IllegalArgumentException is thrown.
	 *
	 * @param hingeRotationAxis The world-space axis about which the global hinge rotates.
	 */
	public void setFreelyRotatingLocalHingedBasebone(Vec3f hingeRotationAxis) {
		setHingeBaseboneConstraint(BaseboneConstraintType3D.LOCAL_HINGE, hingeRotationAxis, 180.0f, 180.0f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis));
	}

	/**
	 * Set this chain to have a locally hinged basebone.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are automatically set to 180 degrees and the hinge reference axis
	 * is generated to be any vector perpendicular to the hinge rotation axis.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation axis are zero vectors then an IllegalArgumentException is thrown.
	 *
	 * @param hingeRotationAxis  The local axis about which the hinge rotates.
	 * @param cwDegs             The clockwise constraint angle in degrees.
	 * @param acwDegs            The anticlockwise constraint angle in degrees.
	 * @param hingeReferenceAxis The local reference axis about which the hinge is constrained.
	 */
	public void setLocalHingedBasebone(Vec3f hingeRotationAxis, float cwDegs, float acwDegs, Vec3f hingeReferenceAxis) {
		setHingeBaseboneConstraint(BaseboneConstraintType3D.LOCAL_HINGE, hingeRotationAxis, cwDegs, acwDegs, hingeReferenceAxis);
	}

	/**
	 * Set this chain to have a globally hinged basebone.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are automatically set to 180 degrees and the hinge reference axis
	 * is generated to be any vector perpendicular to the hinge rotation axis.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation axis are zero vectors then an IllegalArgumentException is thrown.
	 *
	 * @param hingeRotationAxis  The global / world-space axis about which the hinge rotates.
	 * @param cwDegs             The clockwise constraint angle in degrees.
	 * @param acwDegs            The anticlockwise constraint angle in degrees.
	 * @param hingeReferenceAxis The global / world-space reference axis about which the hinge is constrained.
	 */
	public void setGlobalHingedBasebone(Vec3f hingeRotationAxis, float cwDegs, float acwDegs, Vec3f hingeReferenceAxis) {
		setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, hingeRotationAxis, cwDegs, acwDegs, hingeReferenceAxis);
	}

	/**
	 * Connect this chain to the specified bone in the specified chain in the provided structure.
	 * <p>
	 * In order to connect this chain to another chain, both chains must exist within the same structure.
	 * <p>
	 * If the structure does not contain the specified chain or bone then an IllegalArgumentException is thrown.
	 *
	 * @param structure   The structure which contains the chain which contains the bone to connect to.
	 * @param chainNumber The zero-indexed number of the chain in the structure to connect to.
	 * @param boneNumber  The zero-indexed number of the bone in the chain to connect to.
	 */
	public void connectToStructure(FabrikStructure3D structure, int chainNumber, int boneNumber) {
		// Sanity check chain exists
		int numChains = structure.getNumChains();
		if (chainNumber > numChains) {
			throw new IllegalArgumentException("Structure does not contain a chain " + chainNumber + " - it has " + numChains + " chains.");
		}

		// Sanity check bone exists
		int numBones = structure.getChain(chainNumber).getNumBones();
		if (boneNumber > numBones) {
			throw new IllegalArgumentException("Chain does not contain a bone " + boneNumber + " - it has " + numBones + " bones.");
		}

		// All good? Set the connection details
		connectedChainNumber = chainNumber;
		connectedBoneNumber = boneNumber;
	}

	/**
	 * Set the fixed basebone mode for this chain.
	 * <p>
	 * If the basebone is 'fixed' in place, then its start location cannot move. The bone is still allowed to
	 * rotate, with or without constraints.
	 * <p>
	 * Specifying a non-fixed base location while this chain is connected to another chain will result in a
	 * RuntimeException being thrown.
	 * <p>
	 * Fixing the basebone's start location in place and constraining to a global absolute direction are
	 * mutually exclusive. Disabling fixed base mode while the chain's constraint type is
	 * BaseboneConstraintType3D.GLOBAL_ABSOLUTE will result in a RuntimeException being thrown.	 *
	 *
	 * @param value Whether or not to fix the basebone start location in place.
	 */
	@Override
	public void setFixedBaseMode(boolean value) {
		// Enforce that a chain connected to another chain stays in fixed base mode (i.e. it moves with the chain it's connected to instead of independently)
		if (!value && connectedChainNumber != -1) {
			throw new RuntimeException("This chain is connected to another chain so must remain in fixed base mode.");
		}

		// We cannot have a freely moving base location AND constrain the basebone to an absolute direction
		if (baseboneConstraintType == BaseboneConstraintType3D.GLOBAL_ROTOR && !value) {
			throw new RuntimeException("Cannot set a non-fixed base mode when the chain's constraint type is BaseboneConstraintType3D.GLOBAL_ABSOLUTE_ROTOR.");
		}

		// Above conditions met? Set the fixedBaseMode
		fixedBaseMode = value;
	}

	/**
	 * Set the colour of all bones in this chain to the specified colour.
	 *
	 * @param colour The colour to set all bones in this chain.
	 */
	public void setColour(Colour4f colour) {
		for (FabrikBone3D bone : ikChain) {
			bone.setColour(colour);
		}
	}

	/**
	 * Solve this IK chain for the current embedded target location.
	 * <p>
	 * The embedded target location can be updated by calling updateEmbeddedTarget(Vec3f).
	 *
	 * @return The distance between the end effector and the chain's embedded target location for our best solution.
	 */
	@Override
	public float solveForEmbeddedTarget() {
		if (!useEmbeddedTarget) {
			throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true).");
		}

		return solveForTarget(embeddedTarget);
	}

	/**
	 * Method to solve this IK chain for the given target location.
	 * <p>
	 * The end result of running this method is that the IK chain configuration is updated.
	 * <p>
	 * To minimuse CPU usage, this method dynamically aborts if:
	 * - The solve distance (i.e. distance between the end effector and the target) is below the {@link #solveDistanceThreshold},
	 * - A solution incrementally improves on the previous solution by less than the {@link #minIterationChange}, or
	 * - The number of attempts to solve the IK chain exceeds the {@link #maxIterationAttempts}.
	 *
	 * @param targetX The x location of the target
	 * @param targetY The y location of the target
	 * @param targetZ The z location of the target
	 * @return The resulting distance between the end effector and the new target location after solving the IK chain.
	 */
	public float solveForTarget(float targetX, float targetY, float targetZ) {
		return solveForTarget(new Vec3f(targetX, targetY, targetZ));
	}

	/**
	 * Method to solve this IK chain for the given target location.
	 * <p>
	 * The end result of running this method is that the IK chain configuration is updated.
	 * <p>
	 * To minimise CPU usage, this method dynamically aborts if:
	 * - The solve distance (i.e. distance between the end effector and the target) is below the {@link #solveDistanceThreshold},
	 * - A solution incrementally improves on the previous solution by less than the {@link #minIterationChange}, or
	 * - The number of attempts to solve the IK chain exceeds the {@link #maxIterationAttempts}.
	 *
	 * @param newTarget The location of the target for which we will solve this IK chain.
	 * @return The resulting distance between the end effector and the new target location after solving the IK chain.
	 */
	@Override
	public float solveForTarget(Vec3f newTarget) {
		if (ikChain.isEmpty()) {
			throw new RuntimeException("It makes no sense to solve an IK chain with zero bones.");
		}

		// If we have both the same target and base location as the last run then do not solve
		if (lastTargetLocation.approximatelyEquals(newTarget, 0.001f) &&
				lastBaseLocation.approximatelyEquals(getBaseLocation(), 0.001f)) {
			return currentSolveDistance;
		}

		/*
		 * NOTE: We must allow the best solution of THIS run to be used for a new target or base location - we cannot
		 * just use the last solution (even if it's better) - because that solution was for a different target / base
		 * location combination and NOT for the current setup.
		 */

		// Declare a list of bones to use to store our best solution
		List<FabrikBone3D> bestSolution = Collections.emptyList();

		// We start with the best solve distance that can be easily beaten
		float bestSolveDistance = Float.MAX_VALUE;

		// We'll also keep track of the solve distance from the last pass
		float lastPassSolveDistance = Float.MAX_VALUE;

		// Allow up to our iteration limit attempts at solving the chain
		float solveDistance;
		for (int loop = 0; loop < maxIterationAttempts; ++loop) {
			// Solve the chain for this target
			solveDistance = solveIK(newTarget);

			// Did we solve it for distance? If so, update our best distance and best solution, and also
			// update our last pass solve distance. Note: We will ALWAYS beat our last solve distance on the first run.
			if (solveDistance < bestSolveDistance) {
				bestSolveDistance = solveDistance;
				bestSolution = cloneIkChain();

				// If we are happy that this solution meets our distance requirements then we can exit the loop now
				if (solveDistance <= solveDistanceThreshold) {
					break;
				}
			}
			else // Did not solve to our satisfaction? Okay...
			{
				// Did we grind to a halt? If so break out of loop to set the best distance and solution that we have
				if (Math.abs(solveDistance - lastPassSolveDistance) < minIterationChange) {
					//System.out.println("Ground to halt on iteration: " + loop);
					break;
				}
			}

			// Update the last pass solve distance
			lastPassSolveDistance = solveDistance;

		} // End of loop

		// Update our solve distance and chain configuration to the best solution found
		currentSolveDistance = bestSolveDistance;
		ikChain = bestSolution;

		// Update our base and target locations
		lastBaseLocation.set(getBaseLocation());
		lastTargetLocation.set(newTarget);

		return currentSolveDistance;
	}

	/**
	 * Solve the IK chain for the given target using the FABRIK algorithm.
	 * <p>
	 * If this chain does not contain any bones then a RuntimeException is thrown.
	 *
	 * @return The best solve distance found between the end-effector of this chain and the provided target.
	 */
	private float solveIK(Vec3f target) {
		FabrikBone3D bone;
		float boneLength;
		FabrikJoint3D boneJoint;
		JointType boneJointType;

		// ---------- Forward pass from end effector to base -----------

		// Loop over all bones in the chain, from the end effector (numBones-1) back to the base bone (0)
		for (int i = ikChain.size() - 1; i >= 0; i--) {
			bone = ikChain.get(i);
			boneLength = bone.length();
			boneJoint = bone.getJoint();
			boneJointType = boneJoint.getJointType();

			if (i != ikChain.size() - 1) {
				//we are NOT working on the end effector bone

				// Get the outer-to-inner unit vector of this bone
				Vec3f boneDirectionNegated = bone.getDirectionUV().negated();

				// Get the joint type for this bone and handle constraints on thisBoneInnerToOuterUV
				if (boneJointType == JointType.BALL) {
					// Get the outer-to-inner unit vector of the bone further out
					Vec3f prevBoneDirectionNegated = ikChain.get(i + 1).getDirectionUV().negated();

					// Constrain to relative angle between this bone and the outer bone if required
					float angleBetween = Vec3f.getAngleBetweenDegs(prevBoneDirectionNegated, boneDirectionNegated);
					float constraintAngle = boneJoint.getBallJointConstraintDegs();
					if (angleBetween > constraintAngle) {
						boneDirectionNegated = Vec3f.getAngleLimitedUnitVectorDegs(boneDirectionNegated, prevBoneDirectionNegated, constraintAngle);
					}
				}
				else if (boneJointType == JointType.GLOBAL_HINGE) {
					// Project this bone outer-to-inner direction onto the hinge rotation axis
					// Note: The returned vector is normalised.
					boneDirectionNegated = boneDirectionNegated.projectOntoPlane(boneJoint.getHingeRotationAxis());

					// NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.
				}
				else if (boneJointType == JointType.LOCAL_HINGE) {
					Vec3f relativeHingeRotationAxis;
					if (i > 0) {
						// Not a base bone
						// transform the hinge rotation axis into the previous bones frame of reference
						Mat3f m = Mat3f.createRotationMatrix(ikChain.get(i - 1).getDirectionUV());
						relativeHingeRotationAxis = m.times(boneJoint.getHingeRotationAxis()).normalise();
					}
					else {
						//base bone
						//Need to construct matrix from the relative constraint UV.
						relativeHingeRotationAxis = baseboneRelativeConstraintUV;
					}

					// Project this bone's outer-to-inner direction onto the plane described by the relative hinge rotation axis
					boneDirectionNegated = boneDirectionNegated.projectOntoPlane(relativeHingeRotationAxis);

					// NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.
				}

				// At this stage we have a outer-to-inner unit vector for this bone which is within our constraints,
				// so we can set the new inner joint location to be the end joint location of this bone plus the
				// outer-to-inner direction unit vector multiplied by the length of the bone.
				Vec3f newStartLocation = bone.getEndLocation().plus(boneDirectionNegated.times(boneLength));
				bone.setStartLocation(newStartLocation);

				// If we are not working on the basebone, then we also set the end joint location of
				// the previous bone in the chain (i.e. the bone closer to the base) to be the new
				// start joint location of this bone.
				if (i > 0) {
					ikChain.get(i - 1).setEndLocation(newStartLocation);
				}
			}
			else {
				// we ARE working on the End Effector bone...

				// snap the end effector's end location to the target
				bone.setEndLocation(target);

				// Get the UV between the target / end-location (which are now the same) and the start location of this bone
				Vec3f boneDirectionNegated = bone.getDirectionUV().negated();

				switch (boneJointType) {
					case BALL:
						// Ball joints do not get constrained on this forward pass
						break;
					case GLOBAL_HINGE:
						// If the end effector is global hinged then we have to snap to it, then keep that
						// resulting outer-to-inner UV in the plane of the hinge rotation axis
						// Global hinges get constrained to the hinge rotation axis, but not the reference axis within the hinge plane
						boneDirectionNegated = boneDirectionNegated.projectOntoPlane(boneJoint.getHingeRotationAxis());
						break;
					case LOCAL_HINGE:
						// Local hinges get constrained to the hinge rotation axis, but not the reference axis within the hinge plane
						Vec3f relativeHingeRotationAxis;

						if (i > 0) {
							// transform the hinge rotation axis into the previous bones frame of reference
							Mat3f m = Mat3f.createRotationMatrix(ikChain.get(i - 1).getDirectionUV());
							relativeHingeRotationAxis = m.times(boneJoint.getHingeRotationAxis()).normalise();
						}
						else {
							// 1 bone chain, end effector and base bone are the same
							relativeHingeRotationAxis = baseboneRelativeConstraintUV;
						}

						// Project this bone's outer-to-inner direction onto the plane described by the relative hinge rotation axis
						boneDirectionNegated = boneDirectionNegated.projectOntoPlane(relativeHingeRotationAxis);
						break;
				}

				// Calculate the new start joint location as the end joint location plus the outer-to-inner direction UV
				// multiplied by the length of the bone.
				Vec3f newStartLocation = target.plus(boneDirectionNegated.times(boneLength));
				bone.setStartLocation(newStartLocation);

				// ...and set the end joint location of the bone further in to also be at the new start location (if there IS a bone
				// further in - this may be a single bone chain)
				if (i > 0) {
					ikChain.get(i - 1).setEndLocation(newStartLocation);
				}
			}
		}

		// ---------- backward pass from Base to End Effector -----------

		for (int i = 0; i < ikChain.size(); i++) {
			bone = ikChain.get(i);
			boneLength = bone.length();
			boneJoint = bone.getJoint();
			boneJointType = boneJoint.getJointType();

			if (i != 0) {
				// we are NOT working on the base bone

				// Get the inner-to-outer direction of this bone
				Vec3f boneDirection = bone.getDirectionUV();

				if (boneJointType == JointType.BALL) {
					Vec3f prevBoneDirection = ikChain.get(i - 1).getDirectionUV();

					float angleBetween = Vec3f.getAngleBetweenDegs(prevBoneDirection, boneDirection);
					float constraintAngle = boneJoint.getBallJointConstraintDegs();

					if (angleBetween > constraintAngle) {
						// Keep this bone direction constrained within the rotor about the previous bone direction
						boneDirection = Vec3f.getAngleLimitedUnitVectorDegs(boneDirection, prevBoneDirection, constraintAngle);
					}
				}
				else if (boneJointType == JointType.GLOBAL_HINGE) {
					// Get the hinge rotation axis and project our inner-to-outer UV onto it
					Vec3f hingeRotationAxis = boneJoint.getHingeRotationAxis();
					boneDirection = boneDirection.projectOntoPlane(hingeRotationAxis);

					float cwConstraintAngle = boneJoint.getHingeClockwiseConstraintDegs();
					float acwConstraintAngle = boneJoint.getHingeAnticlockwiseConstraintDegs();

					boolean isConstrained =
							!MathUtil.approximatelyEquals(cwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.001f)
									|| !MathUtil.approximatelyEquals(acwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.001f);

					if (isConstrained) {
						Vec3f hingeReferenceAxis = boneJoint.getHingeReferenceAxis();

						// Get the signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone UV
						// Note: ACW rotation is positive, CW rotation is negative.
						float signedAngle = Vec3f.getSignedAngleBetweenDegs(hingeReferenceAxis, boneDirection, hingeRotationAxis);

						// Make our bone inner-to-outer UV the hinge reference axis rotated by its maximum clockwise or anticlockwise rotation as required
						if (signedAngle > acwConstraintAngle) {
							boneDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, acwConstraintAngle, hingeRotationAxis).normalised();
						}
						else if (signedAngle < -cwConstraintAngle) {
							boneDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, -cwConstraintAngle, hingeRotationAxis).normalised();
						}
					}
				}
				else if (boneJointType == JointType.LOCAL_HINGE) {
					Vec3f prevBoneDirection = ikChain.get(i - 1).getDirectionUV();

					// Transform the hinge rotation axis to be relative to the previous bone in the chain
					Vec3f hingeRotationAxis = boneJoint.getHingeRotationAxis();

					// Construct a rotation matrix based on the previous bone's direction
					Mat3f m = Mat3f.createRotationMatrix(prevBoneDirection);

					// Transform the hinge rotation axis into the previous bone's frame of reference
					Vec3f relativeHingeRotationAxis = m.times(hingeRotationAxis).normalise();

					// Project this bone direction onto the plane described by the hinge rotation axis
					// Note: The returned vector is normalised.
					boneDirection = boneDirection.projectOntoPlane(relativeHingeRotationAxis);

					float cwConstraintAngle = boneJoint.getHingeClockwiseConstraintDegs();
					float acwConstraintAngle = boneJoint.getHingeAnticlockwiseConstraintDegs();

					boolean isConstrained = !MathUtil.approximatelyEquals(cwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.001f)
							|| !MathUtil.approximatelyEquals(acwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.001f);

					if (isConstrained) {
						// reference axis in local space
						Vec3f relativeHingeReferenceAxis = m.times(boneJoint.getHingeReferenceAxis()).normalise();

						// Get the signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone UV
						// Note: ACW rotation is positive, CW rotation is negative.
						float signedAngle = Vec3f.getSignedAngleBetweenDegs(relativeHingeReferenceAxis, boneDirection, relativeHingeRotationAxis);

						// Make our bone inner-to-outer UV the hinge reference axis rotated by its maximum clockwise or anticlockwise rotation as required
						if (signedAngle > acwConstraintAngle) {
							boneDirection = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, acwConstraintAngle, relativeHingeRotationAxis).normalise();
						}
						else if (signedAngle < -cwConstraintAngle) {
							boneDirection = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, -cwConstraintAngle, relativeHingeRotationAxis).normalise();
						}
					}
				}

				// At this stage we have an outer-to-inner unit vector for this bone which is within our constraints,
				// so we can set the new inner joint location to be the end joint location of this bone plus the
				// outer-to-inner direction unit vector multiplied by the length of the bone.
				Vec3f newEndLocation = bone.getStartLocation().plus(boneDirection.times(boneLength));
				bone.setEndLocation(newEndLocation);

				// If we are not working on the end effector bone, then we set the start joint location of the next bone in
				// the chain (i.e. the bone closer to the target) to be the new end joint location of this bone.
				if (i < ikChain.size() - 1) {
					ikChain.get(i + 1).setStartLocation(newEndLocation);
				}
			}
			else {
				// handle the base bone

				if (fixedBaseMode) {
					// snap the start location of the base bone back to the fixed base
					bone.setStartLocation(fixedBaseLocation);
				}
				else {
					// project it backwards from the end to the start by its length
					bone.setStartLocation(bone.getEndLocation().minus(bone.getDirectionUV().times(boneLength)));
				}

				if (baseboneConstraintType == BaseboneConstraintType3D.NONE) {
					// Set the new end location of this bone, and if there are more bones,
					// then set the start location of the next bone to be the end location of this bone
					Vec3f newEndLocation = bone.getStartLocation().plus(bone.getDirectionUV().times(boneLength));
					bone.setEndLocation(newEndLocation);

					if (ikChain.size() > 1) {
						ikChain.get(1).setStartLocation(newEndLocation);
					}
				}
				else {
					if (baseboneConstraintType == BaseboneConstraintType3D.GLOBAL_ROTOR) {
						// Get the inner-to-outer direction of this bone
						Vec3f boneDirection = bone.getDirectionUV();

						float angleBetween = Vec3f.getAngleBetweenDegs(baseboneConstraintUV, boneDirection);
						float constraintAngle = bone.getBallJointConstraintDegs();

						if (angleBetween > constraintAngle) {
							boneDirection = Vec3f.getAngleLimitedUnitVectorDegs(boneDirection, baseboneConstraintUV, constraintAngle);
						}

						Vec3f newEndLocation = bone.getStartLocation().plus(boneDirection.times(boneLength));
						bone.setEndLocation(newEndLocation);

						if (ikChain.size() > 1) {
							// set the start location of the next bone to be the end location of this bone
							ikChain.get(1).setStartLocation(newEndLocation);
						}
					}
					else if (baseboneConstraintType == BaseboneConstraintType3D.LOCAL_ROTOR) {
						// Note: The #baseboneRelativeConstraintUV is updated in the FabrikStructure3D.solveForTarget()
						// method BEFORE this FabrikChain3D.solveForTarget() method is called. We no knowledge of the
						// direction of the bone we're connected to in another chain and so cannot calculate this
						// relative base bone constraint direction on our own, but the FabrikStructure3D does it for
						// us so we are now free to use it here.

						// Get the inner-to-outer direction of this bone
						Vec3f direction = bone.getDirectionUV();

						// Constrain about the relative base bone constraint unit vector as necessary
						float angleBetween = Vec3f.getAngleBetweenDegs(baseboneRelativeConstraintUV, direction);
						float constraintAngle = bone.getBallJointConstraintDegs();
						if (angleBetween > constraintAngle) {
							direction = Vec3f.getAngleLimitedUnitVectorDegs(direction, baseboneRelativeConstraintUV, constraintAngle);
						}

						Vec3f newEndLocation = bone.getStartLocation().plus(direction.times(boneLength));
						bone.setEndLocation(newEndLocation);

						if (ikChain.size() > 1) {
							// set the start location of the next bone to be the end location of this bone
							ikChain.get(1).setStartLocation(newEndLocation);
						}
					}
					else if (baseboneConstraintType == BaseboneConstraintType3D.GLOBAL_HINGE) {
						Vec3f hingeRotationAxis = boneJoint.getHingeRotationAxis();
						float cwConstraintAngle = boneJoint.getHingeClockwiseConstraintDegs();
						float acwConstraintAngle = boneJoint.getHingeAnticlockwiseConstraintDegs();

						// Get the inner-to-outer direction of this bone and project it onto the global hinge rotation axis
						Vec3f projectedDirection = bone.getDirectionUV().projectOntoPlane(hingeRotationAxis);

						boolean isConstrained = !MathUtil.approximatelyEquals(cwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.01f)
								|| !MathUtil.approximatelyEquals(acwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.01f);

						if (isConstrained) {
							// constrain global hinge about the reference axis

							Vec3f hingeReferenceAxis = boneJoint.getHingeReferenceAxis();

							// Note: ACW rotation is positive, CW rotation is negative.
							float signedAngle = Vec3f.getSignedAngleBetweenDegs(hingeReferenceAxis, projectedDirection, hingeRotationAxis);

							if (signedAngle > acwConstraintAngle) {
								projectedDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, acwConstraintAngle, hingeRotationAxis).normalise();
							}
							else if (signedAngle < -cwConstraintAngle) {
								projectedDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, -cwConstraintAngle, hingeRotationAxis).normalise();
							}
						}

						// Calc and set the end location of this bone
						Vec3f newEndLocation = bone.getStartLocation().plus(projectedDirection.times(boneLength));
						bone.setEndLocation(newEndLocation);

						if (ikChain.size() > 1) {
							// set the start location of the next bone to be the end location of this bone
							ikChain.get(1).setStartLocation(newEndLocation);
						}
					}
					else if (baseboneConstraintType == BaseboneConstraintType3D.LOCAL_HINGE) {
						Vec3f hingeRotationAxis = baseboneRelativeConstraintUV;
						float cwConstraintAngle = boneJoint.getHingeClockwiseConstraintDegs();
						float acwConstraintAngle = boneJoint.getHingeAnticlockwiseConstraintDegs();

						// Get the inner-to-outer direction of this bone and project it onto the hinge rotation axis
						Vec3f projectedDirection = bone.getDirectionUV().projectOntoPlane(hingeRotationAxis);

						boolean isConstrained = !MathUtil.approximatelyEquals(cwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.01f)
								|| !MathUtil.approximatelyEquals(acwConstraintAngle, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGREES, 0.01f);

						if (isConstrained) {
							// constrain local hinge about the reference axis

							Vec3f hingeReferenceAxis = baseboneRelativeReferenceConstraintUV;

							// Note: ACW rotation is positive, CW rotation is negative.
							float signedAngle = Vec3f.getSignedAngleBetweenDegs(hingeReferenceAxis, projectedDirection, hingeRotationAxis);

							if (signedAngle > acwConstraintAngle) {
								projectedDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, acwConstraintAngle, hingeRotationAxis).normalise();
							}
							else if (signedAngle < -cwConstraintAngle) {
								projectedDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, -cwConstraintAngle, hingeRotationAxis).normalise();
							}
						}

						Vec3f newEndLocation = bone.getStartLocation().plus(projectedDirection.times(boneLength));
						bone.setEndLocation(newEndLocation);

						if (ikChain.size() > 1) {
							// set the start location of the next bone to be the end location of this bone
							ikChain.get(1).setStartLocation(newEndLocation);
						}
					}
				}
			}
		}

		lastTargetLocation.set(target);

		// DEBUG - check the live chain length and the originally calculated chain length are the same
		/*
		if (Math.abs( this.getLiveChainLength() - mChainLength) > 0.01f) {
			System.out.println("Chain length off by > 0.01f");
		}
		*/

		// Finally, calculate and return the distance between the current end effector location and the target.
		return Vec3f.distanceBetween(ikChain.getLast().getEndLocation(), target);
	}

	/**
	 * Calculate the length of this IK chain by adding up the lengths of each bone.
	 * <p>
	 * The resulting chain length is stored in the mChainLength property.
	 * <p>
	 * This method is called each time a bone is added to the chain. In addition, the
	 * length of each bone is recalculated during the process to ensure that our chain
	 * length is accurate. As the typical usage of a FabrikChain3D is to add a number
	 * of bones once (during setup) and then use them, this should not have any
	 * performance implication on the typical execution cycle of a FabrikChain3D object,
	 * as this method will not be called in any method which executes regularly.
	 */
	@Override
	public void updateChainLength() {
		chainLength = 0.0f;
		for (FabrikBone3D bone : ikChain) {
			chainLength += bone.length();
		}
	}

	/**
	 * Update the embedded target for this chain.
	 * <p>
	 * The internal mEmbeddedTarget object is updated with the location of the provided parameter.
	 * If the chain is not in useEmbeddedTarget mode then a RuntimeException is thrown.
	 * Embedded target mode can be enabled by calling setEmbeddedTargetMode(true) on the chain.
	 *
	 * @param newTarget The location of the embedded target.
	 */
	@Override
	public void updateEmbeddedTarget(Vec3f newTarget) {
		if (!useEmbeddedTarget) {
			throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true).");
		}

		embeddedTarget.set(newTarget);
	}

	/**
	 * Update the embedded target for this chain.
	 * <p>
	 * The internal mEmbeddedTarget object is updated with the location of the provided parameter.
	 * If the chain is not in useEmbeddedTarget mode then a RuntimeException is thrown.
	 * Embedded target mode can be enabled by calling setEmbeddedTargetMode(true) on the chain.
	 *
	 * @param x The x location of the embedded target.
	 * @param y The y location of the embedded target.
	 * @param z The z location of the embedded target.
	 */
	public void updateEmbeddedTarget(float x, float y, float z) {
		if (!useEmbeddedTarget) {
			throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true).");
		}

		embeddedTarget.set(new Vec3f(x, y, z));
	}

	/**
	 * @return Cloned IK Chain of this FabrikChain3D, that is, the list of FabrikBone3D objects.
	 */
	private List<FabrikBone3D> cloneIkChain() {
		List<FabrikBone3D> clonedChain = new ArrayList<>(ikChain.size());

		for (FabrikBone3D bone : ikChain) {
			clonedChain.add(new FabrikBone3D(bone));
		}

		return clonedChain;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxIterationAttempts() {
		return maxIterationAttempts;
	}

	/**
	 * Set the maximum number of attempts that will be made to solve this IK chain.
	 * <p>
	 * The FABRIK algorithm may require more than a single pass in order to solve
	 * a given IK chain for an acceptable distance threshold. If we reach this
	 * iteration limit then we stop attempting to solve the IK chain. Further details
	 * on this topic are provided in the {@link #maxIterationAttempts} documentation.
	 * <p>
	 * If a maxIterations value of less than 1 is provided then an IllegalArgumentException is
	 * thrown, as we must make at least a single attempt to solve an IK chain.
	 *
	 * @param maxIterations The maximum number of attempts that will be made to solve this IK chain.
	 */
	@Override
	public void setMaxIterationAttempts(int maxIterations) {
		if (maxIterations < 1) {
			throw new IllegalArgumentException("The maximum number of attempts to solve this IK chain must be at least 1.");
		}

		maxIterationAttempts = maxIterations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getMinIterationChange() {
		return this.minIterationChange;
	}

	/**
	 * Set the minimum iteration change before we dynamically abort any further attempts to solve this IK chain.
	 * <p>
	 * If the latest solution found has changed by less than this amount then we consider the progress being made
	 * to be not worth the computational effort and dynamically abort any further attempt to solve the chain for
	 * the current target to minimise CPU usage.
	 * <p>
	 * If a minIterationChange value of less than zero is specified then an IllegalArgumentException is
	 * thrown.
	 *
	 * @param minIterationChange The minimum change in solve distance from one iteration to the next.
	 */
	@Override
	public void setMinIterationChange(float minIterationChange) {
		if (minIterationChange < 0f) {
			throw new IllegalArgumentException("The minimum iteration change value must be more than or equal to zero.");
		}

		this.minIterationChange = minIterationChange;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getSolveDistanceThreshold() {
		return solveDistanceThreshold;
	}

	/**
	 * Set the distance threshold within which we consider the IK chain to be successfully solved.
	 * <p>
	 * If a solve distance value of less than zero is specified then an IllegalArgumentException is thrown.
	 *
	 * @param solveDistance The distance between the end effector of this IK chain and target within which we will accept the solution.
	 */
	@Override
	public void setSolveDistanceThreshold(float solveDistance) {
		if (solveDistance < 0f) {
			throw new IllegalArgumentException("The solve distance threshold must be greater than or equal to zero.");
		}

		solveDistanceThreshold = solveDistance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseboneConstraintType == null) ? 0 : baseboneConstraintType.hashCode());
		result = prime * result + ((baseboneConstraintUV == null) ? 0 : baseboneConstraintUV.hashCode());
		result = prime * result + ((baseboneRelativeConstraintUV == null) ? 0 : baseboneRelativeConstraintUV.hashCode());
		result = prime * result + ((baseboneRelativeReferenceConstraintUV == null) ? 0 : baseboneRelativeReferenceConstraintUV.hashCode());
		result = prime * result + ((ikChain == null) ? 0 : ikChain.hashCode());
		result = prime * result + Float.floatToIntBits(chainLength);
		result = prime * result + connectedBoneNumber;
		result = prime * result + connectedChainNumber;
		result = prime * result + Float.floatToIntBits(constraintLineWidth);
		result = prime * result + Float.floatToIntBits(currentSolveDistance);
		result = prime * result + embeddedTarget.hashCode();
		result = prime * result + ((fixedBaseLocation == null) ? 0 : fixedBaseLocation.hashCode());
		result = prime * result + (fixedBaseMode ? 1231 : 1237);
		result = prime * result + lastBaseLocation.hashCode();
		result = prime * result + lastTargetLocation.hashCode();
		result = prime * result + maxIterationAttempts;
		result = prime * result + Float.floatToIntBits(minIterationChange);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(solveDistanceThreshold);
		result = prime * result + (useEmbeddedTarget ? 1231 : 1237);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--- FabrikChain3D: ").append(name).append(" ---").append(Utils.NEW_LINE);

		if (!ikChain.isEmpty()) {
			sb.append("Bone count:    : ").append(ikChain.size()).append(Utils.NEW_LINE);
			sb.append("Base location  : ").append(getBaseLocation()).append(Utils.NEW_LINE);
			sb.append("Chain length   : ").append(getChainLength()).append(Utils.NEW_LINE);

			if (fixedBaseMode) {
				sb.append("Fixed base mode: Yes").append(Utils.NEW_LINE);
			}
			else {
				sb.append("Fixed base mode: No").append(Utils.NEW_LINE);
			}

			for (FabrikBone3D aBone : this.ikChain) {
				sb.append("--- Bone: ").append(aBone).append(" ---").append(Utils.NEW_LINE);
				sb.append(aBone.toString()).append(Utils.NEW_LINE);
			}
		}
		else {
			sb.append("Chain does not contain any bones.").append(Utils.NEW_LINE);
		}

		return sb.toString();
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
		FabrikChain3D other = (FabrikChain3D) obj;
		if (baseboneConstraintType != other.baseboneConstraintType) {
			return false;
		}
		if (baseboneConstraintUV == null) {
			if (other.baseboneConstraintUV != null) {
				return false;
			}
		}
		else if (!baseboneConstraintUV.equals(other.baseboneConstraintUV)) {
			return false;
		}
		if (baseboneRelativeConstraintUV == null) {
			if (other.baseboneRelativeConstraintUV != null) {
				return false;
			}
		}
		else if (!baseboneRelativeConstraintUV.equals(other.baseboneRelativeConstraintUV)) {
			return false;
		}
		if (baseboneRelativeReferenceConstraintUV == null) {
			if (other.baseboneRelativeReferenceConstraintUV != null) {
				return false;
			}
		}
		else if (!baseboneRelativeReferenceConstraintUV.equals(other.baseboneRelativeReferenceConstraintUV)) {
			return false;
		}
		if (ikChain == null) {
			if (other.ikChain != null) {
				return false;
			}
		}
		else if (!ikChain.equals(other.ikChain)) {
			return false;
		}
		if (Float.floatToIntBits(chainLength) != Float.floatToIntBits(other.chainLength)) {
			return false;
		}
		if (connectedBoneNumber != other.connectedBoneNumber) {
			return false;
		}
		if (connectedChainNumber != other.connectedChainNumber) {
			return false;
		}
		if (Float.floatToIntBits(constraintLineWidth) != Float.floatToIntBits(other.constraintLineWidth)) {
			return false;
		}
		if (Float.floatToIntBits(currentSolveDistance) != Float.floatToIntBits(other.currentSolveDistance)) {
			return false;
		}
		if (!embeddedTarget.equals(other.embeddedTarget)) {
			return false;
		}
		if (fixedBaseLocation == null) {
			if (other.fixedBaseLocation != null) {
				return false;
			}
		}
		else if (!fixedBaseLocation.equals(other.fixedBaseLocation)) {
			return false;
		}
		if (fixedBaseMode != other.fixedBaseMode) {
			return false;
		}
		if (!lastBaseLocation.equals(other.lastBaseLocation)) {
			return false;
		}
		if (!lastTargetLocation.equals(other.lastTargetLocation)) {
			return false;
		}
		if (maxIterationAttempts != other.maxIterationAttempts) {
			return false;
		}
		if (Float.floatToIntBits(minIterationChange) != Float.floatToIntBits(other.minIterationChange)) {
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
		if (Float.floatToIntBits(solveDistanceThreshold) != Float.floatToIntBits(other.solveDistanceThreshold)) {
			return false;
		}
		return useEmbeddedTarget == other.useEmbeddedTarget;
	}

	public enum BaseboneConstraintType3D implements BaseboneConstraintType {
		/**
		 * No constraint - basebone may rotate freely
		 */
		NONE,

		/**
		 * World-space rotor constraint
		 */
		GLOBAL_ROTOR,

		/**
		 * Rotor constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
		 */
		LOCAL_ROTOR,

		/**
		 * World-space hinge constraint
		 */
		GLOBAL_HINGE,

		/**
		 * Hinge constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
		 */
		LOCAL_HINGE
	}

}
