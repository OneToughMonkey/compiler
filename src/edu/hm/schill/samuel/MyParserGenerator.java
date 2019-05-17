package edu.hm.schill.samuel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyParserGenerator implements edu.hm.cs.rs.compiler.lab06rdparsergenerator.RDParserGenerator {

    @Override
    public String generate(String grammar) {
        final char deduct = grammar.charAt(0);
        final char separate = grammar.charAt(1);
        final char start = grammar.charAt(2);
        final List<String[]> rules = Stream.of(grammar.split("\\Q" + separate + "\\E"))
                .skip(1)
                .map(string -> string.split("\\Q" + deduct + "\\E", 2))
                .collect(Collectors.toList());
        final Set<Character> tokens = grammar.chars()
                .filter(chr -> chr != deduct && chr != separate)
                .mapToObj(chr -> Character.valueOf((char) chr))
                .collect(Collectors.toSet());

        return "import java.util.*;\n"
                + "import java.io.*;\n"
                + "\n"
                + "public class RDParser" + start + " {\n"
                + "    static " + getNodeSourcecode()
                + "\n"
                + "    static " + getSyntaxErrorExceptionSourcecode()
                + "\n"
                + "    private char lookahead;\n"
                + "\n"
                + "    public Node parse(String input) throws SyntaxErrorException {\n"
                + "        lookahead = input.charAt(0);\n"
                + "        return parse_" + start + "();\n"
                + "    }\n"
                + "\n";
    }

    public Map<String[], Set<Character>> getFirstSets (List<String[]> rules) {
        final Map<String[], Set<Character>> firstSets = new HashMap<>();
        rules.stream()
                .forEach(rule -> firstSets.put(rule, new HashSet<>()));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String[] rule : rules) {
               char first = rule[1].charAt(0);
               if (first >= 'A' && first <= 'Z') {
                   firstSets.keySet().stream()
                           .filter(otherRule -> otherRule[0].charAt(0) == first)
                           .forEach(otherRule ->
                                   changed = firstSets.get(rule).addAll(firstSets.get(otherRule)));
               } else {
                   changed = firstSets.get(rule).add(first) || changed;
               }
            }
        }


    }
}
