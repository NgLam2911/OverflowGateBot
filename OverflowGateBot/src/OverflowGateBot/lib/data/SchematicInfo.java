package OverflowGateBot.lib.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import static OverflowGateBot.OverflowGateBot.*;

public class SchematicInfo {

    public String id;
    public String authorId;
    public int star;
    public List<String> tag = new ArrayList<String>();

    public SchematicInfo() {
    }

    public SchematicInfo(String id, String authorId, List<String> tag) {
        this.id = id;
        this.authorId = authorId;
        this.tag = tag;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorId() {
        return this.authorId;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getStar() {
        return this.star;
    }

    public void setTag(List<String> tags) {
        this.tag = tags;
    }

    public List<String> getTag() {
        return this.tag;
    }

    public void update() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, SCHEMATIC_INFO_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, SCHEMATIC_INFO_COLLECTION);
        }
        MongoCollection<SchematicInfo> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY).getCollection(
                SCHEMATIC_INFO_COLLECTION,
                SchematicInfo.class);

        Document filter = new Document().append("_id", this.id);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }
}
