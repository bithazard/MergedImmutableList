MergedImmutableList
===================
Combines multiple immutable lists to a single list that is only a view of the merged lists (no copying).

Example usage:  
```
List<String> merged = MergedImmutableList.of(List.of("test1", "test2"), List.of("test3", "test4"));
//merged would contain "test1", "test2", "test3", "test4"
```

- You can pass as many lists as you like, not just two
- Passing `null` as a list is not allowed - a NullPointerException will be thrown. Empty lists are fine
- Passed lists must be immutable. Supported are
    - Lists that are e.g. created via List.of(...) or Stream.collect(Collectors.toUnmodifiableList())
    - Basically all immutable lists from Guava
- At least Java 11 is required
- MIT license

The passed lists are not actually merged but references to the lists are stored. When you access elements, using the `List` interface methods, the method calls are basically re-mapped to the appropriate method of the according list. This re-mapping might be (depending on the method) a bit slower compared to an actually merged list. But if you merge a certain amount of lists and only perform a few operations (or even only one) on the merged list, this implementation probably offers a better performance (see Performance).

The static factory method to create the merged list, contains a check that restricts the accepted lists to immutable lists. This is done to ensure that the content of the lists (especially the number of elements inside the lists) is not changed.

### Performance
All performance tests were done with Java 11 and Java 17 using [Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh). All tests were performed twice, once with 100 lists of 100 elements and once with 1000 lists of 1000 elements. The following settings were used for the benchmarks:
- 2 warmup iterations of 5 seconds
- 2 measurement iterations of 5 seconds
- 3 forks (meaning 3 runs each)
- benchmark mode of average time (per method call)

The times were mostly measured in microseconds except for the 100x100 baseline benchmark, where nanoseconds were used (results were becoming to small otherwise). The units are also noted in the last table column for each test.

#### Baseline (Java 11)
These tests try to establish a baseline for different list merging methods to compare against the MergedImmutableList. No operations on the merged lists are performed. To no surprise the MergedImmutableList is by far the fastest because no actual merging takes place. As the 'addAll' merge method is the second fastest here, it is used as a benchmark in all further tests.

| Benchmark (100x100)           |     Score |      Error | Units |
|-------------------------------|----------:|-----------:|------:|
| mergeUsingAddAll              | 46988.951 | ± 1692.132 | ns/op |
| mergeUsingMergedImmutableList |   628.984 | ±    4.909 | ns/op |
| mergeUsingStream              | 82954.233 | ± 1335.961 | ns/op |

| Benchmark (1000x1000)         |    Score |      Error | Units |
|-------------------------------|---------:|-----------:|------:|
| mergeUsingAddAll              | 4914.521 | ± 1825.274 | us/op |
| mergeUsingMergedImmutableList |    7.307 | ±    0.139 | us/op |
| mergeUsingStream              | 8342.404 | ±  347.283 | us/op |

#### Baseline (Java 17)
It is interesting to see that with Java 17 the 'addAll' merge method has gotten way faster and the 'stream' merge method is very inconsistent. These inconsistencies could be reproduced multiple times.

| Benchmark (100x100)           |     Score |      Error | Units |
|-------------------------------|----------:|-----------:|------:|
| mergeUsingAddAll              | 13746.792 | ±  137.837 | ns/op |
| mergeUsingMergedImmutableList |   550.911 | ±   12.453 | ns/op |
| mergeUsingStream              | 94412.757 | ± 2815.154 | ns/op |

| Benchmark (1000x1000)         |     Score |     Error | Units |
|-------------------------------|----------:|----------:|------:|
| mergeUsingAddAll              |  2463.543 | ± 140.364 | us/op |
| mergeUsingMergedImmutableList |     6.486 | ±   0.048 | us/op |
| mergeUsingStream              | 11294.380 | ± 594.091 | us/op |

#### Individual (Java 11)
These tests measure certain methods on an ArrayList compared to the same method on the MergedImmutableList. The actual merging is not part of the test. The goal is to see how much overhead the mapping to the appropriate method of the underlying list generates. The expectancy here is that the MergedImmutableList will in general be slower than the ArrayList. This is mostly exactly what we see, with the interesting exception of a simple iteration, where the MergedImmutableList is faster that the ArrayList (at least for the 100 lists of 100 elements). This result could also be reproduced multiple times. We also see that the overhead is quite small for `contains` or `containsAll` but rather significant for `toArray` or `toArray(T[] a)`.

