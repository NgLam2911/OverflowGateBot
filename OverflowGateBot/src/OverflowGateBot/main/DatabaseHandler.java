package OverflowGateBot.main;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class DatabaseHandler {

    private final String URL = "mongodb+srv://bot1:imthebot1@cluster0.7omeswq.mongodb.net/?retryWrites=true&w=majority";
    public static MongoDatabase database;

    public DatabaseHandler() {
        try {
            ConnectionString connectionString = new ConnectionString(URL);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    .build();

            // Set up pojo
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                    fromProviders(pojoCodecProvider));

            MongoClient mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase("Cluster0").withCodecRegistry(pojoCodecRegistry);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean collectionExists(final String collectionName) {
        MongoIterable<String> collectionNames = database.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }
}
