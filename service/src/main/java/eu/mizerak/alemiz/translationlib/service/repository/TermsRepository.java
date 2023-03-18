package eu.mizerak.alemiz.translationlib.service.repository;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Singleton
public class TermsRepository {
    public static final String COLLECTION = "terms";

    @Inject
    MongoDatabase database;

    @Inject
    Gson gson;

    public void addTerm(@NotNull TranslationTerm term) {
        Bson filter = Filters.eq("key", term.getKey());
        this.database.getCollection(COLLECTION).replaceOne(filter, createDocument(term),
                new ReplaceOptions().upsert(true));
    }

    public void addTerms(Collection<TranslationTerm> terms) {
        List<Document> documents = new ArrayList<>();
        for (TranslationTerm term : terms) {
            documents.add(createDocument(term));
        }
        this.database.getCollection(COLLECTION).insertMany(documents);
    }

    public void removeTerm(@NotNull TranslationTerm term) {
        this.database.getCollection(COLLECTION).deleteOne(Filters.eq("key", term.getKey()));
    }

    public boolean isTermCreated(@NotNull System key) {
        return this.database.getCollection(COLLECTION).countDocuments(Filters.eq("key", key)) > 1;
    }

    public Collection<TranslationTerm> getAllTerms(@Nullable Bson filter) {
        FindIterable<Document> documents;
        if (filter == null) {
            documents = this.database.getCollection(COLLECTION).find();
        } else {
            documents = this.database.getCollection(COLLECTION).find(filter);
        }

        List<TranslationTerm> terms = new ArrayList<>();
        try (MongoCursor<Document> cursor = documents.cursor()) {
            while (cursor.hasNext()) {
                terms.add(createTerm(cursor.next()));
            }
        }
        return terms;
    }

    private Document createDocument(TranslationTerm term) {
        return Document.parse(gson.toJson(term));
    }

    private TranslationTerm createTerm(Document document) {
        return gson.fromJson(document.toJson(), TranslationTerm.class);
    }
}
