package me.indite.stats.backend;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.indite.stats.Stats;
import me.indite.stats.profiles.Profile;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.Collections;

import static com.mongodb.client.model.Filters.eq;

public class MongoBackend {
    @Getter @Setter private static MongoCollection<Document> profiles;
    @Getter @Setter private MongoClient mongo;

    @Getter @Setter private MongoDatabase db;

    public MongoBackend() {
        try {
            final ServerAddress address = new ServerAddress(Stats.getInstance().getConfig().getString("mongo.host"), Stats.getInstance().getConfig().getInt("mongo.port"));

            if (Stats.getInstance().getConfig().getBoolean("mongo.auth.enable")) {
                final MongoCredential credential = MongoCredential.createCredential(Stats.getInstance().getConfig().getString("mongo.auth.username"), Stats.getInstance().getConfig().getString("mongo.auth.authdb"), Stats.getInstance().getConfig().getString("mongo.auth.password").toCharArray());
                this.mongo = new MongoClient(address, Collections.singletonList(credential));
            } else {
                this.mongo = new MongoClient(address);
            }

            this.db = this.mongo.getDatabase(Stats.getInstance().getConfig().getString("mongo.database"));
            this.profiles = this.db.getCollection(Stats.getInstance().getConfig().getString("mongo.collection"));

            System.out.println("Mongo Driver successfully loaded.");
        } catch (Exception e) {
            System.out.println("Mongo Driver failed to load.");
            e.printStackTrace();
        }
    }

    public void close() {
        if (this.mongo != null)
            this.mongo.close();
    }

    //=========== Profiles  ===========\\
    public synchronized void createProfile(@NonNull final Profile profile) {
        Bukkit.getScheduler().runTaskAsynchronously(Stats.getInstance(), () -> {
            this.profiles.insertOne(profile.toDocument());
        });
    }

    public synchronized void saveProfile(@NonNull final Profile profile) {
        Bukkit.getScheduler().runTaskAsynchronously(Stats.getInstance(), () -> {
            this.saveProfileSync(profile);
        });
    }

    public synchronized void saveProfileSync(@NonNull final Profile profile) {
        final Document doc = profile.toDocument();
        this.profiles.findOneAndReplace(eq("uuid", profile.getUuid().toString()), doc);
    }

    public void loadProfile(@NonNull final Profile profile) {
        final Document doc = this.profiles.find(eq("uuid", profile.getUuid().toString())).first();

        if (doc != null) {
            profile.load(doc);
        } else {
            this.createProfile(profile);
        }
    }
}
