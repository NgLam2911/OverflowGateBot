package OverflowGateBot.main;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import arc.util.Log;

import static OverflowGateBot.OverflowGateBot.*;

public class DatabaseHandler {

    public enum DATABASE {
        USER,
        GUILD,
        LOG,
        DAILY,
        MINDUSTRY
    }

    public enum LOG_TYPE {
        MESSAGE,
        DATABASE,
        USER,
        MESSAGE_DELETED
    }

    private static String DATABASE_URL = System.getenv("DATABASE_URL");
    private static ConnectionString connectionString = new ConnectionString(DATABASE_URL);
    private static MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .serverApi(ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build())
            .build();

    private static MongoClient mongoClient = MongoClients.create(settings);

    private static CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    private static CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
            fromProviders(pojoCodecProvider));

    private static ConcurrentHashMap<String, MongoDatabase> database = new ConcurrentHashMap<String, MongoDatabase>();

    public DatabaseHandler() {
        Log.info("Database handler up");
    }

    public static MongoDatabase getDatabase(DATABASE name) {
        if (database.containsKey(name.name()))
            return database.get(name.name());
        MongoDatabase db = mongoClient.getDatabase(name.name()).withCodecRegistry(pojoCodecRegistry);
        database.put(name.name(), db);
        return db;
    }

    // Check if collection exists
    public static boolean collectionExists(MongoDatabase database, final String collectionName) {
        MongoCursor<String> collectionNames = database.listCollectionNames().iterator();
        String name;
        while (collectionNames.hasNext()) {
            name = collectionNames.next();
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean collectionExists(DATABASE databaseName, final String collectionName) {
        return collectionExists(getDatabase(databaseName), collectionName);
    }

    public static void createCollection(DATABASE databaseName, final String collectionName) {
        getDatabase(databaseName).createCollection(collectionName);
        log(LOG_TYPE.DATABASE, new Document().append("CREATE GUILD", collectionName));
    }

    public static void log(LOG_TYPE log, Document content) {
        networkHandler.run("LOG " + log.name(), 0, () -> {
            MongoDatabase logDatabase = getDatabase(DATABASE.LOG);
            // Create collection if it doesn't exist
            if (!collectionExists(logDatabase, log.name()))
                logDatabase.createCollection(log.name());

            MongoCollection<Document> collection = logDatabase.getCollection(log.name(), Document.class);
            // Insert log message
            collection.insertOne(content.append(TIME_INSERT_STRING, new BsonDateTime(System.currentTimeMillis())));

            // Delete old log message if storage is full
            while (collection.estimatedDocumentCount() > MAX_LOG_COUNT) {
                collection.deleteOne(new Document());
            }
        });
    }

}