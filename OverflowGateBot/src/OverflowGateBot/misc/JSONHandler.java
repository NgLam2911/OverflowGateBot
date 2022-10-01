package OverflowGateBot.misc;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONHandler {

    public class JSONWriter {
        Writer writer;
        boolean first = true;
        String data = "";

        public JSONWriter(String filePath) throws IOException {
            if (!filePath.endsWith(".json"))
                filePath += ".json";
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
        }

        public void append(String key, String value) {
            if (this.first == false) {
                this.data += ",";
            }
            this.first = false;
            this.data += '\"' + key + '\"' + ":" + value;
        }

        public void write() throws IOException {
            writer.write("{" + this.data + "}");
            writer.flush();
        }

        public void write(String _data) throws IOException {
            writer.write(_data);
            writer.flush();
        }
    }


    public class JSONReader {

        JSONObject data;
        JSONParser jsonParser = new JSONParser();

        public JSONReader(String filePath) throws IOException, ParseException {
            if (!filePath.endsWith(".json"))
                filePath += ".json";
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();

            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            if (reader.ready())
                data = (JSONObject) jsonParser.parse(reader);
        }

        public JSONData read() {
            JSONData jsonData = new JSONData();
            jsonData.data = data;
            return jsonData;
        }
    }


    public class JSONData {
        public JSONObject data;

        public JSONData() {
        }

        public int readInt(String key) {
            Object value = data.get(key);
            return value == null ? 0 : Integer.valueOf((value).toString());
        }

        public long readLong(String key) {
            Object value = data.get(key);
            return value == null ? 0 : Long.valueOf((value).toString());
        }

        public String readString(String key) {
            if (data.containsKey(key)) {
                Object value = data.get(key);
                return value == null ? "" : (value).toString();
            }
            return "";
        }

        public JSONData readJSON(String key) {
            JSONObject newData = (JSONObject) this.data.get(key);
            JSONData newJson = new JSONData();
            newJson.data = newData;
            return newJson;
        }

        public JSONArray readJSONArray(String key) {
            JSONArray newData = (JSONArray) this.data.get(key);
            return newData == null ? null : newData;
        }

        public int size() {
            return (data == null) ? 0 : data.size();
        }
    }
}
