package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.annotations.SerializedName;
import eu.mizerak.alemiz.translationlib.service.utils.gson.JsonFieldAdapter;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
@JsonFieldAdapter(value = "data", mode = JsonFieldAdapter.Mode.READ)
public class TraduoraTerm {
    private String id;
    @SerializedName("value")
    private String key;
    private Collection<Label> labels = new ArrayList<>();

    @Data
    public static class Label {
        @SerializedName("value")
        private String name;
        private String color;
    }
}
