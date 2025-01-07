package com.github.elenterius.fabiko.demo.util;

import com.github.elenterius.fabiko.core.FabrikBone3f;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.visualization.ChainDrawInfo;
import com.github.elenterius.fabiko.visualization.Color4f;
import com.github.elenterius.fabiko.visualization.Color4fc;

import java.util.ArrayList;
import java.util.List;

public record ChainDrawInfoImpl(List<BoneDrawInfoImpl> bones) implements ChainDrawInfo {

	public ChainDrawInfoImpl() {
		this(new ArrayList<>());
	}

	public BoneDrawInfoImpl getBaseBone() {
		return bones.getFirst();
	}

	public BoneDrawInfoImpl addBoneDrawInfo(FabrikBone3f bone) {
		BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
		bones.add(boneDrawInfo);
		return boneDrawInfo;
	}

	public BoneDrawInfoImpl addBoneDrawInfo(FabrikBone3f bone, Color4fc color) {
		BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
		boneDrawInfo.setColorFrom(color);
		bones.add(boneDrawInfo);
		return boneDrawInfo;
	}

	public void addAllBoneDrawInfos(FabrikBone3f[] bones, Color4fc color) {
		for (FabrikBone3f bone : bones) {
			BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
			boneDrawInfo.setColorFrom(color);
			this.bones.add(boneDrawInfo);
		}
	}

	public void addAllBoneDrawInfos(FabrikChain3f chain, Color4fc color) {
		for (FabrikBone3f bone : chain.getBones()) {
			BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
			boneDrawInfo.setColorFrom(color);
			this.bones.add(boneDrawInfo);
		}
	}

	public void addAllBoneDrawInfos(FabrikChain3f chain, ColorFunction color) {
		FabrikBone3f[] chainBones = chain.getBones();
		for (int i = 0; i < chainBones.length; i++) {
			FabrikBone3f bone = chainBones[i];
			BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
			boneDrawInfo.setColorFrom(color.getColor(i, bone));
			this.bones.add(boneDrawInfo);
		}
	}

	public interface ColorFunction {
		Color4fc getColor(int index, FabrikBone3f bone);
	}

	public static final class AlternatingColorFunction implements ColorFunction {
		private final Color4fc colorA;
		private final Color4fc colorB;

		public AlternatingColorFunction(Color4fc baseColor, float delta) {
			this.colorA = baseColor.lighten(delta, new Color4f());
			this.colorB = baseColor.darken(delta, new Color4f());
		}

		public AlternatingColorFunction(Color4fc colorA, Color4fc colorB) {
			this.colorA = colorA;
			this.colorB = colorB;
		}

		@Override
		public Color4fc getColor(int i, FabrikBone3f bone) {
			return (i % 2 == 0) ? colorA : colorB;
		}

	}

}
