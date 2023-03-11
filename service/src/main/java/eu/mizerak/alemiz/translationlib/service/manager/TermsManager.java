package eu.mizerak.alemiz.translationlib.service.manager;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.TranslationLibService;
import eu.mizerak.alemiz.translationlib.service.repository.TermsRepository;
import eu.mizerak.alemiz.translationlib.service.utils.Scheduler;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class TermsManager {

    @Inject
    TranslationLibService loader;

    @Inject
    TermsRepository repository;

    private final Map<String, TranslationTerm> terms = new ConcurrentHashMap<>();
    private final Map<String, TermsGroup> groups = new ConcurrentHashMap<>();


    public void onInit() {
        Collection<TranslationTerm> terms = this.repository.getAllTerms(null);
        for (TranslationTerm term : terms) {
            this.terms.put(term.getKey(), term);
            for (String tag : term.getTags()) {
                this.groups.computeIfAbsent(tag, TermsGroup::new).addTerm(term);
            }
        }

        log.info("Database currently contains {} known terms", this.terms.size());
        Scheduler.DEFAULT.scheduleAtFixedRate(this::refreshTask, 10, 60 * 5, TimeUnit.MINUTES);
    }

    public void refreshTask() {
        try {
            Collection<TranslationTerm> terms = this.loader.getScrapper().resolveTerms();
            List<TranslationTerm> newTerms = new ArrayList<>();
            int updatedTerms = 0;

            for (TranslationTerm term : terms) {
                TranslationTerm oldTerm = this.terms.put(term.getKey(), term);
                if (oldTerm == null) {
                    newTerms.add(term);
                } else if (!oldTerm.equals(term)) {
                    updatedTerms++;
                    Scheduler.DEFAULT.execute(() -> this.repository.addTerm(term)); // update existing
                }

                for (String tag : term.getTags()) {
                    this.groups.computeIfAbsent(tag, TermsGroup::new).addTerm(term);
                }
            }

            // Upload all new terms together
            Scheduler.DEFAULT.execute(() -> this.repository.addTerms(newTerms));

            log.info("Terms refreshed successfully! New terms: {} Updated terms: {}", newTerms.size(), updatedTerms);
        } catch (Exception e) {
            log.error("Failed to reload terms", e);
        }
    }

    public Collection<TranslationTerm> getTranslationTerms(String tag) {
        TermsGroup termsGroup = this.groups.get(tag);
        return termsGroup == null ? Collections.emptyList() : termsGroup.getTerms();
    }

    public Collection<String> getTags() {
        return this.groups.keySet();
    }

}
