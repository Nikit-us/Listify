package com.tech.listify.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
public class HitCounterService {

    private final Map<String, LongAdder> urlHitCounters = new ConcurrentHashMap<>();

    public void incrementHit(String url) {
        urlHitCounters.computeIfAbsent(url, k -> new LongAdder()).increment();
    }

    public long getHitCount(String url) {
        LongAdder adder = urlHitCounters.get(url);
        return (adder != null) ? adder.sum() : 0;
    }

    public Map<String, Long> getAllHits() {
        return urlHitCounters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().sum()));
    }
}