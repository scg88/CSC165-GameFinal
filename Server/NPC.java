public class NPC {
    double locationX, locationY, locationZ;
    double dir = 0.1;
    double size = 1.0;
    private float yAngle = 0.0f;

    private boolean isNearLastFrame = false;

    public double getSize() { 
        return size; 
    }

    public void setSize(double s) { 
        size = s; 
    }

    public float getYAngle() {
        return yAngle;
    }

    public void setYAngle(float angle) {
        this.yAngle = angle % 360.0f;
    }

    public void setNear(boolean near) {
        isNearLastFrame = near;
    }

    public boolean wasNear() {
        return isNearLastFrame;
    }

    public NPC() {
        locationX = 0.0; 
        locationY = 0.0; 
        locationZ = 0.0;
        dir = 0.1;
    }

    public void randomizeLocation(int seedX, int seedZ) {
        locationX = ((double)seedX)/4.0 - 5.0;
        locationY = 0;
        locationZ = -2;
    }

    public void updateLocation() {
        // Professor's example has it pacing back and forth
        // COMMENT OUT TO KEEP NPC STILL
        if (locationX > 10) dir = -0.1;
        if (locationX < -10) dir = 0.1;
        locationX += dir;
    }

    public double getX() { return locationX; }
    public double getY() { return locationY; }
    public double getZ() { return locationZ; }

    public void setLocation(double x, double y, double z) {
        locationX = x;
        locationY = y;
        locationZ = z;
    }
}