package a2;

import tage.*;
import tage.shapes.*;

public class ManualHome extends ManualObject {
    private float[] vertices = new float[] {
        // Form two triangles for each wall to make a rectangle
        // Wall 1 (Left Side)
        -2.0f, 0.0f, 2.0f,  0.0f, 0.0f, 0.0f,  0.0f, 4.0f, 0.0f, // Triangle 1
        -2.0f, 0.0f, 2.0f,  0.0f, 4.0f, 0.0f, -2.0f, 4.0f, 2.0f, // Triangle 2

        // Wall 2 (Right Side)
        0.0f, 0.0f, 0.0f,  2.0f, 0.0f, 2.0f,  2.0f, 4.0f, 2.0f,  // Triangle 3
        0.0f, 0.0f, 0.0f,  2.0f, 4.0f, 2.0f,  0.0f, 4.0f, 0.0f   // Triangle 4
    };
    
    private float[] texcoords = new float[] {
        // Wall 1
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        // Wall 2
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
    };

    private float[] normals = new float[] {
        // Wall 1 Normals (Pointing inward/right)
        1.0f, 0.0f, 1.0f,  1.0f, 0.0f, 1.0f,  1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f,  1.0f, 0.0f, 1.0f,  1.0f, 0.0f, 1.0f,
        // Wall 2 Normals (Pointing inward/left)
        -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f
    };

    public ManualHome() {
        super();
        setNumVertices(12); // 4 triangles * 3 vertices each
        setVertices(vertices);
        setTexCoords(texcoords);
        setNormals(normals);
    }
}