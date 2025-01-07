package tests.caliko;

import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.utils.Vec3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CalikoHack {

	public static void setBaseboneRelativeConstraintUV(FabrikChain3D chain, Vec3f constraintUV) {
		try {
			Method privateMethod = FabrikChain3D.class.getDeclaredMethod("setBaseboneRelativeConstraintUV", Vec3f.class);
			privateMethod.setAccessible(true);
			privateMethod.invoke(chain, constraintUV);
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
