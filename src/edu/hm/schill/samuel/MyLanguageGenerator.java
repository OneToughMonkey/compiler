package edu.hm.schill.samuel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
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
     * Ein temporärer Cache bereits bearbeiteter Strings.
     */
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

    /**
     * Fuehrt alle moeglichen Produktionen auf einem String aus und startet
     * neue Tasks fuer die Ergebnisse.
     */
    private class GenerationTask extends RecursiveTask<Stream<String>> {
        /**
         * Noetig fuer PMD.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Der String, auf den Produktionen angewendet werden sollen.
         */
        private final String leftSide;

        /**
         * Erzeugt einen neuen GenerationTask fuer den gegebenen String.
         * @param leftSide Der String, auf den Produktionen angewendet werden sollen
         */
        /* default */ GenerationTask(String leftSide) {
            this.leftSide = leftSide;
        }

        /* Beendet die Rekursion, wenn der String die zulaessige Laenge ueberschritten hat
         * oder keine Variablen mehr enthaelt oder legt neue Tasks fuer die entstehenden
         * Expansionen an.
         */
        @Override
        protected Stream<String> compute() {
            final Stream<String> result;
            if (leftSide.length() > limit || beenThere.contains(leftSide))
                result = Stream.empty();
            else if (leftSide.equals(leftSide.toLowerCase()))
                result = Stream.of(leftSide);
            else
                result = createSubtasks().parallelStream()
                        .flatMap(RecursiveTask::invoke);
            beenThere.add(leftSide);
            return result;
        }

        /**
         * Generiert alle mit Anwendung einer Produktionsregel moeglichen Expansionen
         * des Strings leftSide und legt neue GenerationTasks fuer sie an.
         * @return Die angelegten GenerationTasks
         */
        private Collection<GenerationTask> createSubtasks() {
            final Collection<GenerationTask> tasks = new ArrayList<>();
            for (String[] rule : rules) {
                for (int i = 0; i <= leftSide.length() - rule[0].length(); i++)
                    if (leftSide.startsWith(rule[0], i))
                        tasks.add(new GenerationTask(
                                leftSide.substring(0, i)
                                + rule[1]
                                + leftSide.substring(i + rule[0].length())
                        ));
            }
            return tasks;
        }
    }

    @Override
    public Stream<String> generate(Stream<String[]> grammar, int uptoLength) {
        beenThere.clear();
        limit = uptoLength;
        rules = grammar.collect(Collectors.toList());
        final char start = rules.get(0)[0].charAt(0);
        final GenerationTask firstTask = new GenerationTask(String.valueOf(start));
        return ForkJoinPool.commonPool().invoke(firstTask);
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
