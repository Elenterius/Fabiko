package com.github.elenterius.fabiko.demo.scenes;

import com.github.elenterius.fabiko.core.FabrikStructure3f;
import com.github.elenterius.fabiko.demo.util.StructureDrawInfoImpl;
import com.github.elenterius.fabiko.visualization.StructureDrawInfo;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author jsalvo
 */
public abstract class DemoScene implements Scene {

	public static final Vector3fc X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3fc Y_AXIS = new Vector3f(0, 1, 0);
	public static final Vector3fc Z_AXIS = new Vector3f(0, 0, 1);

	public static final Vector3fc NEG_X_AXIS = new Vector3f(-1, 0, 0);
	public static final Vector3fc NEG_Y_AXIS = new Vector3f(0, -1, 0);
	public static final Vector3fc NEG_Z_AXIS = new Vector3f(0, 0, -1);

	protected static final Vector3fc DEFAULT_BONE_DIRECTION = new Vector3f(NEG_Z_AXIS);
	protected static final float DEFAULT_BONE_LENGTH = 10f;

	protected String title = "";
	protected FabrikStructure3f structure;
	protected StructureDrawInfoImpl structureDrawInfo;

	public FabrikStructure3f getStructure() {
		return structure;
	}

	public StructureDrawInfo getStructureDrawInfo() {
		return structureDrawInfo;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
