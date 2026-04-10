package cz.cvut.fel.omo.hw.functions.statistics;

import cz.cvut.fel.omo.hw.functions.data.model.AbroadResults;
import cz.cvut.fel.omo.hw.functions.data.model.Candidates;
import cz.cvut.fel.omo.hw.functions.data.model.RegionResults;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtils;
import cz.cvut.fel.omo.hw.functions.utils.CandidateUtilsImpl;
import cz.cvut.fel.omo.hw.functions.utils.ElectionsUtils;
import cz.cvut.fel.omo.hw.functions.utils.ElectionsUtilsImpl;
import cz.cvut.fel.omo.hw.functions.data.model.Vote;
import cz.cvut.fel.omo.hw.functions.data.model.VoterTurnout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ElectionStatisticsImpl implements ElectionStatistics {

    private final ElectionsUtils electionsUtils;
    private final CandidateUtils candidateUtils;

    public ElectionStatisticsImpl(
            CompletableFuture<List<RegionResults>> regionResultsFuture,
            CompletableFuture<AbroadResults> abroadResultsFuture,
            CompletableFuture<Candidates> candidatesFuture
    ) {
        this.candidateUtils = new CandidateUtilsImpl(candidatesFuture);
        this.electionsUtils = new ElectionsUtilsImpl(regionResultsFuture, abroadResultsFuture);
    }

    @Override
    public int getTotalValidVotes() {
        return electionsUtils.getAllVoterTurnouts().stream()
                .mapToInt(VoterTurnout::getNumberOfValidVotes)
                .sum();
    }

    @Override
    public int getTotalInvalidVotes() {
        return electionsUtils.getAllVoterTurnouts().stream()
                .mapToInt(vt -> vt.getNumberOfSubmittedVotingEnvelopes() - vt.getNumberOfValidVotes())
                .sum();
    }

    @Override
    public int getTotalVoterCount() {
        return electionsUtils.getAllVoterTurnouts().stream()
                .mapToInt(VoterTurnout::getNumberOfRegisteredVoters)
                .sum();
    }

    @Override
    public int getTotalIssuedEnvelopes() {
        return electionsUtils.getAllVoterTurnouts().stream()
                .mapToInt(VoterTurnout::getNumberOfIssuedVotingEnvelopes)
                .sum();
    }

    @Override
    public double getTotalVoterTurnout() {
        return getTotalVoterCount() == 0
                ? 0.0
                : (double) getTotalIssuedEnvelopes() * 100.0 / getTotalVoterCount();
    }

    @Override
    public Map<String, Integer> getCandidateVotesMap() {
        return electionsUtils.getAllVotes().stream()
                .collect(Collectors.groupingBy(
                        v -> candidateUtils.getCandidateFullName(v.getCandidateId()).orElse("Unknown"),
                        Collectors.summingInt(Vote::getVotes)
                ));
    }

    @Override
    public Map<String, Double> getCandidateVotesPercentMap() {
        int totalValid = getTotalValidVotes();
        Map<String, Integer> candidateVotes = getCandidateVotesMap();
        return candidateVotes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> totalValid == 0
                                ? 0.0
                                : e.getValue() * 100.0 / (double) totalValid
                ));
    }

    @Override
    public String getCandidatesByVotesDesc() {
        Map<String, Integer> votesMap = getCandidateVotesMap();
        Map<String, Double> percentMap = getCandidateVotesPercentMap();
        return votesMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> String.format("%s (%.2f%%)",
                        e.getKey(),
                        percentMap.getOrDefault(e.getKey(), 0.0)
                ))
                .collect(Collectors.joining(", "));
    }
}
