package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.JsonObject;
import io.avaje.http.client.AuthToken;
import io.avaje.http.client.AuthTokenProvider;
import io.avaje.http.client.HttpClientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class TraduoraAuthProvider implements AuthTokenProvider {
    private final String baseUrl;
    private final String clientSecret;
    private final String clientId;

    @Override
    public AuthToken obtainToken(HttpClientRequest tokenRequest) {
        JsonObject response = tokenRequest
                .url(this.baseUrl + "/api/v1/auth/token")
                .header("content-type", "application/json")
                .body(this.requestBody())
                .POST()
                .bean(JsonObject.class);


        String expiresIn = response.get("expires_in").getAsString();
        int duration = Integer.parseInt(expiresIn.substring(0, expiresIn.length() - 1));
        Instant validUntil = Instant.now().plusSeconds(duration).minusSeconds(60);

        log.info("Successfully authenticated to Traduora and obtained access token. Token will be refreshed in {} minutes!", duration / 60);

        return AuthToken.of(response.get("access_token").getAsString(), validUntil);
    }

    private JsonObject requestBody() {
        JsonObject body = new JsonObject();
        body.addProperty("grant_type", "client_credentials");
        body.addProperty("client_id", this.clientId);
        body.addProperty("client_secret", this.clientSecret);
        return body;
    }
}
