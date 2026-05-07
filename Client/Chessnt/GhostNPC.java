package Chessnt;

import java.util.UUID;
import tage.*;
import org.joml.*;

public class GhostNPC extends GameObject {
    private UUID id; // Using UUID to match your networking style

    public GhostNPC(UUID id, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.id = id;
        this.setLocalLocation(p);
    }

    public void setSize(boolean big) {
        if (!big) {
            this.setLocalScale((new Matrix4f()).scaling(0.5f));
        } else {
            this.setLocalScale((new Matrix4f()).scaling(1.0f));
        }
    }

    public void setPosition(Vector3f p) {
        this.setLocalLocation(p);
    }

    public UUID getID() { return id; }
}