package eu.mizerak.alemiz.translationlib.service.manager;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TermsGroup {
    private final String groupName;
    private final Map<String, TranslationTerm> terms = new HashMap<>();

    public TermsGroup(String groupName) {
        this.groupName = groupName;
    }

    public void addTerm(TranslationTerm term) {
        this.terms.put(term.getKey(), term);
    }

    public void removeTerm(String key) {
        this.terms.remove(key);
    }

    public Collection<TranslationTerm> getTerms() {
        return this.terms.values();
    }
}
