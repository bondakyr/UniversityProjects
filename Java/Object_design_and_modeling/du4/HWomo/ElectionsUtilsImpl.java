package cz.cvut.fel.omo.hw.functions.utils;

import cz.cvut.fel.omo.hw.functions.data.model.AbroadResults;
import cz.cvut.fel.omo.hw.functions.data.model.RegionResults;
import cz.cvut.fel.omo.hw.functions.data.model.Vote;
import cz.cvut.fel.omo.hw.functions.data.model.VoterTurnout;
import cz.cvut.fel.omo.hw.functions.data.model.City;
import cz.cvut.fel.omo.hw.functions.data.model.Country;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ElectionsUtilsImpl implements ElectionsUtils {

    private final CompletableFuture<List<RegionResults>> regionResultsFuture;
    private final CompletableFuture<AbroadResults> abroadResultsFuture;

    @Override
    public List<Vote> getAllVotes() {
        return CompletableFuture
                .allOf(regionResultsFuture, abroadResultsFuture)
                .thenApply(ignored ->
                        Stream.concat(
                                // Регіони
                                Optional.ofNullable(regionResultsFuture.join())
                                        .stream()
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(rr -> Optional.ofNullable(rr.getRegions()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(r -> Optional.ofNullable(r.getDistricts()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(d -> Optional.ofNullable(d.getCities()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(city -> Optional.ofNullable(city.getVotes()).stream())
                                        .flatMap(List::stream),

                                // Закордон
                                Optional.ofNullable(abroadResultsFuture.join())
                                        .map(AbroadResults::getAbroad)
                                        .stream()
                                        .flatMap(abroad -> Optional.ofNullable(abroad.getContinents()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(cont -> Optional.ofNullable(cont.getCountries()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(country -> Optional.ofNullable(country.getVotes()).stream())
                                        .flatMap(List::stream)
                        ).toList()
                )
                .thenApply(list -> Optional.ofNullable(list).orElse(List.of()))
                .join();
    }

    @Override
    public List<VoterTurnout> getAllVoterTurnouts() {
        return CompletableFuture
                .allOf(regionResultsFuture, abroadResultsFuture)
                .thenApply(ignored ->
                        Stream.concat(
                                // Регіони
                                Optional.ofNullable(regionResultsFuture.join())
                                        .stream()
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(rr -> Optional.ofNullable(rr.getRegions()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(r -> Optional.ofNullable(r.getDistricts()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(d -> Optional.ofNullable(d.getCities()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .map(City::getVoterTurnout)
                                        .filter(Objects::nonNull),

                                // Закордон
                                Optional.ofNullable(abroadResultsFuture.join())
                                        .map(AbroadResults::getAbroad)
                                        .stream()
                                        .flatMap(abroad -> Optional.ofNullable(abroad.getContinents()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .flatMap(cont -> Optional.ofNullable(cont.getCountries()).stream())
                                        .flatMap(List::stream)
                                        .filter(Objects::nonNull)
                                        .map(Country::getVoterTurnout)
                                        .filter(Objects::nonNull)
                        ).toList()
                )
                .thenApply(list -> Optional.ofNullable(list).orElse(List.of()))
                .join();
    }
}
