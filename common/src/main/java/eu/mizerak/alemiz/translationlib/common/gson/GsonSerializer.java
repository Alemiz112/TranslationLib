package eu.mizerak.alemiz.translationlib.common.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public abstract class GsonSerializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {
}