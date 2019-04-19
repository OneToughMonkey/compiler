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

    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private Set<String> beenThere = new ConcurrentSkipListSet<>();

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

    public Stream<String> process() {
        String leftSide;
        final Stream.Builder<String> streamBuilder = Stream.builder();
        try {
            while ((leftSide = queue.poll(5, TimeUnit.MILLISECONDS)) != null) {
                if (leftSide.equals(leftSide.toLowerCase()))
                    streamBuilder.accept(leftSide);
                else
                    for (String[] rule : rules) {
                        for (int i = 0; i <= leftSide.length() - rule[0].length(); i++)
                            if (leftSide.startsWith(rule[0], i)) {
                                final String expansion = leftSide.substring(0, i)
                                        + rule[1]
                                        + leftSide.substring(i + rule[0].length());
                                if (expansion.length() <= limit && !beenThere.contains(expansion)) {
                                    queue.offer(expansion);
                                    beenThere.add(expansion);
                                }
                            }
                    }
            }
        } catch (InterruptedException e) {}

        return streamBuilder.build();
    }
    @Override
    public Stream<String> generate(Stream<String[]> grammar, int uptoLength) {
        limit = uptoLength;
        rules = grammar.collect(Collectors.toList());
        final char start = rules.get(0)[0].charAt(0);
        queue.offer(String.valueOf(start));
        return IntStream.range(1, Runtime.getRuntime().availableProcessors())
                .mapToObj(token -> process())
                .flatMap(Function.identity())
                .parallel()
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