| Benchmark (100x100)                  |   Score |   Error | Units |
|--------------------------------------|--------:|--------:|------:|
| containsAllUsingArrayList            | 106.325 | ± 2.921 | us/op |
| containsAllUsingMergedImmutableList  | 109.921 | ± 4.322 | us/op |
| containsOneUsingArrayList            |  23.187 | ± 0.216 | us/op |
| containsOneUsingMergedImmutableList  |  24.619 | ± 0.529 | us/op |
| iterateUsingArrayList                |  71.254 | ± 0.214 | us/op |
| iterateUsingMergedImmutableList      |  69.456 | ± 0.255 | us/op |
| toArrayTypedUsingArrayList           |  13.518 | ± 0.223 | us/op |
| toArrayTypedUsingMergedImmutableList |  37.067 | ± 0.752 | us/op |
| toArrayUsingArrayList                |   4.585 | ± 0.038 | us/op |
| toArrayUsingMergedImmutableList      |  41.729 | ± 0.274 | us/op |

| Benchmark (1000x1000)                |     Score |     Error | Units |
|--------------------------------------|----------:|----------:|------:|
| containsAllUsingArrayList            | 28904.134 | ± 196.529 | us/op |
| containsAllUsingMergedImmutableList  | 29257.887 | ± 257.403 | us/op |
| containsOneUsingArrayList            |  8545.871 | ± 222.771 | us/op |
| containsOneUsingMergedImmutableList  |  8659.631 | ± 186.306 | us/op |
| iterateUsingArrayList                | 10133.194 | ± 420.082 | us/op |
| iterateUsingMergedImmutableList      | 10717.469 | ± 176.586 | us/op |
| toArrayTypedUsingArrayList           |  6619.861 | ± 222.717 | us/op |
| toArrayTypedUsingMergedImmutableList |  9602.006 | ± 656.554 | us/op |
| toArrayUsingArrayList                |  1345.583 | ± 270.297 | us/op |
| toArrayUsingMergedImmutableList      |  4804.152 | ± 590.599 | us/op |

#### Individual (Java 17)
As in the baseline tests we can see some nice performance improvements for Java 17 (with the exception of the `contains` and `containsAll` methods) from which the MergedImmutableList seems to profit a bit more than the ArrayList. We also see a huge improvement for the simple iteration which here also matches the expectation that the MergedImmutableList should be slower than the ArrayList.

| Benchmark (100x100)                  |    Score |     Error | Units |
|--------------------------------------|---------:|----------:|------:|
| containsAllUsingArrayList            |  105.147 | ± 1.733 | us/op |
| containsAllUsingMergedImmutableList  |  118.502 | ± 1.393 | us/op |
| containsOneUsingArrayList            |   23.317 | ± 0.173 | us/op |
| containsOneUsingMergedImmutableList  |   26.059 | ± 0.377 | us/op |
| iterateUsingArrayList                |   19.651 | ± 0.074 | us/op |
| iterateUsingMergedImmutableList      |   25.920 | ± 0.171 | us/op |
| toArrayTypedUsingArrayList           |   12.829 | ± 0.303 | us/op |
| toArrayTypedUsingMergedImmutableList |   23.110 | ± 0.341 | us/op |
| toArrayUsingArrayList                |    4.566 | ± 0.049 | us/op |
| toArrayUsingMergedImmutableList      |   13.397 | ± 0.033 | us/op |

| Benchmark (1000x1000)                |     Score |     Error | Units |
|--------------------------------------|----------:|----------:|------:|
| containsAllUsingArrayList            | 30321.942 | ± 790.119 | us/op |
| containsAllUsingMergedImmutableList  | 30529.166 | ± 509.899 | us/op |
| containsOneUsingArrayList            |  9234.044 | ± 228.350 | us/op |
| containsOneUsingMergedImmutableList  |  9352.756 | ± 138.868 | us/op |
| iterateUsingArrayList                |  7506.416 | ±  94.404 | us/op |
| iterateUsingMergedImmutableList      |  7969.725 | ±  72.334 | us/op |
| toArrayTypedUsingArrayList           |  6698.300 | ± 776.370 | us/op |
| toArrayTypedUsingMergedImmutableList |  7621.062 | ± 477.296 | us/op |
| toArrayUsingArrayList                |  1464.219 | ± 190.614 | us/op |
| toArrayUsingMergedImmutableList      |  2444.867 | ± 139.415 | us/op |

#### Overall (Java 11)
Finally, these tests measure some scenarios that could be viable use cases. This means that the actual merging is part of the test. The expectancy here is of course that the MergedImmutableList is faster than the actually merged list. And this is indeed the case for every test.

