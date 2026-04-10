package cz.cvut.fel.omo.hw.functions.statistics;

import cz.cvut.fel.omo.hw.functions.data.model.AbroadResults;
import cz.cvut.fel.omo.hw.functions.data.model.Candidates;
import cz.cvut.fel.omo.hw.functions.data.model.Continent;
import cz.cvut.fel.omo.hw.functions.data.model.Country;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtils;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtilsImpl;
import cz.cvut.fel.omo.hw.functions.data.model.Vote;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AbroadStatisticsImpl implements AbroadStatistics {

    private final CompletableFuture<AbroadResults> abroadResults;
    private final CandidateUtils candidateUtils;

    public AbroadStatisticsImpl(CompletableFuture<AbroadResults> abroadResults,
                                CompletableFuture<Candidates> candidates) {
        this.abroadResults = abroadResults;
        this.candidateUtils = new CandidateUtilsImpl(candidates);
    }

    @Override
    public String getNameOfCountryWithTheHighestNonValidVotesRatio() {
        return abroadResults
                .thenApply(ar ->
                        Optional.ofNullable(ar.getAbroad()).stream()
                                .flatMap(ab -> Optional.ofNullable(ab.getContinents()).stream())
                                .flatMap(List::stream)               // Continent
                                .flatMap(cont -> Optional.ofNullable(cont.getCountries()).stream())
                                .flatMap(List::stream)               // Country
                                .map(country -> new AbstractMap.SimpleEntry<>(
                                        country.getName(),
                                        ratioOfInvalid(country)
                                ))
                                .max(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
                                .map(AbstractMap.SimpleEntry::getKey)
                                .orElse("Unknown")
                )
                .join();
    }

    // Усе inline, без локальних submitted/invalid
    private double ratioOfInvalid(Country country) {
        return Optional.ofNullable(country.getVoterTurnout()).map(vt ->
                vt.getNumberOfSubmittedVotingEnvelopes() == 0
                        ? 0.0
                        : (double)(vt.getNumberOfSubmittedVotingEnvelopes()
                        - vt.getNumberOfValidVotes())
                        / vt.getNumberOfSubmittedVotingEnvelopes()
        ).orElse(0.0);
    }

    @Override
    public Map<String, List<String>> getCandidateVictoryCountryMap() {
        return abroadResults
                .thenApply(ar ->
                        Optional.ofNullable(ar.getAbroad()).stream()
                                .flatMap(ab -> Optional.ofNullable(ab.getContinents()).stream())
                                .flatMap(List::stream)  // Continent
                                .flatMap(cont -> Optional.ofNullable(cont.getCountries()).stream())
                                .flatMap(List::stream)  // Country
                                .flatMap(country -> {
                                    List<Integer> winnerIds = getWinnersInCountry(country);
                                    return winnerIds.stream().map(winnerId -> new AbstractMap.SimpleEntry<>(
                                            candidateUtils.getCandidateFullName(winnerId).orElse("Unknown"),
                                            country.getName()
                                    ));
                                })
                                .collect(Collectors.groupingBy(
                                        AbstractMap.SimpleEntry::getKey,
                                        Collectors.mapping(
                                                AbstractMap.SimpleEntry::getValue,
                                                Collectors.toList()
                                        )
                                ))
                )
                .thenApply(result -> Optional.ofNullable(result).orElse(Map.of()))
                .join();
    }

    // Теж без локальних змінних
    private List<Integer> getWinnersInCountry(Country country) {
        return Optional.ofNullable(country.getVotes()).stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        Vote::getCandidateId,
                        Vote::getVotes,
                        Integer::sum
                ))
                .entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .orElse(List.of())
                .stream()
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public Map<String, Integer> getContinentRegisteredVoterCountMap() {
        return abroadResults
                .thenApply(ar ->
                        Optional.ofNullable(ar.getAbroad()).stream()
                                .flatMap(ab -> Optional.ofNullable(ab.getContinents()).stream())
                                .flatMap(List::stream)  // Continent
                                .collect(Collectors.toMap(
                                        Continent::getName,
                                        this::countRegisteredVotersInContinent
                                ))
                )
                .thenApply(result -> Optional.ofNullable(result).orElse(Map.of()))
                .join();
    }

    private int countRegisteredVotersInContinent(Continent continent) {
        return Optional.ofNullable(continent.getCountries()).stream()
                .flatMap(List::stream)
                .mapToInt(c -> c.getVoterTurnout().getNumberOfRegisteredVoters())
                .sum();
    }

    @Override
    public String getNameOfCountryWithMostRegisteredVoters() {
        return abroadResults
                .thenApply(ar ->
                        Optional.ofNullable(ar.getAbroad()).stream()
                                .flatMap(ab -> Optional.ofNullable(ab.getContinents()).stream())
                                .flatMap(List::stream)
                                .flatMap(cont -> Optional.ofNullable(cont.getCountries()).stream())
                                .flatMap(List::stream)
                                .max(Comparator.comparingInt(
                                        c -> c.getVoterTurnout().getNumberOfRegisteredVoters()
                                ))
                                .map(Country::getName)
                                // Якщо нічого не знайшли — Unknown
                                .orElse("Unknown")
                )
                .join();
    }
}
