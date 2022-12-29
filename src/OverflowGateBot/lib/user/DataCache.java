package OverflowGateBot.lib.user;

public class DataCache {

    private final int ALIVE_LIMIT;
    private final int UPDATE_LIMIT;

    private int time = 0;
    private int updateTime = 0;

    public DataCache(final int aliveLimit, final int updateLimit) {
        this.UPDATE_LIMIT = updateLimit;
        this.ALIVE_LIMIT = aliveLimit;
        this.time = ALIVE_LIMIT;
    }

    public boolean isAlive() {
        return time > 0;
    }

    public boolean isAlive(int n) {
        this.time -= n;
        return isAlive();
    }

    public void reset() {
        time = ALIVE_LIMIT;
    }

    public void kill() {
        this.time = 0;
    }

    public void update(int n) {
        updateTime += n;
        if (updateTime >= UPDATE_LIMIT) {
            updateTime = 0;
            update();
        }
    }

    public void update() {
    }
}
