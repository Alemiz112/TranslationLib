package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AddTranslationRequest {
    private final String termId;
    @SerializedName("value")
    private final String text;
}
