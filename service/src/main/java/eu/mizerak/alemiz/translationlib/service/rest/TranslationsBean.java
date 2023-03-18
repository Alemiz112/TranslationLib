package eu.mizerak.alemiz.translationlib.service.rest;

import eu.mizerak.alemiz.translationlib.common.structure.RestStatus;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.access.AccessRole;
import eu.mizerak.alemiz.translationlib.service.access.Roles;
import eu.mizerak.alemiz.translationlib.service.manager.TermsManager;
import io.avaje.http.api.*;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@Controller
@Roles(AccessRole.PRIVATE)
@Path("/api/translations/")
public class TranslationsBean {

    @Inject
    TermsManager termsManager;

    @Get("export/{tag}")
    Collection<TranslationTerm> exportByTag(String tag) {
        return this.termsManager.getTranslationTerms(tag);
    }

    @Get("tags")
    Collection<String> translationTags() {
        return this.termsManager.getTags();
    }

    @Post("refresh")
    RestStatus refreshTerms() {
        this.termsManager.refreshTask();
        return RestStatus.OK;
    }

    @Post("clean")
    RestStatus clean() {
        this.termsManager.cleanOldTerms();
        return RestStatus.OK;
    }

    @Post("update")
    RestStatus termUpdate(TranslationTerm term, @QueryParam boolean replace) {
        this.termsManager.requestTermUpdate(term, replace);
        return RestStatus.OK;
    }
}
