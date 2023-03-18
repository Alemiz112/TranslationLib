package eu.mizerak.alemiz.translationlib.service.utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collection;

public class DataCollectionTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }

        TypeAdapter<Collection<T>> adapter = (TypeAdapter<Collection<T>>) gson.getDelegateAdapter(this, typeToken);
        return new Adapter(adapter);
    }

    @RequiredArgsConstructor
    private static class Adapter<E> extends TypeAdapter<Collection<E>> {
        private final TypeAdapter<Collection<E>> adapter;

        @Override
        public void write(JsonWriter out, Collection<E> collection) throws IOException {
            this.adapter.write(out, collection);
        }

        @Override
        public Collection<E> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            if (in.peek() != JsonToken.BEGIN_OBJECT) {
                return this.adapter.read(in);
            }

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                if (name.equals("data")) {
                    return this.adapter.read(in);
                }
            }
            in.endObject();
            return null;
        }
    }
}
