package OverflowGateBot.lib.data;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import static OverflowGateBot.OverflowGateBot.*;

public class SchematicData {

    public String id;
    public String data;

    public SchematicData() {
    }

    public SchematicData(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public void update() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, SCHEMATIC_DATA_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, SCHEMATIC_DATA_COLLECTION);
        }
        MongoCollection<SchematicData> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY).getCollection(
                SCHEMATIC_DATA_COLLECTION,
                SchematicData.class);

        Bson filter = new Document().append("_id", this.id);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }
}
