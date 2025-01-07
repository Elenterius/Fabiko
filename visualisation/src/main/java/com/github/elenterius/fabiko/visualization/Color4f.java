package com.github.elenterius.fabiko.visualization;

import org.joml.Math;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class Color4f extends Vector4f implements Color4fc {

	/// The constructed color has it's RGBA components set to default values of 1.0f, which equates to white at full opacity.
	public Color4f() {
		set(1f);
	}

	/// @param color the source color to copy the component values from
	public Color4f(Color4f color) {
		super(color);
	}

	/// @param color the source color to copy the component values from
	public Color4f(Color4fc color) {
		super(color);
	}

	/**
	 * The valid range of each component is 0.0f to 1.0f inclusive, any values outside of this range will be clamped.
	 *
	 * @param red   The red component of this colour.
	 * @param green The green component of this colour.
	 * @param blue  The blue component of this colour.
	 * @param alpha The alpha component of this colour.
	 */
	public Color4f(float red, float green, float blue, float alpha) {
		x = clamp(red);
		y = clamp(green);
		z = clamp(blue);
		w = clamp(alpha);
	}

	public Color4f(float red, float green, float blue) {
		x = clamp(red);
		y = clamp(green);
		z = clamp(blue);
		w = 1f;
	}

	public static float clamp(float componentValue) {
		return Math.clamp(0f, 1f, componentValue);
	}

	@Override
	public float r() {
		return x;
	}

	@Override
	public float g() {
		return y;
	}

	@Override
	public float b() {
		return z;
	}

	@Override
	public float a() {
		return w;
	}

	@Override
	public Vector4f set(float[] rgba) {
		this.x = clamp(rgba[0]);
		this.y = clamp(rgba[1]);
		this.z = clamp(rgba[2]);
		this.w = clamp(rgba[3]);
		return this;
	}

	/// Set the RGBA values of this Color4f object from a source Color4f object.
	///
	/// Source values are **clamped** to the range `0.0f…1.0f`.
	///
	/// @param rgba The source colour to set the values of this colour to.
	@Override
	public Vector4f set(Vector4fc rgba) {
		x = clamp(rgba.x());
		y = clamp(rgba.y());
		z = clamp(rgba.z());
		w = clamp(rgba.w());
		return this;
	}

	/// Set the r, g, b and a components to the supplied values.
	///
	/// Any values outside the range `0.0f…1.0f` are clamped to the nearest valid value.
	///
	/// @param red   the red   component
	/// @param green the green component
	/// @param blue  the blue  component
	/// @param alpha the alpha component
	/// @return this
	@Override
	public Vector4f set(float red, float green, float blue, float alpha) {
		x = clamp(red);
		y = clamp(green);
		z = clamp(blue);
		w = clamp(alpha);
		return this;
	}

	/// Add to the RGB components of this colour by the given amounts and return this modified colour for chaining.
	///
	/// When adding, colour values are clamped to a maximum value of 1.0f.
	///
	/// @param red   The red   component to add to this colour.
	/// @param green The green component to add to this colour.
	/// @param blue  The blue  component to add to this colour.
	/// @return this
	public Color4f add(float red, float green, float blue) {
		return add(red, green, blue, this);
	}

	@Override
	public Color4f add(float red, float green, float blue, Color4f dest) {
		dest.x = clamp(this.x + red);
		dest.y = clamp(this.y + green);
		dest.z = clamp(this.z + blue);
		return dest;
	}

	/// Subtract from the RGB components of this color by the given amounts and modifies this.
	///
	/// When subtracting, values are **clamped** to the range `0.0f…1.0f`.
	///
	/// @param red   the red   component to subtract
	/// @param green the green component to subtract
	/// @param blue  the blue  component to subtract
	/// @return this
	public Color4f sub(float red, float green, float blue) {
		return sub(red, green, blue, this);
	}

	@Override
	public Color4f sub(float red, float green, float blue, Color4f dest) {
		dest.x = clamp(this.x - red);
		dest.y = clamp(this.y - green);
		dest.z = clamp(this.z - blue);
		return dest;
	}

	/// Lighten the RGB components of this colour by a given amount.
	///
	/// Resulting colour components are clamped to the range `0.0f…1.0f`.
	///
	/// @param amount The value to add to each (RGB only) component of the colour.
	/// @return The 'lightened' colour with the amount added to each component.
	public Color4f lighten(float amount) {
		return lighten(amount, this);
	}

	public Color4f lighten(float amount, Color4f dest) {
		return add(amount, amount, amount, dest);
	}

	/// Darken the RGB components of this colour by a given amount.
	///
	/// Resulting color components are clamped to the range 0.0f…1.0f.
	///
	/// @param amount The value to subtract from each (RGB only) component of the color.
	/// @return this
	public Color4f darken(float amount) {
		return darken(amount, this);
	}

	/// {@inheritDoc}
	@Override
	public Color4f darken(float amount, Color4f dest) {
		return sub(amount, amount, amount, dest);
	}

}
