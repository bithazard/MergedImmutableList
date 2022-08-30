package bithazard.util.collection;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
public class MergedImmutableListBaselinePerf {
    private static final int NUMBER_OF_LISTS = 100;
    private static final int ELEMENTS_PER_LIST = 100;
    @SuppressWarnings("unchecked")
    private final List<String>[] listsToMerge = new List[NUMBER_OF_LISTS];

    public static void main(String[] args) throws RunnerException, CommandLineOptionException {
        CommandLineOptions commandLineOptions = new CommandLineOptions(args);
        List<String> includes = commandLineOptions.getIncludes();
        if (includes.isEmpty()) {
            includes.add(MergedImmutableListBaselinePerf.class.getSimpleName());
        }
        new Runner(commandLineOptions).run();
    }

    @Setup
    public void setup() {
        for (int i = 0; i < NUMBER_OF_LISTS; i++) {
            listsToMerge[i] = Stream.generate(() -> UUID.randomUUID().toString())
                    .limit(ELEMENTS_PER_LIST)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    @Benchmark
    public List<String> mergeUsingMergedImmutableList() {
        return MergedImmutableList.of(listsToMerge);
    }

    @Benchmark
    public List<String> mergeUsingStream() {
        return concat(listsToMerge);
    }

    @Benchmark
    public List<String> mergeUsingAddAll() {
        return mergeLists(listsToMerge);
    }

    @SafeVarargs
    public static <T> List<T> concat(List<T>... lists) {
        return Stream.of(lists).flatMap(List::stream).collect(Collectors.toList());
    }

    @SafeVarargs
    private static <T> List<T> mergeLists(List<T>... lists) {
        int totalSize = 0;
        for (List<T> list : lists) {
            totalSize += list.size();
        }
        List<T> mergedList = new ArrayList<>(totalSize);
        for (List<T> list : lists) {
            mergedList.addAll(list);
        }
        return mergedList;
    }
}
