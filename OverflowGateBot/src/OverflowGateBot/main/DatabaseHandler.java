package OverflowGateBot.main;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;

import static OverflowGateBot.OverflowGateBot.*;

public class DatabaseHandler {

    private static MongoClient mongoClient;

    public static MongoDatabase userDatabase;
    public static MongoDatabase guildDatabase;
    public static MongoDatabase logDatabase;

    public enum DATABASE {
        USER, GUILD, LOG
    }

    public enum LOG_TYPE {
        MESSAGE, DATABASE, USER
    }

    public DatabaseHandler() {

        ConnectionString connectionString = new ConnectionString(DATABASE_URL);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();

        mongoClient = MongoClients.create(settings);

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                fromProviders(pojoCodecProvider));

        guildDatabase = mongoClient.getDatabase(DATABASE.GUILD.name()).withCodecRegistry(pojoCodecRegistry);
        userDatabase = mongoClient.getDatabase(DATABASE.USER.name()).withCodecRegistry(pojoCodecRegistry);
        logDatabase = mongoClient.getDatabase(DATABASE.LOG.name()).withCodecRegistry(pojoCodecRegistry);

        System.out.println("Database handler up");
    }

    // Check if collection exists
    public static boolean collectionExists(MongoDatabase database, final String collectionName) {
        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    // TODO: Threading support

    public static void log(LOG_TYPE log, String content) {
        networkHandler.run(0, () -> {
            // Create collection if it doesn't exist
            if (!collectionExists(logDatabase, log.name()))
                logDatabase.createCollection(log.name(),
                        new CreateCollectionOptions().timeSeriesOptions(new TimeSeriesOptions(TIME_INSERT_STRING)));

            MongoCollection<Document> collection = logDatabase.getCollection(log.name(), Document.class);
            // Insert log message
            collection.insertOne(new Document().//
                    append("Content", content).//
                    append(TIME_INSERT_STRING, new BsonDateTime(System.currentTimeMillis())));

            // Delete old log message if storage is full
            while (collection.estimatedDocumentCount() > MAX_LOG_COUNT) {
                collection.deleteOne(new Document());
            }
        });
    }

}
