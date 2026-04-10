package cz.cvut.fel.omo.hw.functions.statistics;

import cz.cvut.fel.omo.hw.functions.data.model.Candidates;
import cz.cvut.fel.omo.hw.functions.data.model.Candidate;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CandidateStatisticsImpl implements CandidateStatistics {

    private final CompletableFuture<Candidates> candidates;

    @Override
    public double getAverageAge() {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .mapToInt(Candidate::getAge)
                        .average()
                        .orElse(0.0)
        ).join();
    }

    @Override
    public String getOldestCandidateName() {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .max(Comparator.comparingInt(Candidate::getAge))
                        .map(Candidate::getFullName)
                        .orElse("Unknown")
        ).join();
    }

    @Override
    public Map<String, Integer> getCandidateAgeMap() {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .collect(Collectors.toMap(
                                Candidate::getFullName,
                                Candidate::getAge
                        ))
        ).join();
    }
}
