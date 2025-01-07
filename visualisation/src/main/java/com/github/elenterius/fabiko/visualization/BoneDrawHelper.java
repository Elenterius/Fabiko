/// The MIT License (MIT)
///
/// Copyright (c) 2016-2020 Alastair Lansley / Federation University Australia
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.

package com.github.elenterius.fabiko.visualization;

import com.github.elenterius.fabiko.core.FabrikBone3f;
import org.joml.Matrix4fc;

/// Refactored copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/FabrikLine3D.java">FabrikLine3D</a>
public final class BoneDrawHelper {

	private static final LineDrawHelper LINE_DRAW_HELPER = new LineDrawHelper();

	private BoneDrawHelper() {
	}

	/// Draw this bone as a line.
	///
	/// The line will be drawn in the colour and line width as taken from the bone.
	///
	/// @param bone                The bone to draw
	/// @param modelViewProjection The ModelViewProjection matrix with which to draw the bone
	public static void draw(BoneDrawInfo bone, Matrix4fc modelViewProjection) {
		LINE_DRAW_HELPER.draw(bone.startLocation(), bone.endLocation(), bone.color(), bone.lineWidth(), modelViewProjection);
	}

	/// Draw this bone as a line using a specific line width.
	///
	/// The line will be drawn in the colour as taken from the bone.
	///
	/// @param bone                The bone to draw
	/// @param lineWidth           The width of the line use to draw the bone
	/// @param modelViewProjection The ModelViewProjection matrix with which to draw the bone
	public static void draw(BoneDrawInfo bone, float lineWidth, Matrix4fc modelViewProjection) {
		LINE_DRAW_HELPER.draw(bone.startLocation(), bone.endLocation(), bone.color(), lineWidth, modelViewProjection);
	}

	/// Draw this bone as a line using a specific colour and line width.
	///
	/// The line will be drawn in the colour and line width as taken from the provided arguments.
	///
	/// @param bone                the bone to draw
	/// @param colour              the colour to draw the bone
	/// @param lineWidth           the width of the line use to draw the bone
	/// @param modelViewProjection the ModelViewProjection matrix with which to draw the bone
	public static void draw(FabrikBone3f bone, Color4f colour, float lineWidth, Matrix4fc modelViewProjection) {
		LINE_DRAW_HELPER.draw(bone.getStartLocation(), bone.getEndLocation(), colour, lineWidth, modelViewProjection);
	}

	/// Draw the provided IK chain as a series of lines using the colour and line width properties of each bone.
	///
	/// @param chain               the chain to draw
	/// @param modelViewProjection the ModelViewProjection matrix with which to draw the chain
	public static void draw(ChainDrawInfo chain, Matrix4fc modelViewProjection) {
		for (BoneDrawInfo bone : chain.bones()) {
			draw(bone, modelViewProjection);
		}

		//		if (chain.hasEmbeddedTarget()) {
		//			pointDrawHelper.draw(chain.embeddedTarget(), Color4f.YELLOW, 4f, modelViewProjection);
		//		}
	}

	/// Draw a FabrikChain3D as a series of lines.
	///
	///
	/// @param chain               the chain to draw.
	/// @param lineWidth           the width of the line use to draw the bone
	/// @param modelViewProjection the ModelViewProjection matrix with which to draw the chain
	public static void draw(ChainDrawInfo chain, float lineWidth, Matrix4fc modelViewProjection) {
		for (BoneDrawInfo bone : chain.bones()) {
			draw(bone, lineWidth, modelViewProjection);
		}

		//		if (chain.hasEmbeddedTarget()) {
		//			pointDrawHelper.draw(chain.embeddedTarget(), Color4f.YELLOW, 4f, modelViewProjection);
		//		}
	}

	/// Draw the provided FabrikStructure3D by drawing each chain in this structure.
	///
	/// All bones in all chains are drawn with line widths and colours as per their member properties.
	///
	/// @param structure           The structure to draw
	/// @param modelViewProjection The ModelViewProjection matrix with which to draw the structure
	public static void draw(StructureDrawInfo structure, Matrix4fc modelViewProjection) {
		for (ChainDrawInfo chain : structure.chains()) {
			draw(chain, modelViewProjection);
		}
	}

	/// Draw the provided FabrikStructure3D by drawing each chain in this structure.
	///
	/// All bones in all chains are draw with colours as per their member properties, but with the provided line width.
	///
	/// @param structure           the structure to draw
	/// @param lineWidth           the width of the line use to draw the structure
	/// @param modelViewProjection the ModelViewProjection matrix with which to draw the structure
	public static void draw(StructureDrawInfo structure, float lineWidth, Matrix4fc modelViewProjection) {
		for (ChainDrawInfo chain : structure.chains()) {
			draw(chain, lineWidth, modelViewProjection);
		}
	}

}