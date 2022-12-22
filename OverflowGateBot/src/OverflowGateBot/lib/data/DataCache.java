package OverflowGateBot.lib.data;

public class DataCache {

    private int time = 0;
    private int aliveTime = 0;

    public DataCache(int aliveTime) {
        this.time = aliveTime;
        this.aliveTime = aliveTime;
    }

    public void alive(int n) {
        time -= n;
    }

    public boolean isAlive() {
        return time > 0;
    }

    public boolean isAlive(int n) {
        alive(n);
        return isAlive();
    }

    public void reset() {
        time = aliveTime;
    }
}
