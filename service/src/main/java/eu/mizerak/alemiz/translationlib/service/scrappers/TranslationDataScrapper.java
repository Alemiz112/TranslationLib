package eu.mizerak.alemiz.translationlib.service.scrappers;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;

import java.util.Collection;
import java.util.Set;

public interface TranslationDataScrapper {

    Collection<TranslationTerm> resolveTerms() throws Exception;

    Collection<TranslationTerm> resolveTerms(Set<String> locales) throws Exception;
}
