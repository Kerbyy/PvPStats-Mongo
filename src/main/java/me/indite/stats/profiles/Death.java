package me.indite.stats.profiles;

import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public class Death {
    @Getter private UUID killer;
    @Getter private UUID killed;
    @Getter private Long date;

    public Death(UUID killer, UUID killed, Long date) {
        this.killer = killer;
        this.killed = killed;
        this.date = date;
    }

    public Document toDocument() {
        final Document doc = new Document();
        doc.append("killer", this.getKiller().toString());
        doc.append("killed", this.getKilled().toString());
        doc.append("date", this.getDate());
        return doc;
    }
}
