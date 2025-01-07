package com.github.elenterius.fabiko.visualization;

import org.joml.Vector4fc;

public interface Color4fc extends Vector4fc {

	Color4fc CYAN = new Color4f(0f, 1f, 1f, 1f);
	Color4fc MAGENTA = new Color4f(1f, 0f, 1f, 1f);
	Color4fc YELLOW = new Color4f(1f, 1f, 0f, 1f);

	Color4fc BLACK = new Color4f(0f, 0f, 0f, 1f);
	Color4fc GREY = new Color4f(0.5f, 0.5f, 0.5f, 1f);
	Color4fc WHITE = new Color4f(1f, 1f, 1f, 1f);

	Color4fc RED = new Color4f(1f, 0f, 0f, 1f);
	Color4fc GREEN = new Color4f(0f, 1f, 0f, 1f);
	Color4fc BLUE = new Color4f(0f, 0f, 1f, 1f);

	Color4fc MID_RED = new Color4f(0.6f, 0f, 0f, 1f);
	Color4fc MID_GREEN = new Color4f(0f, 0.6f, 0f, 1f);
	Color4fc MID_BLUE = new Color4f(0f, 0f, 0.6f, 1f);

	Color4fc ORANGE = new Color4f(1f, 0.6471f, 0f, 1f);
	Color4fc ORANGE_RED = new Color4f(1f, 0.28f, 0f, 1f);

	float r();

	float g();

	float b();

	float a();

	Color4f add(float red, float green, float blue, Color4f dest);

	Color4f sub(float red, float green, float blue, Color4f dest);

	Color4f lighten(float amount, Color4f dest);

	/// Darken the RGB components of the color by a given amount and modifies dest.
	///
	/// @param amount the value to subtract from each `R`, `G` and `B` component
	/// @return dest
	Color4f darken(float amount, Color4f dest);

}
