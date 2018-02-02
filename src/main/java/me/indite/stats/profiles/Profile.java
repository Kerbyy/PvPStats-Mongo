package me.indite.stats.profiles;

import com.mongodb.BasicDBList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.indite.stats.Stats;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Profile {
    @Getter @Setter private final static HashMap<String, Profile> profiles = new HashMap();

    @Getter @Setter private String name;
    @Getter @Setter private UUID uuid;
    @Getter @Setter private ArrayList<Kill> kills;
    @Getter @Setter private ArrayList<Death> deaths;

    //==================================

    public Profile(@NonNull UUID uuid, @NonNull boolean cache) {
        this.uuid = uuid;
        this.kills = new ArrayList<>();
        this.deaths = new ArrayList<>();

        Stats.getInstance().getMongoBackend().loadProfile(this);

        if (cache) {
            profiles.put(uuid.toString(), this);
        }
    }

    public Profile(@NonNull final UUID uuid) {
        this(uuid, false);
    }

    public Document toDocument() {
        final Document doc = new Document();
        final BasicDBList kills = new BasicDBList();
        final BasicDBList deaths = new BasicDBList();

        doc.append("uuid", this.getUuid().toString());
        doc.append("name", this.getName());

        for (Kill kill : this.kills)
            kills.add(kill.toDocument());

        for (Death death : this.deaths)
            deaths.add(death.toDocument());

        doc.append("kills", kills);
        doc.append("deaths", deaths);

        return doc;
    }

    public void load(@NonNull final Document doc) {
        this.setName(doc.getString("name"));
        for (final Object object : (ArrayList) doc.get("kills")) {
            final Document killsdoc = (Document) object;
            final Kill kill = new Kill(UUID.fromString(killsdoc.getString("killer")), UUID.fromString(killsdoc.getString("killed")), killsdoc.getLong("date"));
            this.getKills().add(kill);
        }

        for (final Object object : (ArrayList) doc.get("deaths")) {
            final Document deathsdoc = (Document) object;
            UUID uuid = null;

            if (deathsdoc.get("killer") != null)
                uuid = UUID.fromString(deathsdoc.getString("killer"));

            final Death death = new Death(uuid, UUID.fromString(deathsdoc.getString("killed")), deathsdoc.getLong("date"));
            this.getDeaths().add(death);
        }
    }

    public void save() {
        Stats.getInstance().getMongoBackend().saveProfile(this);
    }

    public static Profile getByUuid(@NonNull final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player != null)
            return getByPlayer(player);
        else
            return new Profile(uuid);
    }

    public static Profile getByPlayer(@NonNull final Player player) {
        if (profiles.containsKey(player.getUniqueId().toString()))
            return profiles.get(player.getUniqueId().toString());

        return new Profile(player.getUniqueId());
    }
}
