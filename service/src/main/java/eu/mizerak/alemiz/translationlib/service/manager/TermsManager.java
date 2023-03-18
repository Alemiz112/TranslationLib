package eu.mizerak.alemiz.translationlib.service.manager;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.TranslationLibService;
import eu.mizerak.alemiz.translationlib.service.repository.TermsRepository;
import eu.mizerak.alemiz.translationlib.service.utils.Scheduler;
import io.avaje.http.client.HttpException;
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
        Scheduler.DEFAULT.scheduleAtFixedRate(this::refreshTask, 1, 60 * 5, TimeUnit.SECONDS);
    }

    private void onTermUpdateRequested(TranslationTerm term) {
        // TODO: webhook
    }

    private void onTermUpdated(TranslationTerm term) {
        // TODO: webhook
    }

    private void onTermRemoved(TranslationTerm term) {
        // TODO: webhook
    }

    private void addTermInternal(TranslationTerm term) {
        this.terms.put(term.getKey(), term);
        for (String tag : term.getTags()) {
            this.groups.computeIfAbsent(tag, TermsGroup::new).addTerm(term);
        }
    }

    private void removeTermInternal(String key) {
        this.terms.remove(key);
        for (TermsGroup group : this.groups.values()) {
            group.removeTerm(key);
        }
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
                    this.onTermUpdated(term);
                }

                for (String tag : term.getTags()) {
                    this.groups.computeIfAbsent(tag, TermsGroup::new).addTerm(term);
                }
            }

            // Upload all new terms together
            Scheduler.DEFAULT.execute(() -> this.repository.addTerms(newTerms));

            log.info("Terms refreshed successfully! New terms: {} Updated terms: {}", newTerms.size(), updatedTerms);
        } catch (HttpException e) {
            log.error("Failed to reload terms: {}", e.bodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed to reload terms", e);
        }
    }

    public void cleanOldTerms() {
        try {
            Collection<TranslationTerm> terms = this.loader.getScrapper().resolveTerms();
            Map<String, TranslationTerm> termsMap = new HashMap<>();
            terms.forEach(term -> termsMap.put(term.getKey(), term));

            int count = 0;
            for (TranslationTerm term : this.terms.values()) {
                if (!termsMap.containsKey(term.getKey())) {
                    Scheduler.DEFAULT.execute(() -> this.repository.removeTerm(term));
                    this.terms.remove(term.getKey());
                    this.onTermRemoved(term);
                    count++;
                }
            }

            log.info("Removed {} old terms!", count);
        } catch (HttpException e) {
            log.error("Failed to remove old terms: {}", e.bodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed to remove old terms", e);
        }
    }

    public void requestTermUpdate(TranslationTerm term, boolean replace) {
        TranslationTerm oldTerm = this.terms.get(term.getKey());
        if (oldTerm != null) {
            if (!replace) {
                return;
            }
            term.setInternalId(oldTerm.getInternalId());
        }

        if (replace) {
            this.removeTermInternal(term.getKey());
        }

        String termId = this.loader.getScrapper().addTerm(term, replace);
        term.setInternalId(termId);

        if (replace) {
            this.addTermInternal(term);
        }
        this.onTermUpdated(term);
    }

    public Collection<TranslationTerm> getTranslationTerms(String tag) {
        TermsGroup termsGroup = this.groups.get(tag);
        return termsGroup == null ? Collections.emptyList() : termsGroup.getTerms();
    }

    public Collection<String> getTags() {
        return this.groups.keySet();
    }

}
