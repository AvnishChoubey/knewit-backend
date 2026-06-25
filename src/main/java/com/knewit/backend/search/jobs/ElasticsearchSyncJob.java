package com.knewit.backend.search.jobs;

import com.knewit.backend.search.entity.SearchIndexSyncEvent;
import com.knewit.backend.search.repository.SearchIndexSyncEventRepository;
import com.knewit.backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncJob {

    @Autowired private SearchIndexSyncEventRepository outboxRepository;
    @Autowired private SearchService searchService;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processOutbox() {
        try {
            List<SearchIndexSyncEvent> events =
                    outboxRepository.findTop100ByStatusAndNextAttemptAtLessThanEqual(
                            "PENDING",
                            LocalDateTime.now()
                    );

            if (events.isEmpty()) {
                return;
            }

            log.info("Processing {} outbox events", events.size());

            List<SearchIndexSyncEvent> toUpdate = new ArrayList<>();

            for(SearchIndexSyncEvent event : events) {

                try {
                    searchService.processSyncEvent(event);

                    event.setStatus("PROCESSED");
                    event.setProcessedAt(LocalDateTime.now());

                } catch (Exception e) {

                    log.error("Failed event {}", event.getId(), e);

                    event.setAttemptCount(event.getAttemptCount() + 1);

                    if (event.getAttemptCount() >= 5) {
                        event.setStatus("FAILED");
                    } else {
                        event.setNextAttemptAt(
                                LocalDateTime.now().plusSeconds(60L * event.getAttemptCount())
                        );
                    }
                }

                toUpdate.add(event);
            }

            outboxRepository.saveAll(toUpdate);

        } catch (Exception e) {
            log.error("Outbox batch processing failed", e);
        }
    }
}
