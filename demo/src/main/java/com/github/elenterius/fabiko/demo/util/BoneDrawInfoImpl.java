package com.github.elenterius.fabiko.demo.util;

import com.github.elenterius.fabiko.core.FabrikBone3f;
import com.github.elenterius.fabiko.visualization.BoneDrawInfo;
import com.github.elenterius.fabiko.visualization.Color4f;
import com.github.elenterius.fabiko.visualization.Color4fc;
import org.joml.Vector3fc;

public final class BoneDrawInfoImpl implements BoneDrawInfo {

	private final FabrikBone3f bone;
	private final Color4f color = new Color4f();
	private float lineWidth = 1f;

	public BoneDrawInfoImpl(FabrikBone3f bone, Color4f color, float lineWidth) {
		this.bone = bone;
		this.color.set(color);
		this.lineWidth = lineWidth;
	}

	public BoneDrawInfoImpl(FabrikBone3f bone) {
		this.bone = bone;
		color.set(Color4fc.WHITE);
	}

	@Override
	public Vector3fc startLocation() {
		return bone.getStartLocation();
	}

	@Override
	public Vector3fc endLocation() {
		return bone.getEndLocation();
	}

	@Override
	public Color4fc color() {
		return color;
	}

	public BoneDrawInfoImpl setColorFrom(Color4fc color) {
		this.color.set(color);
		return this;
	}

	@Override
	public float lineWidth() {
		return lineWidth;
	}

	public BoneDrawInfoImpl setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		return this;
	}

}
