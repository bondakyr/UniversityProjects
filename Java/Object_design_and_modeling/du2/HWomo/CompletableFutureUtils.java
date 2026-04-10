package cz.cvut.fel.omo.hw.functions.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@UtilityClass
public class CompletableFutureUtils {

    public static <T, U> U applyAndGet(CompletableFuture<T> cf, Function<T, U> fn) {
        return cf.handle((value, err) ->
                Optional.ofNullable(err)
                        .map(e -> (U) null)
                        .orElseGet(() -> fn.apply(value))
        ).join();
    }
}
