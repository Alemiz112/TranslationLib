package eu.mizerak.alemiz.translationlib.service.utils.gson;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.SerializationDelegatingTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

public class BundledDataSerializer implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
        Class<? super T> rawType = targetType.getRawType();
        JsonFieldAdapter annotation = rawType.getAnnotation(JsonFieldAdapter.class);
        if (annotation == null) {
            return null;
        }

        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, targetType);
        return new Adapter<>(delegate, annotation);
    }

    @RequiredArgsConstructor
    private static class Adapter<T> extends SerializationDelegatingTypeAdapter<T> {
        private final TypeAdapter<T> delegate;
        private final JsonFieldAdapter annotation;

        @Override
        public T read(JsonReader in) throws IOException {
            JsonElement element = Streams.parse(in);
            if ((annotation.mode() == JsonFieldAdapter.Mode.ALL || annotation.mode() == JsonFieldAdapter.Mode.READ) && element.isJsonObject() && element.getAsJsonObject().has("data")) {
                return this.delegate.fromJsonTree(element.getAsJsonObject().get(annotation.value()));
            }
            return this.delegate.fromJsonTree(element);
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            if ((annotation.mode() == JsonFieldAdapter.Mode.ALL || annotation.mode() == JsonFieldAdapter.Mode.WRITE)) {
                out.beginObject();
                out.name(annotation.value());
                this.delegate.write(out, value);
                out.endObject();
            } else {
                this.delegate.write(out, value);
            }
        }

        @Override
        public TypeAdapter<T> getSerializationDelegate() {
            return this.delegate;
        }
    }
}
