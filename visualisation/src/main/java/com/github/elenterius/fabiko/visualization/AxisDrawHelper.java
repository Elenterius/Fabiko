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
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/// Refactored copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Axis.java">Axis</a>
public class AxisDrawHelper {

	private static final String VERTEX_SHADER_SOURCE = """
			#version 330
			in vec3 vertexLocation;   // Incoming vertex attribute
			in vec4 vertexColour;     // Incoming colour value
			flat out vec4 fragColour; // Outgoing colour value
			uniform mat4 mvpMatrix;   // Combined Model/View/Projection matrix
			void main(void) {
				fragColour = vertexColour;                          // Pass through colour
				gl_Position = mvpMatrix * vec4(vertexLocation, 1); // Project our geometry
			}
			""";

	private static final String FRAGMENT_SHADER_SOURCE = """
			#version 330
			flat in vec4 fragColour; // Incoming colour from vertex shader
			out vec4 vOutputColour;  // Outgoing colour value
			void main() {
				vOutputColour = fragColour;
			}
			""";

	private static final int NUM_VERTICES = 3 * 2; // 3 lines with 2 vertices each
	private static final int VERTEX_COMPONENTS = 3; // xyz
	private static final int COLOUR_COMPONENTS = 4; // rgba
	private static final int COMPONENT_COUNT = VERTEX_COMPONENTS + COLOUR_COMPONENTS;

	private static final FloatBuffer VERTEX_BUFFER = BufferUtils.createFloatBuffer(NUM_VERTICES * COMPONENT_COUNT);
	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer LINE_WIDTH_BUFFER = BufferUtils.createFloatBuffer(1);

	private static final int VAO_ID;
	private static final int VBO_ID;

	private static final ShaderProgram SHADER_PROGRAM;

	private static final Matrix4f CORRECTION = new Matrix4f().m22(-1);

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addAttribute("vertexColour");
		SHADER_PROGRAM.addUniform("mvpMatrix");

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----

		// Create a VAO and bind to it
		VAO_ID = glGenVertexArrays();
		glBindVertexArray(VAO_ID);

		// ----- Vertex Buffer Object (VBO) -----

		// Create a VBO and bind to it
		VBO_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);

		// Note: We do NOT copy the data into the buffer at this time - we do that on draw!

		// Vertex attribute configuration
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"), // Vertex location attribute index
				VERTEX_COMPONENTS, // Number of location components per vertex
				GL_FLOAT, // Data type
				false, // Normalised?
				COMPONENT_COUNT * Float.BYTES, // Stride
				0); // Offset

		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexColour"),  // Vertex colour attribute index
				COLOUR_COMPONENTS,  // Number of colour components per vertex
				GL_FLOAT,  // Data type
				true,   // Normalised?
				COMPONENT_COUNT * Float.BYTES,  // Stride
				(long) VERTEX_COMPONENTS * Float.BYTES);  // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexColour"));

		// Unbind VAO - all the buffer and attribute settings above will now be associated with our VAO
		glBindVertexArray(0);
	}

	Matrix4f m = new Matrix4f();
	Matrix4f mvp = new Matrix4f();

	// The float array storing the axis vertex (including color) data
	private float[] axisData;
	private float lineWidth = 1f;

	/// Construct an axis which will be draw with a given line length and line width.
	///
	/// @param axisLength the length of the line representing the X/Y/Z axes
	/// @param lineWidth  the width of the line representing the X/Y/Z axes
	/// @throws IllegalArgumentException when the axis length is less than 0.0f
	/// @throws IllegalArgumentException when the line width is less than 1.0f
	public AxisDrawHelper(float axisLength, float lineWidth) {
		setAxisLength(axisLength);
		setLineWidth(lineWidth);

		// Note:
		// We cannot just transfer the data into the vertex buffer here once instead of per frame
		// because we may have multiple Axis objects, each with their own axis size.
	}

	/// Set the line width with which to draw this axis.
	///
	/// @param lineWidth the width to draw the lines of the axis in pixels
	/// @throws IllegalArgumentException when the line width is less than 1.0f
	public void setLineWidth(float lineWidth) {
		if (lineWidth < 1f) {
			throw new IllegalArgumentException("Axis line width must be greater than 1.0f");
		}
		this.lineWidth = lineWidth;
	}

	/// Set the length of the lines used to draw this axis object.
	///
	/// @param axisLength the desired axis line length
	/// @throws IllegalArgumentException when the axis length is less than 0.0f
	public void setAxisLength(float axisLength) {
		if (axisLength < 0f) {
			throw new IllegalArgumentException("Axis length must be greater than 0.0f");
		}
		axisData = new float[]{
				// x,    y,    z,    r,    g,    b,    a
				0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,       // Origin vertex - red
				axisLength, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // +X vertex     - red
				0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,       // Origin vertex - green
				0.0f, axisLength, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // +Y vertex     - green
				0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,       // Origin vertex - blue
				0.0f, 0.0f, axisLength, 0.0f, 0.0f, 1.0f, 1.0f  // +Z vertex     - blue
		};
	}

	/// draw a 3D axis which consists of three drawn lines, one each in the +x, +y and +z directions.
	public void draw(Matrix4f modelViewProjectionMatrix) {
		SHADER_PROGRAM.use();

		glBindVertexArray(VAO_ID); // bind VAO
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID); // bind VBO

		VERTEX_BUFFER.put(axisData);
		VERTEX_BUFFER.flip();
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		glUniformMatrix4fv(SHADER_PROGRAM.getUniformLocation("mvpMatrix"), false, modelViewProjectionMatrix.get(MVP_MATRIX_BUFFER));

		glGetFloatv(GL_LINE_WIDTH, LINE_WIDTH_BUFFER); // store current GL_LINE_WIDTH

		glLineWidth(lineWidth);
		glDrawArrays(GL_LINES, 0, NUM_VERTICES); // draw the axis lines

		glLineWidth(LINE_WIDTH_BUFFER.get(0)); // restore previous GL_LINE_WIDTH

		// Unbind from VBO & VAO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		SHADER_PROGRAM.disable();
	}

	/// Method to draw an axis at each bone in a structure.
	///
	/// The X axis is red, the Y axis is green and the Z axis (along which the axis is aligned with the bone) is blue.
	///
	/// @param structure        the FabrikStructure3D to draw
	/// @param viewMatrix       the view matrix, typically extracted from the camera
	/// @param projectionMatrix the projection matrix, typically extracted from the OpenGLWindow
	public void draw(FabrikStructure3f structure, Matrix4fc viewMatrix, Matrix4fc projectionMatrix) {
		for (FabrikChain3f chain : structure.getChains()) {
			for (FabrikBone3f bone : chain.getBones()) {
				Matrix4f modelMatrix = m.identity().set(bone.getOrientation()).setTranslation(bone.getStartLocation()).mul(CORRECTION);
				Matrix4f modelViewProjectionMatrix = mvp.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
				draw(modelViewProjectionMatrix);
			}
		}
	}

}
