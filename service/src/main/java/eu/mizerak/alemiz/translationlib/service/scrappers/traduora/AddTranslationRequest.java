package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AddTranslationRequest {
    @SerializedName("termId")
    private final String termId;
    private final String value;
}