| Benchmark (100x100)                  |   Score |     Error | Units |
|--------------------------------------|--------:|----------:|------:|
| containsAllUsingActuallyMergedList   | 108.354 | ±  11.488 | us/op |
| containsAllUsingMergedImmutableList  |  87.953 | ±   2.081 | us/op |
| containsOneUsingActuallyMergedList   |  41.034 | ±   1.143 | us/op |
| containsOneUsingMergedImmutableList  |  20.646 | ±   0.616 | us/op |
| iterateUsingActuallyMergedList       |  98.188 | ±   4.876 | us/op |
| iterateUsingMergedImmutableList      |  63.073 | ±   0.357 | us/op |
| toArrayTypedUsingActuallyMergedList  |  59.041 | ±   1.296 | us/op |
| toArrayTypedUsingMergedImmutableList |  38.080 | ±   1.797 | us/op |
| toArrayUsingActuallyMergedList       |  51.104 | ±   0.767 | us/op |
| toArrayUsingMergedImmutableList      |  45.529 | ±   3.512 | us/op |

| Benchmark (1000x1000)                |     Score |     Error | Units |
|--------------------------------------|----------:|----------:|------:|
| containsAllUsingActuallyMergedList   | 29698.145 | ± 430.147 | us/op |
| containsAllUsingMergedImmutableList  | 29248.727 | ± 430.473 | us/op |
| containsOneUsingActuallyMergedList   |  9886.018 | ± 461.922 | us/op |
| containsOneUsingMergedImmutableList  |  8630.384 | ± 213.103 | us/op |
| iterateUsingActuallyMergedList       | 15137.034 | ± 611.877 | us/op |
| iterateUsingMergedImmutableList      | 10792.757 | ± 283.660 | us/op |
| toArrayTypedUsingActuallyMergedList  | 10733.376 | ± 220.864 | us/op |
| toArrayTypedUsingMergedImmutableList |  9705.237 | ± 271.194 | us/op |
| toArrayUsingActuallyMergedList       |  5423.569 | ± 444.690 | us/op |
| toArrayUsingMergedImmutableList      |  4592.566 | ± 307.250 | us/op |

#### Overall (Java 17)
For Java 17 the results are generally the same but the differences are much smaller. We even see two cases for the 1000 lists of 1000 elements where the MergedImmutableList is slower than the actually merged list. This is quite strange as the difference is not that large in the individual benchmarks above.

| Benchmark (100x100)                  |   Score |    Error | Units |
|--------------------------------------|--------:|---------:|------:|
| containsAllUsingActuallyMergedList   | 102.042 | ± 20.676 | us/op |
| containsAllUsingMergedImmutableList  |  96.055 | ±  3.061 | us/op |
| containsOneUsingActuallyMergedList   |  29.703 | ±  0.692 | us/op |
| containsOneUsingMergedImmutableList  |  22.326 | ±  0.614 | us/op |
| iterateUsingActuallyMergedList       |  28.797 | ±  1.143 | us/op |
| iterateUsingMergedImmutableList      |  26.057 | ±  1.298 | us/op |
| toArrayTypedUsingActuallyMergedList  |  24.166 | ±  0.391 | us/op |
| toArrayTypedUsingMergedImmutableList |  23.530 | ±  0.412 | us/op |
| toArrayUsingActuallyMergedList       |  18.372 | ±  0.251 | us/op |
| toArrayUsingMergedImmutableList      |  14.502 | ±  0.643 | us/op |

| Benchmark (1000x1000)                |     Score |     Error | Units |
|--------------------------------------|----------:|----------:|------:|
| containsAllUsingActuallyMergedList   | 26541.411 | ± 505.556 | us/op |
| containsAllUsingMergedImmutableList  | 30636.665 | ± 259.922 | us/op |
| containsOneUsingActuallyMergedList   |  8333.845 | ± 914.031 | us/op |
| containsOneUsingMergedImmutableList  |  9389.064 | ± 159.438 | us/op |
| iterateUsingActuallyMergedList       |  8727.077 | ± 216.717 | us/op |
| iterateUsingMergedImmutableList      |  8000.400 | ±  95.285 | us/op |
| toArrayTypedUsingActuallyMergedList  | 10160.488 | ± 491.910 | us/op |
| toArrayTypedUsingMergedImmutableList |  7620.307 | ± 487.910 | us/op |
| toArrayUsingActuallyMergedList       |  4247.123 | ± 260.636 | us/op |
| toArrayUsingMergedImmutableList      |  2420.188 | ± 213.982 | us/op |
