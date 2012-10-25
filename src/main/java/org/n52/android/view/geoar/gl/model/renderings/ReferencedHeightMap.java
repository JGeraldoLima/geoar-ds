/**
 * Copyright 2012 52�North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.android.view.geoar.gl.model.renderings;

import org.n52.android.view.geoar.gl.model.RenderNode;
import org.n52.android.view.geoar.gl.model.primitives.HeightMap;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class ReferencedHeightMap extends RenderNode {

    static final int SIZE_PER_SIDE = 64;
    static final float MIN_POSITION = -10f;
    static final float POSITION_RANGE = 20f;

    @Override
    protected void onPreRender() {
	GLES20.glLineWidth(2f);

    }

    public ReferencedHeightMap() {
//	HeightMap map = new HeightMap();
//	addChild(map);
//	setPosition(new float[]{0f,0.00001f,0f});
	int xLength = 64;
	int yLength = 64;

	final int floatsPerVertex = 3;
	final int floatsPerColor = 4;
	final int floatsPerNormal = 3;

	final float[] vertices = new float[xLength * yLength * floatsPerVertex];
	final float[] colors = new float[xLength * yLength * floatsPerColor];
	final float[] normals = new float[xLength * yLength * floatsPerNormal];

	int offset = 0;
	int normalOffSet = 0;
	int colorOffset = 0;

	for (int y = 0; y < yLength; y++) {
	    for (int x = 0; x < xLength; x++) {
		final float xRatio = x / (float) (xLength - 1);

		// Build our heightmap from the top down, so that our
		// triangles are counter-clockwise.
		final float yRatio = 1f - (y / (float) (yLength - 1));

		final float xPosition = MIN_POSITION + (xRatio * POSITION_RANGE);
		final float yPosition = MIN_POSITION + (yRatio * POSITION_RANGE);

		vertices[offset++] = xPosition;
		vertices[offset++] = ((xPosition * xPosition) + (yPosition * yPosition)) / 20f;
		vertices[offset++] = yPosition;

		final float xSlope = (2 * xPosition) / 10f;
		final float ySlope = (2 * yPosition) / 10f;

		// Calculate the normal using the cross product of the
		// slopes.
		final float[] planeVectorX = { 1f, 0f, xSlope };
		final float[] planeVectorY = { 0f, 1f, ySlope };
		final float[] normalVector = {
			(planeVectorX[1] * planeVectorY[2]) - (planeVectorX[2] * planeVectorY[1]),
			(planeVectorX[2] * planeVectorY[0]) - (planeVectorX[0] * planeVectorY[2]),
			(planeVectorX[0] * planeVectorY[1]) - (planeVectorX[1] * planeVectorY[0]) };

		// Normalize the normal
		final float length = Matrix.length(normalVector[0], normalVector[1], normalVector[2]);

		normals[normalOffSet++] = normalVector[0] / length;
		normals[normalOffSet++] = normalVector[1] / length;
		normals[normalOffSet++] = normalVector[2] / length;

		colors[colorOffset++] = 0f;
		colors[colorOffset++] = 0f;
		colors[colorOffset++] = 0.5f;
		colors[colorOffset++] = 0.5f;
	    }
	}

	// Now build the index data
	final int numStripsRequired = yLength - 1;
	final int numDegensRequired = 2 * (numStripsRequired - 1);
	final int verticesPerStrip = 2 * xLength - 1;

	final int[] heightMapIndexData = new int[(verticesPerStrip * verticesPerStrip)];

	offset = 0;
	
	for(int y = 0; y < yLength; y++){
	    for(int x = 0; x < xLength - 1; x++){
		heightMapIndexData[offset++] = (int) ((y * yLength) + x);
		heightMapIndexData[offset++] = (int) ((y * yLength) + x + 1);
	    }
	}
	
	for(int x = 0; x < xLength; x++){
	    for(int y = 0; y < yLength - 1; y++){
		heightMapIndexData[offset++] = (int) ((y * yLength) + x);
		heightMapIndexData[offset++] = (int) (((y+1) * yLength) + x);
	    }
	}
	
	drawingMode =  GLES20.GL_LINES;
	setRenderObjectives(vertices, colors, normals, heightMapIndexData);
    }

}
