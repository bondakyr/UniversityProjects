package cz.cvut.fel.nss.scraperservice.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScraperStrategyFactory {

    private final List<ScraperStrategy> strategies;
    private Map<String, ScraperStrategy> byKey;

    public Optional<ScraperStrategy> get(String key) {
        if (key == null) return Optional.empty();
        if (byKey == null) {
            byKey = strategies.stream()
                    .collect(Collectors.toMap(s -> s.key().toUpperCase(), s -> s));
        }
        return Optional.ofNullable(byKey.get(key.toUpperCase()));
    }

    public List<String> registeredKeys() {
        return strategies.stream().map(ScraperStrategy::key).sorted().toList();
    }
}
