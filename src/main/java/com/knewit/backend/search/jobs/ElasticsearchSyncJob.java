package com.knewit.backend.search.jobs;

import com.knewit.backend.search.entity.SearchIndexSyncEvent;
import com.knewit.backend.search.repository.SearchIndexSyncEventRepository;
import com.knewit.backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncJob {

    @Autowired private SearchIndexSyncEventRepository outboxRepository;
    @Autowired private SearchService searchService;

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        try {
            List<SearchIndexSyncEvent> events = outboxRepository.findPendingEvents(Instant.now());
            if (events.isEmpty()) {
                return;
            }
            log.debug("Found {} pending search index sync events to process", events.size());
            for (SearchIndexSyncEvent event : events) {
                try {
                    searchService.processSyncEvent(event);
                    event.setStatus("PROCESSED");
                    event.setProcessedAt(Instant.now());
                    event.setUpdatedAt(Instant.now());
                    outboxRepository.save(event);
                } catch (Exception e) {
                    log.error("Failed to process sync event: {}", event.getId(), e);
                    event.setAttemptCount(event.getAttemptCount() + 1);
                    event.setUpdatedAt(Instant.now());
                    if (event.getAttemptCount() >= 5) {
                        event.setStatus("FAILED");
                    } else {
                        event.setNextAttemptAt(Instant.now().plusSeconds(60L * event.getAttemptCount()));
                    }
                    outboxRepository.save(event);
                }
            }
        } catch (Exception e) {
            log.error("Error processing outbox events", e);
        }
    }
}
