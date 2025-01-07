package com.github.elenterius.fabiko.demo.util;

import com.github.elenterius.fabiko.core.FabrikBone3f;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.visualization.Color4fc;
import com.github.elenterius.fabiko.visualization.StructureDrawInfo;

import java.util.ArrayList;
import java.util.List;

public record StructureDrawInfoImpl(List<ChainDrawInfoImpl> chains) implements StructureDrawInfo {

	public StructureDrawInfoImpl() {
		this(new ArrayList<>());
	}

	public ChainDrawInfoImpl addChainDrawInfo() {
		ChainDrawInfoImpl chainDrawInfo = new ChainDrawInfoImpl(new ArrayList<>());
		chains.add(chainDrawInfo);
		return chainDrawInfo;
	}

	public ChainDrawInfoImpl addChainDrawInfo(FabrikChain3f chain, Color4fc color) {
		ChainDrawInfoImpl chainDrawInfo = new ChainDrawInfoImpl(new ArrayList<>());

		for (FabrikBone3f bone : chain.getBones()) {
			BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
			boneDrawInfo.setColorFrom(color);
			chainDrawInfo.bones().add(boneDrawInfo);
		}

		chains.add(chainDrawInfo);
		return chainDrawInfo;
	}

	public ChainDrawInfoImpl addChainDrawInfo(FabrikChain3f chain, ChainDrawInfoImpl.ColorFunction colorFunction) {
		ChainDrawInfoImpl chainDrawInfo = new ChainDrawInfoImpl(new ArrayList<>());

		FabrikBone3f[] chainBones = chain.getBones();
		for (int i = 0; i < chainBones.length; i++) {
			FabrikBone3f bone = chainBones[i];
			BoneDrawInfoImpl boneDrawInfo = new BoneDrawInfoImpl(bone);
			boneDrawInfo.setColorFrom(colorFunction.getColor(i, bone));
			chainDrawInfo.bones().add(boneDrawInfo);
		}

		chains.add(chainDrawInfo);
		return chainDrawInfo;
	}

}
