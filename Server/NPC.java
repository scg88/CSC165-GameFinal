public class NPC {
    double locationX, locationY, locationZ;
    private double xDir = 0.2;  // Speed along X
    private double zDir = 5.0;  // How far to jump in Z (one tile)
    private double targetZ = 0.0;
    private boolean isSteppingZ = false;
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
        xDir = 0.1;
    }

    public void randomizeLocation(int seedX, int seedZ) {
        locationX = ((double)seedX)/4.0 - 5.0;
        locationY = 0;
        locationZ = -2;
    }

    public void updateLocation() {
        if (!isSteppingZ) {
            // 1. Move across the X-axis
            locationX += xDir;

            // 2. When we hit the X boundary, switch to Z-stepping mode
            if (Math.abs(locationX) >= 20.0) {
                isSteppingZ = true;
                targetZ = locationZ + zDir;

                // 3. If the next Z row is out of bounds, reverse Z direction
                if (targetZ > 10 || targetZ < -10) {
                    zDir = -zDir;
                    targetZ = locationZ + zDir;
                }

                // Reverse X direction for the next row sweep
                xDir = -xDir;
            }
        } else {
            // 4. Move toward the next Z row
            if (locationZ < targetZ) locationZ += 0.1;
            else if (locationZ > targetZ) locationZ -= 0.1;

            // 5. If we arrived at the row, switch back to X-pacing mode
            if (Math.abs(locationZ - targetZ) < 0.1) {
                locationZ = targetZ; 
                isSteppingZ = false;
            }
        }
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