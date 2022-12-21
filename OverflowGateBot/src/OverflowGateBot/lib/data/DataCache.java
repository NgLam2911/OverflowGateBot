package OverflowGateBot.lib.data;

public class DataCache {

    private int time = 0;
    private final int ALIVE_TIME = 30;

    public DataCache() {
        time = ALIVE_TIME;
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
        time = ALIVE_TIME;
    }
}
