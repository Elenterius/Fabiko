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

import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/// Refactored copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Grid.java">Grid</a>
public class GridDrawHelper {

	private static final String VERTEX_SHADER_SOURCE = """
			#version 330
			in vec3 vertexLocation; // Incoming vertex attribute
			uniform mat4 mvpMatrix; // Combined Model/View/Projection matrix
			void main(void) {
				gl_Position = mvpMatrix * vec4(vertexLocation, 1.0); // Project our geometry
			}
			""";

	private static final String FRAGMENT_SHADER_SOURCE = """
			#version 330
			out vec4 vOutputColour; // Outgoing colour value
			void main() {
				vOutputColour = vec4(1.0); // Draw our pixel in white with full opacity
			}
			""";

	private static final int VERTEX_COMPONENTS = 3; // x, y, z

	private static final ShaderProgram SHADER_PROGRAM;

	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer LINE_WIDTH_BUFFER = BufferUtils.createFloatBuffer(1);

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addUniform("mvpMatrix");
	}

	private final int gridVaoId;            // The Vertex Array Object ID which holds our shader attributes
	private final int numVerts;             // How many vertices in this grid?
	private final int vertexBufferId;       // The id of the vertex buffer containing the grid vertex data
	private final FloatBuffer vertexBuffer; // Vertex buffer to hold the gridArray vertex data

	/// Width is along +/- X-axis, depth is along +/- Z-axis, height is the location of
	/// the grid on the Y-axis, numDivisions is how many lines to draw across each axis.
	///
	/// @param width        The width of the grid in world-space units.
	/// @param depth        The depth of the grid in world-space units.
	/// @param height       The location of the grid on the Y-axis.
	/// @param numDivisions The number of divisions in the grid.
	public GridDrawHelper(float width, float depth, float height, int numDivisions) {
		// Calculate how many vertices our grid will consist of.
		// Multiplied by 2 because 2 vertices per line, and times 2 again because our
		// grid is composed of -z to +z lines, as well as -x to +x lines. Add +4 to
		// the total for the final two lines to 'close off' the grid.
		numVerts = (numDivisions * 2 * 2) + 4;

		float[] gridArray = createGridArray(width, depth, height, numDivisions);

		// Transfer the data into the vertex float buffer
		vertexBuffer = BufferUtils.createFloatBuffer(gridArray.length);
		vertexBuffer.put(gridArray);
		vertexBuffer.flip();

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----
		// Note: The grid VAO cannot be static because we may have multiple grids of various sizes.

		// Get an ID for the Vertex Array Object (VAO) and bind to it
		gridVaoId = glGenVertexArrays();
		glBindVertexArray(gridVaoId);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		vertexBufferId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"),  // 0, Vertex attribute index
				3,  // Number of normal components per vertex
				GL_FLOAT,  // Data type
				false,  // Normalised?
				0,  // Stride
				0); // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attribute at this location
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));

		// Unbind our Vertex Array object - all the buffer and attribute settings above will be associated with our VAO!
		glBindVertexArray(0);
	}

	private float[] createGridArray(float width, float depth, float height, int numDivisions) {
		float[] gridArray = new float[numVerts * VERTEX_COMPONENTS];

		// For a grid of width and depth, the extent goes from -halfWidth to +halfWidth,
		// and -halfDepth to +halfDepth.
		float halfWidth = width / 2.0f;
		float halfDepth = depth / 2.0f;

		// How far we move our vertex locations each time through the loop
		float xStep = width / numDivisions;
		float zStep = depth / numDivisions;

		// Starting locations
		float xLoc = -halfWidth;
		float zLoc = -halfDepth;

		// Split the vertices into half for -z to +z lines, and half for -x to +x
		int halfNumVerts = numVerts / 2;

		// Our counter will keep track of the index of the float value we're working on
		int counter = 0;

		// Near to far lines
		// Note: Step by 2 because we're setting two vertices each time through the loop
		for (int i = 0; i < halfNumVerts; i += 2) {
			// Far vertex
			gridArray[counter++] = xLoc;       // x
			gridArray[counter++] = height;     // y
			gridArray[counter++] = -halfDepth; // z

			gridArray[counter++] = xLoc;      // x
			gridArray[counter++] = height;    // y
			gridArray[counter++] = halfDepth; // z

			// Move across on the x-axis
			xLoc += xStep;
		}

		// Left to right lines
		// Note: Step by 2 because we're setting two vertices each time through the loop
		for (int i = halfNumVerts; i < numVerts; i += 2) {
			// Left vertex
			gridArray[counter++] = -halfWidth; // x
			gridArray[counter++] = height;     // y
			gridArray[counter++] = zLoc;       // z

			// Right vertex
			gridArray[counter++] = halfWidth; // x
			gridArray[counter++] = height;    // y
			gridArray[counter++] = zLoc;      // z

			// Move across on the z-axis
			zLoc += zStep;
		}

		return gridArray;
	}

	public void draw(Matrix4fc modelViewProjection) {
		SHADER_PROGRAM.use();
		glBindVertexArray(gridVaoId); // bind VAO

		// flip needs to be omitted with JOML, see https://github.com/JOML-CI/JOML/wiki/Common-Pitfalls#methods-taking-a-nio-buffer-do-not-modify-the-buffer-position
		SHADER_PROGRAM.setUniformValueMatrix4f("mvpMatrix", modelViewProjection.get(MVP_MATRIX_BUFFER));

		glGetFloatv(GL_LINE_WIDTH, LINE_WIDTH_BUFFER); // store current GL_LINE_WIDTH

		glLineWidth(1f); // set the GL_LINE_WIDTH
		glDrawArrays(GL_LINES, 0, numVerts); // draw the grid as lines

		glLineWidth(LINE_WIDTH_BUFFER.get(0)); // restore previous value GL_LINE_WIDTH

		glBindVertexArray(0); // unbind VAO
		SHADER_PROGRAM.disable();
	}

}
