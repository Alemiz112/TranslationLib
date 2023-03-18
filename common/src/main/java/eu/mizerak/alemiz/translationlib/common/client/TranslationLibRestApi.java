package eu.mizerak.alemiz.translationlib.common.client;

import eu.mizerak.alemiz.translationlib.common.structure.RestStatus;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import io.avaje.http.api.*;

import java.util.Collection;

@Client
@Path("/api")
public interface TranslationLibRestApi {

    @Get("translations/export/{tag}")
    TranslationTerm[] exportTerms(String tag);

    @Get("translations/tags")
    Collection<String> translationTags();

    @Post("translations/refresh")
    RestStatus refresh();

    @Post("translations/clean")
    RestStatus clean();

    @Post("translations/update")
    RestStatus termUpdate(TranslationTerm term, @QueryParam boolean replace);
}
