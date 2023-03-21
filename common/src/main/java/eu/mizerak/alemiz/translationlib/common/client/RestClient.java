package eu.mizerak.alemiz.translationlib.common.client;

import eu.mizerak.alemiz.translationlib.common.TranslationLibLoader;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.RequestIntercept;
import io.avaje.http.client.SimpleRetryHandler;
import io.avaje.http.client.gson.GsonBodyAdapter;
import lombok.RequiredArgsConstructor;

import static eu.mizerak.alemiz.translationlib.common.TranslationLibLoader.GSON;

@RequiredArgsConstructor
public class RestClient {
    private final TranslationLibLoader loader;
    private final HttpClient client;
    private TranslationLibRestApi api;

    public RestClient(TranslationLibLoader loader, String address, String token) {
        this.loader = loader;

        this.client = HttpClient.builder()
                .baseUrl(address)
                .bodyAdapter(new GsonBodyAdapter(GSON))
                .requestIntercept(new RequestIntercept() {
                    @Override
                    public void beforeRequest(HttpClientRequest request) {
                        request.header("auth", token);
                    }
                })
                .retryHandler(new SimpleRetryHandler(3, 1000, 0))
                .build();
    }

    public TranslationLibRestApi getApi() {
        if (this.api == null) {
            this.api = this.client.create(TranslationLibRestApi.class);
        }
        return this.api;
    }
}
