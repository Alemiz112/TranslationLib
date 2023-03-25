package eu.mizerak.alemiz.translationlib.service.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.utils.Configuration;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

@Slf4j
@Singleton
public class TermsRepository {
    @Inject
    Configuration config;

    @Inject
    Gson gson;

    Connection connection;

    @PostConstruct
    void onStartup() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.config.getTermsDatabase());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to open connection to SQLite database", e);
        }

        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS `terms` (`key` varchar(200) NOT NULL PRIMARY KEY, `internal_id` varchar(200) DEFAULT NULL, " +
                    "`tags` MEDIUMTEXT DEFAULT NULL, `translations` LONGTEXT NOT NULL);");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to init terms database");
        }
    }

    @PreDestroy
    void onShutdown() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    public void addTerm(@NotNull TranslationTerm term) {
        try {
            if (this.isTermCreated(term.getKey())) {
                this.updateTerm(term);
            } else {
                this.insertTerm(term);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update translation term " + term.getKey(), e);
        }
    }

    private void updateTerm(@NotNull TranslationTerm term) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("UPDATE `terms` SET `internal_id` = ?, `tags` = ?, `translations` = ? WHERE `key` = ?")) {
            if (term.getInternalId() == null || term.getInternalId().trim().isEmpty()) {
                statement.setNull(1, Types.NULL);
            } else {
                statement.setString(1, term.getInternalId());
            }

            if (term.getTags() == null || term.getTags().isEmpty()) {
                statement.setNull(2, Types.NULL);
            } else {
                statement.setString(2, String.join(",", term.getTags()));
            }
            statement.setString(3, gson.toJson(term.getTranslations()));
            statement.setString(4, term.getKey());

            statement.execute();
        }
    }

    private void insertTerm(@NotNull TranslationTerm term) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO `terms` (`key`, `internal_id`, `tags`, `translations`) VALUES (?, ?, ?, ?);")) {
            statement.setString(1, term.getKey());
            if (term.getInternalId() == null || term.getInternalId().trim().isEmpty()) {
                statement.setNull(2, Types.NULL);
            } else {
                statement.setString(2, term.getInternalId());
            }

            if (term.getTags() == null || term.getTags().isEmpty()) {
                statement.setNull(3, Types.NULL);
            } else {
                statement.setString(3, String.join(",", term.getTags()));
            }
            statement.setString(4, gson.toJson(term.getTranslations()));

            statement.execute();
        }
    }

    public void addTerms(Collection<TranslationTerm> terms) {
        for (TranslationTerm term : terms) {
            this.addTerm(term);
        }
    }

    public void removeTerm(@NotNull TranslationTerm term) {
        try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM `terms` WHERE `key` = ?;")) {
            statement.setString(1, term.getKey());
            statement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete term " + term.getKey(), e);
        }
    }

    public boolean isTermCreated(@NotNull String key) {
        try (PreparedStatement queryStatement = this.connection.prepareStatement("SELECT `key` FROM `terms` WHERE `key` = ?")) {
            queryStatement.setString(1, key);
            try (ResultSet query = queryStatement.executeQuery()) {
                return query.next() && query.getRow() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable check if term exists " + key, e);
        }
    }

    public Collection<TranslationTerm> getAllTerms() {
        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `terms`;")) {
                List<TranslationTerm> terms = new ArrayList<>();
                while (resultSet.next()) {
                    TranslationTerm term = new TranslationTerm();
                    term.setKey(resultSet.getString("key"));
                    term.setInternalId(resultSet.getString("internal_id"));

                    String tags = resultSet.getString("tags");
                    if (tags != null && !tags.trim().isEmpty()) {
                        term.getTags().addAll(Arrays.asList(tags.split(",")));
                    }

                    String translations = resultSet.getString("translations");
                    Type type = new TypeToken<Map<Locale, String>>(){}.getType();
                    term.getTranslations().putAll(gson.fromJson(translations, type));

                    terms.add(term);
                }
                return terms;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load terms from database", e);
        }
    }
}
