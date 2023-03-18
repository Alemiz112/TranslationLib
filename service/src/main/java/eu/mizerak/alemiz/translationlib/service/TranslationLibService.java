package eu.mizerak.alemiz.translationlib.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.mizerak.alemiz.translationlib.common.gson.LocaleSerializer;
import eu.mizerak.alemiz.translationlib.common.structure.RestStatus;
import eu.mizerak.alemiz.translationlib.service.access.AccessRole;
import eu.mizerak.alemiz.translationlib.service.manager.TermsManager;
import eu.mizerak.alemiz.translationlib.service.scrappers.TranslationDataScrapper;
import eu.mizerak.alemiz.translationlib.service.utils.Configuration;
import eu.mizerak.alemiz.translationlib.service.utils.gson.BundledDataSerializer;
import eu.mizerak.alemiz.translationlib.service.utils.gson.DataCollectionTypeAdapterFactory;
import eu.mizerak.alemiz.translationlib.service.utils.gson.GsonMapper;
import io.avaje.http.api.WebRoutes;
import io.avaje.http.client.HttpException;
import io.avaje.inject.BeanScope;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.security.RouteRole;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class TranslationLibService {

    public static void main(String[] args) throws Exception {
        log.info("Loading configuration...");

        Configuration configuration = loadConfiguration();

        try {
            new TranslationLibService(configuration);
        } catch (Exception e) {
            log.error("Exception was caught! Shutting down...", e);
        }
    }

    public static Configuration loadConfiguration() throws IOException {
        Path path = Paths.get("service.properties");
        if (!Files.isRegularFile(path)) {
            try (InputStream inputStream = TranslationLibService.class.getClassLoader().getResourceAsStream("service.properties");
                 OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                byte[] bytes = new byte[8192];
                int read;
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
        }

        try (InputStream stream = Files.newInputStream(path)) {
            Properties properties = new Properties();
            properties.load(stream);
            return Configuration.parse(properties);
        }
    }

    private final Configuration configuration;

    private final Javalin server;
    private final Gson gson;

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    private final TranslationDataScrapper scrapper;
    private final TermsManager termsManager;

    public TranslationLibService(Configuration configuration) {
        this.configuration = configuration;

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(configuration.getMongoUrl()))
                .applyToConnectionPoolSettings(builder -> builder
                        .minSize(2)
                        .maxSize(configuration.getMaxMongoPoolSize()))
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS))
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.mongoDatabase = mongoClient.getDatabase(configuration.getMongoDatabase());

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Locale.class, new LocaleSerializer())
                .registerTypeAdapterFactory(new DataCollectionTypeAdapterFactory())
                .registerTypeAdapterFactory(new BundledDataSerializer())
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        BeanScope scope = BeanScope.builder()
                .bean(TranslationLibService.class, this)
                .bean(Configuration.class, configuration)
                .bean(MongoClient.class, this.mongoClient)
                .bean(MongoDatabase.class, this.mongoDatabase)
                .bean(Gson.class, this.gson)
                .build();

        Optional<TranslationDataScrapper> optional = scope.getOptional(TranslationDataScrapper.class, configuration.getScrapperName());
        this.scrapper = optional.orElseThrow(() -> new IllegalArgumentException("Unknown scrapper name" + configuration.getScrapperName()));

        log.info("Using {} scrapper", this.scrapper.getClass().getSimpleName());

        // Init terms manager
        this.termsManager = scope.get(TermsManager.class);
        this.termsManager.onInit();

        // Setup Http server
        this.server = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.accessManager(this::accessManager);
            config.jsonMapper(new GsonMapper(this.gson));
        });

        // Init default routes
        this.server.get("/", ctx -> ctx.result("Hello World"));
        this.server.routes(() -> scope.list(WebRoutes.class).forEach(WebRoutes::registerRoutes));

        this.server.exception(HttpException.class, (e, ctx) -> {
            // Handle general http exceptions
            ctx.json(RestStatus.create(RestStatus.Status.ERROR, e.getClass().getSimpleName(), e.getMessage() + ": " + e.bodyAsString()));
            log.error("Exception in route {}: status={} msg={}", ctx.url(), e.statusCode(), e.bodyAsString(), e);
        });

        this.server.exception(Exception.class, (e, ctx) -> {
            ctx.json(RestStatus.create(e));
            log.error("Exception in route {}", ctx.url(), e);
        });

        // Start server
        this.server.start(configuration.getHttpHost(), configuration.getHttpPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            scope.close();
        }));
    }

    private void accessManager(Handler handler, Context ctx, Set<? extends RouteRole> routeRoles) throws Exception {
        String authToken = ctx.header("auth");
        AccessRole role;
        if (authToken == null || authToken.trim().isEmpty() || !this.configuration.getAccessTokens().contains(authToken)) {
            role = AccessRole.PUBLIC;
        } else {
            role = AccessRole.PRIVATE;
        }

        if (routeRoles.isEmpty() || routeRoles.contains(role)) {
            handler.handle(ctx);
        } else {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Unauthorized");
        }
    }
}
