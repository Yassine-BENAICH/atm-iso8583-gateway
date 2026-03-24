package com.atm.iso8583.service;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.model.TrafficEvent;
import com.atm.iso8583.model.TrafficMetrics;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MonitoringService {

    private static final int MAX_RECENT_EVENTS = 500;

    private final Instant startedAt = Instant.now();

    private final LongAdder totalTransactions = new LongAdder();
    private final LongAdder successfulTransactions = new LongAdder();
    private final LongAdder declinedTransactions = new LongAdder();
    private final LongAdder errorTransactions = new LongAdder();
    private final LongAdder totalLatencyMs = new LongAdder();

    private final AtomicLong minLatencyMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxLatencyMs = new AtomicLong(0L);

    private final Deque<TrafficEvent> recentEvents = new ArrayDeque<>();
    private final Deque<Long> recentLatenciesMs = new ArrayDeque<>();
    private final ReentrantLock historyLock = new ReentrantLock();

    public void recordTransaction(Iso8583Request request, Iso8583Response response, long latencyMs) {
        long safeLatency = Math.max(0L, latencyMs);
        Outcome outcome = classifyOutcome(response);

        totalTransactions.increment();
        totalLatencyMs.add(safeLatency);
        minLatencyMs.accumulateAndGet(safeLatency, Math::min);
        maxLatencyMs.accumulateAndGet(safeLatency, Math::max);

        switch (outcome) {
            case SUCCESS -> successfulTransactions.increment();
            case DECLINED -> declinedTransactions.increment();
            case ERROR -> errorTransactions.increment();
        }

        String errorMessage = outcome == Outcome.ERROR && response != null ? normalize(response.getErrorMessage()) : null;
        TrafficEvent event = TrafficEvent.builder()
                .timestamp(Instant.now())
                .requestMti(request != null ? request.getMti() : null)
                .responseMti(response != null ? response.getMti() : null)
                .stan(firstNonBlank(response != null ? response.getStan() : null, request != null ? request.getStan() : null))
                .responseCode(response != null ? response.getResponseCode() : null)
                .status(outcome.name())
                .latencyMs(safeLatency)
                .errorMessage(errorMessage)
                .build();

        historyLock.lock();
        try {
            recentEvents.addLast(event);
            recentLatenciesMs.addLast(safeLatency);
            trimToLimit(recentEvents);
            trimToLimit(recentLatenciesMs);
        } finally {
            historyLock.unlock();
        }
    }

    public TrafficMetrics getCurrentMetrics() {
        long total = totalTransactions.sum();
        long successful = successfulTransactions.sum();
        long declined = declinedTransactions.sum();
        long errors = errorTransactions.sum();
        long totalLatency = totalLatencyMs.sum();

        List<TrafficEvent> eventSnapshot;
        List<Long> latencySnapshot;

        historyLock.lock();
        try {
            eventSnapshot = new ArrayList<>(recentEvents);
            latencySnapshot = new ArrayList<>(recentLatenciesMs);
        } finally {
            historyLock.unlock();
        }

        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(60);
        long transactionsLastMinute = eventSnapshot.stream()
                .filter(event -> event.getTimestamp() != null && event.getTimestamp().isAfter(oneMinuteAgo))
                .count();

        return TrafficMetrics.builder()
                .startedAt(startedAt)
                .snapshotAt(now)
                .uptimeSeconds(Math.max(0L, now.getEpochSecond() - startedAt.getEpochSecond()))
                .totalTransactions(total)
                .successfulTransactions(successful)
                .declinedTransactions(declined)
                .errorTransactions(errors)
                .successRatePercent(total == 0 ? 0D : round2((successful * 100D) / total))
                .averageLatencyMs(total == 0 ? 0D : round2(totalLatency / (double) total))
                .p95LatencyMs(percentile(latencySnapshot, 95D))
                .minLatencyMs(total == 0 ? 0L : minLatencyMs.get())
                .maxLatencyMs(total == 0 ? 0L : maxLatencyMs.get())
                .transactionsLastMinute(transactionsLastMinute)
                .recentEventsCount(eventSnapshot.size())
                .build();
    }

    public List<TrafficEvent> getRecentEvents(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_RECENT_EVENTS));

        historyLock.lock();
        try {
            List<TrafficEvent> snapshot = new ArrayList<>(recentEvents);
            Collections.reverse(snapshot); // newest first

            if (snapshot.size() <= safeLimit) {
                return snapshot;
            }
            return new ArrayList<>(snapshot.subList(0, safeLimit));
        } finally {
            historyLock.unlock();
        }
    }

    private Outcome classifyOutcome(Iso8583Response response) {
        if (response == null) {
            return Outcome.ERROR;
        }

        String status = normalize(response.getStatus());
        String responseCode = normalize(response.getResponseCode());

        if ("ERROR".equalsIgnoreCase(status)) {
            return Outcome.ERROR;
        }
        if ("SUCCESS".equalsIgnoreCase(status) || "00".equals(responseCode) || "85".equals(responseCode)) {
            return Outcome.SUCCESS;
        }
        if ("DECLINED".equalsIgnoreCase(status) || responseCode != null) {
            return Outcome.DECLINED;
        }
        return Outcome.ERROR;
    }

    private <T> void trimToLimit(Deque<T> deque) {
        while (deque.size() > MAX_RECENT_EVENTS) {
            deque.pollFirst();
        }
    }

    private long percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0L;
        }

        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((percentile / 100D) * sorted.size()) - 1;
        int clampedIndex = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(clampedIndex);
    }

    private String firstNonBlank(String first, String second) {
        String firstNormalized = normalize(first);
        if (firstNormalized != null) {
            return firstNormalized;
        }
        return normalize(second);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private double round2(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private enum Outcome {
        SUCCESS,
        DECLINED,
        ERROR
    }
}
