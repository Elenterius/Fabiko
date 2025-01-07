package com.github.elenterius.fabiko.visualization;

import org.joml.Vector3fc;

public interface BoneDrawInfo {
	Vector3fc startLocation();

	Vector3fc endLocation();

	Color4fc color();

	float lineWidth();
}
