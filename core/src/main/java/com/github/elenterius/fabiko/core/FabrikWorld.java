package com.github.elenterius.fabiko.core;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class FabrikWorld {

	public static final Vector3fc X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3fc NEG_X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3fc Y_AXIS = new Vector3f(0, 1, 0);
	public static final Vector3fc NEG_Y_AXIS = new Vector3f(0, -1, 0);
	public static final Vector3fc Z_AXIS = new Vector3f(0, 0, 1);
	public static final Vector3fc NEG_Z_AXIS = new Vector3f(0, 0, -1);

	public static final Vector3fc RIGHT = X_AXIS;
	public static final Vector3fc LEFT = NEG_X_AXIS;
	public static final Vector3fc UP = Y_AXIS;
	public static final Vector3fc DOWN = NEG_Y_AXIS;

	/// moves towards viewer
	public static final Vector3fc BACKWARDS = Z_AXIS;

	/// moves away from viewer
	public static final Vector3fc FORWARDS = NEG_Z_AXIS;

	public static final Vector3fc NORTH = FORWARDS;
	public static final Vector3fc SOUTH = BACKWARDS;
	public static final Vector3fc WEST = LEFT;
	public static final Vector3fc EAST = RIGHT;

}
