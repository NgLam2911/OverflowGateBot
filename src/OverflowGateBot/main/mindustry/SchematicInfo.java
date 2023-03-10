package OverflowGateBot.main.mindustry;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.BotConfig;
import OverflowGateBot.main.handler.DatabaseHandler;
import OverflowGateBot.main.handler.DatabaseHandler.DATABASE;
import OverflowGateBot.main.user.PenguinData;
import OverflowGateBot.main.user.StarData;

public class SchematicInfo {

    public String id;
    public String authorId;
    public List<String> tag = new ArrayList<String>();
    private boolean deleted = false;
    private int star = -1;
    private int penguin = -1;

    public SchematicInfo() {
    }

    public SchematicInfo(String id, String authorId, List<String> tag) {
        this.id = id;
        this.authorId = authorId;
        this.tag = tag;
    }

    @Override
    protected void finalize() {
        update();
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

    public void setTag(List<String> tags) {
        this.tag = tags;
    }

    public List<String> getTag() {
        return this.tag;
    }

    public long getStar() {
        if (this.star != -1)
            return this.star;
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.STAR, this.id)) {
            return 0;
        }
        MongoCollection<StarData> collection = DatabaseHandler.getDatabase(DATABASE.STAR)
                .getCollection(this.id, StarData.class);

        return collection.countDocuments();

    }

    public boolean addStar(@Nonnull String userId) {
        if (!DatabaseHandler.collectionExists(DATABASE.STAR, this.id)) {
            DatabaseHandler.createCollection(DATABASE.STAR, this.id);
        }
        MongoCollection<StarData> collection = DatabaseHandler.getDatabase(DATABASE.STAR)
                .getCollection(this.id, StarData.class);

        Document filter = new Document().append("_id", this.id);
        if (collection.find(filter).first() != null)
            return false;
        collection.insertOne(new StarData(userId));
        this.star += 1;
        return true;
    }

    public long getPenguin() {
        if (this.penguin != -1)
            return this.penguin;
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.PENGUIN, this.id)) {
            return 0;
        }
        MongoCollection<PenguinData> collection = DatabaseHandler.getDatabase(DATABASE.PENGUIN)
                .getCollection(this.id, PenguinData.class);

        return collection.countDocuments();

    }

    public boolean addPenguin(@Nonnull String userId) {
        if (!DatabaseHandler.collectionExists(DATABASE.PENGUIN, this.id)) {
            DatabaseHandler.createCollection(DATABASE.PENGUIN, this.id);
        }
        MongoCollection<PenguinData> collection = DatabaseHandler.getDatabase(DATABASE.PENGUIN)
                .getCollection(this.id, PenguinData.class);

        Document filter = new Document().append("_id", this.id);
        if (collection.find(filter).first() != null)
            return false;
        collection.insertOne(new PenguinData(userId));
        return true;
    }

    public void update() {
        if (deleted)
            return;
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_INFO_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_INFO_COLLECTION);
        }
        MongoCollection<SchematicInfo> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY)
                .getCollection(BotConfig.SCHEMATIC_INFO_COLLECTION, SchematicInfo.class);

        Document filter = new Document().append("_id", this.id);
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
