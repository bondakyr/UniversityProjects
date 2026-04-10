package cz.cvut.fel.omo.hw.functions.statistics;

import cz.cvut.fel.omo.hw.functions.data.model.Candidates;
import cz.cvut.fel.omo.hw.functions.data.model.City;
import cz.cvut.fel.omo.hw.functions.data.model.RegionResults;
import cz.cvut.fel.omo.hw.functions.data.model.Vote;
import cz.cvut.fel.omo.hw.functions.data.model.Region;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtils;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtilsImpl;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.Comparator;
import java.util.stream.Collectors;

public class NationalStatisticsImpl implements NationalStatistics {

    private final CompletableFuture<List<RegionResults>> regionResults;
    private final CandidateUtils candidateUtils;

    public NationalStatisticsImpl(
            CompletableFuture<List<RegionResults>> regionResults,
            CompletableFuture<Candidates> candidates
    ) {
        this.regionResults = regionResults;
        this.candidateUtils = new CandidateUtilsImpl(candidates);
    }

    @Override
    public String getNameOfCityWithTheLowestVoterTurnout() {
        return regionResults.thenApply(rrList ->
                rrList.stream()
                        .flatMap(rr -> rr.getRegions().stream())
                        .flatMap(region -> region.getDistricts().stream())
                        .flatMap(district -> district.getCities().stream())
                        .map(city -> new AbstractMap.SimpleEntry<>(
                                city.getName(),
                                cityTurnoutRatio(city)
                        ))
                        .min(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
                        .map(AbstractMap.SimpleEntry::getKey)
                        .orElse("Unknown")
        ).join();
    }

    // Без "int registered/issued"
    private double cityTurnoutRatio(City city) {
        return Optional.ofNullable(city.getVoterTurnout()).map(vt ->
                vt.getNumberOfRegisteredVoters() == 0
                        ? 0.0
                        : (double) vt.getNumberOfIssuedVotingEnvelopes() * 100.0
                        / vt.getNumberOfRegisteredVoters()
        ).orElse(0.0);
    }

    @Override
    public String getNameOfCityWithTheHighestNonValidVotesRatio() {
        return regionResults.thenApply(rrList ->
                Optional.ofNullable(rrList).stream()
                        .flatMap(List::stream)
                        .flatMap(rr -> Optional.ofNullable(rr.getRegions()).stream())
                        .flatMap(List::stream)
                        .flatMap(region -> Optional.ofNullable(region.getDistricts()).stream())
                        .flatMap(List::stream)
                        .flatMap(district -> Optional.ofNullable(district.getCities()).stream())
                        .flatMap(List::stream)
                        .map(city -> new AbstractMap.SimpleEntry<>(
                                city.getName(),
                                cityRatioInvalid(city)
                        ))
                        .max(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
                        .map(AbstractMap.SimpleEntry::getKey)
                        .orElse("Unknown")
        ).join();
    }

    private double cityRatioInvalid(City c) {
        return Optional.ofNullable(c.getVoterTurnout()).map(vt ->
                vt.getNumberOfSubmittedVotingEnvelopes() == 0
                        ? 0.0
                        : (vt.getNumberOfSubmittedVotingEnvelopes()
                        - vt.getNumberOfValidVotes())
                        / (double) vt.getNumberOfSubmittedVotingEnvelopes()
        ).orElse(0.0);
    }

    @Override
    public List<String> getTop10CitiesWhereCandidateWonOrderedByNumberOfVotesDesc(int candidateId) {
        return regionResults.thenApply(rrList ->
                        Optional.ofNullable(rrList).stream()
                                .flatMap(List::stream)
                                .flatMap(rr -> Optional.ofNullable(rr.getRegions()).stream())
                                .flatMap(List::stream)
                                .flatMap(region -> Optional.ofNullable(region.getDistricts()).stream())
                                .flatMap(List::stream)
                                .flatMap(d -> Optional.ofNullable(d.getCities()).stream())
                                .flatMap(List::stream)
                                .filter(city -> cityIsWinner(city, candidateId))
                                .sorted(Comparator.comparingInt(c -> cityCandidateVotes((City) c, candidateId)).reversed())
                                .limit(10)
                                .map(City::getName)
                                .toList()
                ).thenApply(list -> Optional.ofNullable(list).orElse(List.of()))
                .join();
    }

    private boolean cityIsWinner(City city, int candidateId) {
        return Optional.ofNullable(city.getVotes()).stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        Vote::getCandidateId,
                        Collectors.summingInt(Vote::getVotes)
                ))
                .entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .orElse(List.of())
                .stream()
                .anyMatch(e -> e.getKey() == candidateId);
    }

    private int cityCandidateVotes(City city, int candidateId) {
        return Optional.ofNullable(city.getVotes()).stream()
                .flatMap(List::stream)
                .filter(v -> v.getCandidateId() == candidateId)
                .mapToInt(Vote::getVotes)
                .sum();
    }

    @Override
    public Map<String, String> getRegionWinnerMap() {
        return regionResults.thenApply(rrList ->
                        Optional.ofNullable(rrList).stream()
                                .flatMap(List::stream)
                                .flatMap(rr -> Optional.ofNullable(rr.getRegions()).stream())
                                .flatMap(List::stream)
                                .collect(Collectors.toMap(
                                        Region::getName,
                                        this::regionWinnersString
                                ))
                ).thenApply(m -> Optional.ofNullable(m).orElse(Map.of()))
                .join();
    }

    private String regionWinnersString(Region r) {
        return Optional.ofNullable(r.getDistricts()).stream()
                .flatMap(List::stream)
                .flatMap(d -> Optional.ofNullable(d.getCities()).stream())
                .flatMap(List::stream)
                .flatMap(city -> Optional.ofNullable(city.getVotes()).stream())
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        Vote::getCandidateId,
                        Collectors.summingInt(Vote::getVotes)
                ))
                .entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .orElse(List.of())
                .stream()
                .map(e -> candidateUtils.getCandidateFullName(e.getKey()).orElse("Unknown"))
                .collect(Collectors.joining(", "));
    }
}
