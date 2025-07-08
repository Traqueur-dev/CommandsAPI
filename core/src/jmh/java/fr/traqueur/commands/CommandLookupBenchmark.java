package fr.traqueur.commands;

import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CommandLookupBenchmark {

    @Param({ "1000", "10000", "50000" })
    public int N;

    @Param({ "1", "2", "3" })
    public int maxDepth;

    private Map<String, DummyCommand> flatMap;
    private CommandTree<DummyCommand, Object> tree;
    private String[] rawLabels;

    @Setup(Level.Trial)
    public void setup() {
        flatMap = new HashMap<>(N);
        tree    = new CommandTree<>();

        rawLabels = new String[N];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < N; i++) {
            int depth = 1 + rnd.nextInt(maxDepth);
            StringBuilder sb = new StringBuilder();
            for (int d = 0; d < depth; d++) {
                if (d > 0) sb.append('.');
                sb.append("cmd").append(rnd.nextInt(N * 10));
            }
            String label = sb.toString();
            rawLabels[i] = label;

            DummyCommand cmd = new DummyCommand(label);
            flatMap.put(label, cmd);
            tree.addCommand(label, cmd);
        }
    }

    @Benchmark
    public DummyCommand mapLookup() {
        String raw = rawLabels[ThreadLocalRandom.current().nextInt(N)];
        String[] segments = raw.split("\\.");
        for (int len = segments.length; len > 0; len--) {
            String key = String.join(".", Arrays.copyOf(segments, len));
            DummyCommand c = flatMap.get(key);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Benchmark
    public CommandTree.MatchResult<DummyCommand, Object> treeLookup() {
        String raw = rawLabels[ThreadLocalRandom.current().nextInt(N)];
        String[] parts = raw.split("\\.");
        String base = parts[0];
        String[] sub = Arrays.copyOfRange(parts, 1, parts.length);
        return tree.findNode(base, sub).orElse(null);
    }

    public static class DummyCommand extends Command<DummyCommand, Object> {
        public DummyCommand(String name) { super(null, name); }
        @Override public void execute(Object s, fr.traqueur.commands.api.arguments.Arguments a) {}
    }
}
