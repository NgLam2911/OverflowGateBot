package OverflowGateBot.MiniGame;

import static OverflowGateBot.OverflowGateBot.*;

import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONObject;

import OverflowGateBot.misc.JSONHandler;
import OverflowGateBot.misc.JSONHandler.*;

public class GuessTheNumberHandler {

    private int number;
    public int money;
    private Random rng = new Random();

    public GuessTheNumberHandler() {
        load();
    }

    public void restart() {
        this.number = rng.nextInt(1000);
    }

    public int onGuess(int _number, int point) {
        if ((int) _number == this.number) {
            int reward = this.money;
            this.money = 0;
            restart();
            return reward;
        } else {
            this.money += point;
            return -1;
        }
    }

    public void save() {
        try {
            JSONHandler jsonHandler = new JSONHandler();
            JSONWriter writer = jsonHandler.new JSONWriter(guessTheNumberPath);
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.put("number", number);
            map.put("money", money);
            writer.write((new JSONObject(map)).toJSONString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            JSONHandler jsonHandler = new JSONHandler();

            // Load daily data
            JSONData reader = (jsonHandler.new JSONReader(guessTheNumberPath)).read();
            int _number = reader.readInt("number", -1);
            if (_number == -1)
                restart();
            this.money = reader.readInt("money", 0);
            if (this.money < 0)
                this.money = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
