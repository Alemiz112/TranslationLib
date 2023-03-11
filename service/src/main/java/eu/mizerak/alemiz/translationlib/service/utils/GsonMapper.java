package eu.mizerak.alemiz.translationlib.service.utils;

import com.google.gson.Gson;
import io.javalin.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

@RequiredArgsConstructor
public class GsonMapper implements JsonMapper {
    private final Gson gson;

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return this.gson.toJson(obj, type);
    }

    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        return this.gson.fromJson(json, targetType);
    }
}
