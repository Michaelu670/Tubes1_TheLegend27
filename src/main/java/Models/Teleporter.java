package Models;

import java.util.UUID;

public class Teleporter {
    private UUID id;
    private boolean isTeleporterActive;
    private int heading;
    private Position targetPos;
    public int cntNotFound;
    public int cntOutput;

    public Teleporter() {
        id = null;
        isTeleporterActive = false;
        heading = -1;
        targetPos = new Position();
        cntNotFound = 0;
        cntOutput = 0;
    }

    public void setTeleporterActive(boolean value) {
        this.isTeleporterActive = value;
    }

    public boolean isTeleporterActive() {
        return isTeleporterActive;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getHeading() {
        return heading;
    }

    public void setTargetPosition(Position targetPos) {
        this.targetPos = targetPos;
    }

    public Position getTargetPosition() {
        return targetPos;
    }

    public void reset() {
        id = null;
        isTeleporterActive = false;
        heading = -1;
        targetPos = new Position();
        cntNotFound = 0;
        cntOutput = 0;
    }
}
