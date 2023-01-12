package OverflowGateBot.lib.mindustry;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.BotConfig;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

public class SchematicData {

    public String id;
    public String data;
    private boolean deleted = false;

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
        if (deleted)
            return;
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_DATA_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_DATA_COLLECTION);
        }
        MongoCollection<SchematicData> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY)
                .getCollection(BotConfig.SCHEMATIC_DATA_COLLECTION, SchematicData.class);

        Bson filter = new Document().append("_id", this.id);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }

    public void delete() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_INFO_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_INFO_COLLECTION);
        }
        MongoCollection<SchematicInfo> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY)
                .getCollection(BotConfig.SCHEMATIC_INFO_COLLECTION, SchematicInfo.class);

        Document filter = new Document().append("_id", this.id);
        collection.deleteOne(filter);
        this.deleted = true;
    }
}
