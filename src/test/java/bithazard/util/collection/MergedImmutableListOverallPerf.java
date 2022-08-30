package bithazard.util.collection;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(3)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
public class MergedImmutableListOverallPerf {
    private static final int NUMBER_OF_LISTS = 100;
    private static final int ELEMENTS_PER_LIST = 100;
    private static final int NUMBER_OF_CONTAINED_ELEMENTS = 20;
    private static final int CONTAINED_ELEMENTS_PERCENT = 50;
    private static final long CONTAINED_ELEMENTS_SEED = 230685777655985L;
    @SuppressWarnings("unchecked")
    private final List<String>[] listsToMerge = new List[NUMBER_OF_LISTS];
    private final List<String> containedElements = new ArrayList<>(NUMBER_OF_CONTAINED_ELEMENTS * CONTAINED_ELEMENTS_PERCENT / 100);
    private final List<String> notContainedElements = new ArrayList<>(NUMBER_OF_CONTAINED_ELEMENTS * (100 - CONTAINED_ELEMENTS_PERCENT) / 100);

    public static void main(String[] args) throws RunnerException, CommandLineOptionException {
        CommandLineOptions commandLineOptions = new CommandLineOptions(args);
        List<String> includes = commandLineOptions.getIncludes();
        if (includes.isEmpty()) {
            includes.add(MergedImmutableListOverallPerf.class.getSimpleName());
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
        Random random = new Random(CONTAINED_ELEMENTS_SEED);
        for (int i = 0; i < NUMBER_OF_CONTAINED_ELEMENTS * CONTAINED_ELEMENTS_PERCENT / 100; i++) {
            int listIndex = random.nextInt(NUMBER_OF_LISTS);
            int indexWithinList = random.nextInt(ELEMENTS_PER_LIST);
            containedElements.add(listsToMerge[listIndex].get(indexWithinList));
        }
        for (int i = 0; i < NUMBER_OF_CONTAINED_ELEMENTS * (100 - CONTAINED_ELEMENTS_PERCENT) / 100; i++) {
            notContainedElements.add(UUID.randomUUID().toString());
        }
    }

    @Benchmark
    public Object[] toArrayUsingMergedImmutableList() {
        List<String> mergedList = MergedImmutableList.of(listsToMerge);
        return mergedList.toArray();
    }

    @Benchmark
    public Object[] toArrayUsingActuallyMergedList() {
        List<String> mergedList = mergeLists(listsToMerge);
        return mergedList.toArray();
    }

    @Benchmark
    public Object[] toArrayTypedUsingMergedImmutableList() {
        List<String> mergedList = MergedImmutableList.of(listsToMerge);
        return mergedList.toArray(new String[0]);
    }

    @Benchmark
    public Object[] toArrayTypedUsingActuallyMergedList() {
        List<String> mergedList = mergeLists(listsToMerge);
        return mergedList.toArray(new String[0]);
    }

    @Benchmark
    public List<String> iterateUsingMergedImmutableList(Blackhole blackhole) {
        List<String> mergedList = MergedImmutableList.of(listsToMerge);
        for (String s : mergedList) {
            blackhole.consume(s);
        }
        return mergedList;
    }

    @Benchmark
    public List<String> iterateUsingActuallyMergedList(Blackhole blackhole) {
        List<String> mergedList = mergeLists(listsToMerge);
        for (String s : mergedList) {
            blackhole.consume(s);
        }
        return mergedList;
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public List<String> containsOneUsingMergedImmutableList(Blackhole blackhole) {
        List<String> mergedList = MergedImmutableList.of(listsToMerge);
        blackhole.consume(mergedList.contains(containedElements.get(0)));
        blackhole.consume(mergedList.contains(notContainedElements.get(0)));
        return mergedList;
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public List<String> containsOneUsingActuallyMergedList(Blackhole blackhole) {
        List<String> mergedList = mergeLists(listsToMerge);
        blackhole.consume(mergedList.contains(containedElements.get(0)));
        blackhole.consume(mergedList.contains(notContainedElements.get(0)));
        return mergedList;
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public List<String> containsAllUsingMergedImmutableList(Blackhole blackhole) {
        List<String> mergedList = MergedImmutableList.of(listsToMerge);
        blackhole.consume(mergedList.containsAll(containedElements));
        blackhole.consume(mergedList.containsAll(notContainedElements));
        return mergedList;
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public List<String> containsAllUsingActuallyMergedList(Blackhole blackhole) {
        List<String> mergedList = mergeLists(listsToMerge);
        blackhole.consume(mergedList.containsAll(containedElements));
        blackhole.consume(mergedList.containsAll(notContainedElements));
        return mergedList;
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
