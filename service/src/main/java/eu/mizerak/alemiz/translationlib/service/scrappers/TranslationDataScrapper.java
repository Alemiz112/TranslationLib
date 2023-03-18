package eu.mizerak.alemiz.translationlib.service.scrappers;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import io.avaje.http.client.HttpException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface TranslationDataScrapper {

    Collection<TranslationTerm> resolveTerms() throws HttpException;

    Collection<TranslationTerm> resolveTerms(@NotNull Set<String> locales) throws HttpException;

    String addTerm(@NotNull TranslationTerm term, boolean replace) throws HttpException;

    void removeTerm(@NotNull TranslationTerm term);
}
