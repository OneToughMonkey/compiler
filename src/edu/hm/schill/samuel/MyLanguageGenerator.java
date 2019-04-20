package edu.hm.schill.samuel;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Loesung fuer die vierte Praktikumsaufgabe.
 */
public class MyLanguageGenerator implements LanguageGenerator {
    /**
     * Die Produktionsregeln.
     */
    private List<String[]> rules;
    /**
     * Die maximal zu erzeugende Wortlaenge.
     */
    private int limit;
    /**
     * Eine Queue von ggf. zu expandierenden Strings.
     */
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    /**
     * Ein temporaerer Cache bereits bearbeiteter Strings.
     * Vermeidet viele unnoetige Berechnungen
     */
    private final Set<String> beenThere = new ConcurrentSkipListSet<>();

    /**
     * Gibt die Woerter, die die Grammatik im ersten Kommandozeilenargument erzeugt
     * bis zur maximalen Laenge im zweiten Kommandozeilenargument aus.
     * @param args [0]: Grammatik [1]: Maximale Laenge
     */
    public static void main(String... args) {
        final LanguageGenerator generator = new MyLanguageGenerator();
        generator.generate(generator.read(args[0]), Integer.parseInt(args[1]))
                .forEach(System.out::println);
    }

    /**
     * Generiert Expansionen aus den Strings in der Queue und haengt diese an die Queue
     * an, falls noch Variablen vorkommen, oder gibt sie sonst in einem Stream zurueck,
     * sofern die Expansion nicht bereits gesehen wurde.
     * @return Ein Stream aller durchgefuehrten Expansionen, in denen keine Variablen mehr vorkommen.
     */
    private Stream<String> process() {
        final Stream.Builder<String> streamBuilder = Stream.builder();
        try {
            for (String leftSide = queue.poll(10, TimeUnit.MILLISECONDS);
                 leftSide != null;
                 leftSide = queue.poll(10, TimeUnit.MILLISECONDS))
                for (String[] rule : rules)
                    for (int index = 0; index <= leftSide.length() - rule[0].length(); index++)
                        if (leftSide.startsWith(rule[0], index)) {
                            final String expansion = leftSide.substring(0, index)
                                    + rule[1]
                                    + leftSide.substring(index + rule[0].length());
                            if (expansion.length() <= limit && !beenThere.contains(expansion)) {
                                beenThere.add(expansion);
                                if (expansion.equals(expansion.toLowerCase()))
                                    streamBuilder.accept(expansion);
                                else
                                    queue.offer(expansion);
                            }
                        }
        } catch (InterruptedException exception) { /* not happening */ }

        return streamBuilder.build();
    }

    @Override
    public Stream<String> generate(Stream<String[]> grammar, int uptoLength) {
        queue.clear();
        beenThere.clear();
        limit = uptoLength;
        rules = grammar.collect(Collectors.toList());
        final String start = rules.get(0)[0];
        queue.offer(String.valueOf(start));
        return IntStream.range(1, Runtime.getRuntime().availableProcessors())
                .parallel()
                .mapToObj(processor -> process())
                .flatMap(Function.identity())
                /* Der Cache bereits gesehener Expansionen reicht aufgrund der parallelen Bearbeitung
                 * leider nicht aus, um Duplikate vollstaendig zu vermeiden. */
                .distinct();
    }

    @Override
    public Stream<String[]> read(String grammarString) {
        final char deduct = grammarString.charAt(0);
        final char separate = grammarString.charAt(1);
        return Stream.of(grammarString.split("\\Q" + separate + "\\E"))
                .skip(1)
                .map(string -> string.split("\\Q" + deduct + "\\E", 2));
    }
}
