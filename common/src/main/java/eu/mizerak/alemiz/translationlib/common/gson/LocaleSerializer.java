package eu.mizerak.alemiz.translationlib.common.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Locale;

public class LocaleSerializer extends GsonSerializer<Locale>  {

    @Override
    public Locale deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String[] split = json.getAsString().split("_");
        if (split.length > 1) {
            return new Locale(split[0], split[1]);
        } else {
            return new Locale(split[0]);
        }
    }

    @Override
    public JsonElement serialize(Locale src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getLanguage() + "_" + src.getCountry());
    }
}
