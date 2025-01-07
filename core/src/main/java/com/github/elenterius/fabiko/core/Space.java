package com.github.elenterius.fabiko.core;

/// The space within which to interpret the position and orientation of a bone joint.
public enum Space {
	/// Joint is relative to the parent bone (i.e. previous joint).
	LOCAL,

	/// Joint is relative to the entire scene or a global origin point.
	///
	/// Also known as world-space.
	GLOBAL
}
