package eu.mizerak.alemiz.translationlib.service.rest;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.access.AccessRole;
import eu.mizerak.alemiz.translationlib.service.access.Roles;
import eu.mizerak.alemiz.translationlib.service.manager.TermsManager;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import jakarta.inject.Inject;

import java.util.Collection;

@Path("/api/translations")
@Controller
public class TranslationsBean {

    @Inject
    TermsManager termsManager;

    @Get("/export/{tag}")
    @Roles(AccessRole.PUBLIC)
    Collection<TranslationTerm> exportByTag(String tag) {
        return this.termsManager.getTranslationTerms(tag);
    }

    @Get("/tags")
    @Roles(AccessRole.PUBLIC)
    Collection<String> translationTags() {
        return this.termsManager.getTags();
    }
}
