package cz.cvut.fel.omo.hw.functions.utils;

import cz.cvut.fel.omo.hw.functions.data.model.Candidate;
import cz.cvut.fel.omo.hw.functions.data.model.Candidates;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Log
@RequiredArgsConstructor
public class CandidateUtilsImpl implements CandidateUtils {

    private final CompletableFuture<Candidates> candidates;

    @Override
    public Optional<String> getCandidateFullName(int id) {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .map(Candidate::getFullName)
        ).join();
    }

    @Override
    public Optional<Integer> getCandidateAge(int id) {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .map(Candidate::getAge)
        ).join();
    }

    @Override
    public Optional<Candidate> getCandidate(int id) {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
        ).join();
    }

    @Override
    public <T> Optional<T> getCandidateAttribute(int id, Function<Candidate, T> mappingFunction) {
        return candidates.thenApply(cs ->
                cs.getCandidatesList().stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .map(mappingFunction)
        ).join();
    }
}
