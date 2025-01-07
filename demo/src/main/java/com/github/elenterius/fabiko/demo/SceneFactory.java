package com.github.elenterius.fabiko.demo;

import com.github.elenterius.fabiko.demo.scenes.*;

import java.util.ArrayList;
import java.util.List;

public final class SceneFactory {

	private SceneFactory() {
	}

	private static final List<Factory<? extends DemoScene>> FACTORIES = new ArrayList<>();

	static {
		register(UnconstrainedBones::new);
		register(RotorJointConstrainedBones::new);
		register(RotorConstrainedBaseBones::new);
		register(FreelyRotatingGlobalHinges::new);
		register(GlobalHingesWithReferenceAxisConstraints::new);
		register(FreelyRotatingLocalHinges::new);
		register(LocalHingesWithReferenceAxisConstraints::new);
		register(ConnectedChains::new);
		register(GlobalRotorConstrainedConnectedChains::new);
		register(LocalRotorConstrainedConnectedChains::new);
		register(ConnectedChainsWithFreelyRotatingGlobalHingesBaseboneConstraints::new);
		register(ConnectedChainsWithEmbeddedTargets::new);
	}

	private static <T extends DemoScene> void register(Factory<T> factory) {
		FACTORIES.add(factory);
	}

	public static int getNumberOfScenes() {
		return FACTORIES.size();
	}

	public static DemoScene crate(int demoIndex) {
		return FACTORIES.get(demoIndex).create();
	}

	interface Factory<T> {
		T create();
	}

}
