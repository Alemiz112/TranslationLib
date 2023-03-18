package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.JsonObject;
import io.avaje.http.api.*;

import java.util.List;

@Client
@Path("/api/v1/projects")
public interface TraduoraClientApi {

    @Get("{projectId}/translations")
    JsonObject resolveLocales(String projectId);

    @Get("{projectId}/terms")
    List<TraduoraTerm> resolveTerms(String projectId);

    @Get("{projectId}/exports")
    JsonObject resolveTranslations(String projectId, @QueryParam String locale, @QueryParam String format);

    @Post("{projectId}/terms")
    TraduoraTerm addTerm(TraduoraTerm body, String projectId);

    @Delete("{projectId}/terms/{termId}")
    void deleteTerm(String projectId, String termId);

    @Patch("{projectId}/translations/{localeCode}")
    void updateTermTranslation(AddTranslationRequest request, String projectId, String localeCode);
}