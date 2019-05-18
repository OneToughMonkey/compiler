package edu.hm.schill.samuel;

import edu.hm.cs.rs.compiler.lab06rdparsergenerator.RDParserGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyParserGenerator implements RDParserGenerator {

    public static void main(String[] args) {
        RDParserGenerator pg = new MyParserGenerator();
        String grammar = args[0];
        System.out.println(pg.generate(grammar));
    }

    @Override
    public String generate(String grammar) {
        final Map<String[], Set<Character>> firstSets = getFirstSets(grammar);
        final char start = grammar.charAt(2);
        final Set<Character> nonTerminals = grammar.chars()
                .filter(chr -> chr >= 'A' && chr <= 'Z')
                .mapToObj(chr -> (char) chr)
                .collect(Collectors.toSet());

        return "import java.util.*;\n"
                + "\n"
                + "public class RDParser" + start + " {\n"
                + "static " + getNodeSourcecode()
                + "\n"
                + "static " + getSyntaxErrorExceptionSourcecode()
                + "\n"
                + "    public static void main(String... args) throws SyntaxErrorException {\n"
                + "        Node parseTree = new RDParser" + start + "().parse(args[0]);\n"
                + "        System.out.println(parseTree);  // Parsebaum in einer Zeile\n"
                + "        parseTree.prettyPrint();        // Parsebaum gekippt, mehrzeilig\n"
                + "    }\n"
                + "\n"
                + "    private String input;\n"
                + "\n"
                + "    public Node parse(String newInput) throws SyntaxErrorException {\n"
                + "        input = newInput;\n"
                + "        final Node result = " + start + "();\n"
                + "        if (!input.isEmpty())\n"
                + "            throw new SyntaxErrorException(\"Expected end of file but found '\" + input.charAt(0) + \"'\");\n"
                + "        return result;\n"
                + "    }\n"
                + "\n"
                + "    public Node terminal(char expected) throws SyntaxErrorException {\n"
                + "        final char lookahead = input.charAt(0);\n"
                + "        if(lookahead != expected)\n"
                + "            throw new SyntaxErrorException(\"Expected '\" + expected + \"' but found '\" + lookahead + \"'\");\n"
                + "        input = input.substring(1);\n"
                + "        return new Node(Character.toString(expected));\n"
                + "    }\n"
                + "\n"
                + nonTerminals.stream()
                .map(token -> "    private Node " + token + "() throws SyntaxErrorException {\n"
                                + "        final char lookahead = input.charAt(0);\n"
                                + firstSets.keySet().stream()
                                .filter(rule -> rule[0].charAt(0) == token)
                                .map(rule ->
                                        "        if(" + firstSets.get(rule).stream()
                                                .map(chr -> "lookahead == '" + chr + "'")
                                                .collect(Collectors.joining(" || ")) + ")\n "
                                                + "            return new Node(\"" + token + "\", "
                                                + rule[1].chars()
                                                .mapToObj(chr ->
                                                        chr >= 'A' && chr <= 'Z'
                                                                ? (char) chr + "()"
                                                                : "terminal('" + (char) chr + "')")
                                                .collect(Collectors.joining(", "))
                                                + ");\n"
                                ).collect(Collectors.joining(""))
                                + "        throw new SyntaxErrorException(\"Expected one of "
                                + firstSets.keySet().stream()
                                .filter(rule -> rule[0].charAt(0) == token)
                                .flatMap(rule -> firstSets.get(rule).stream())
                                .map(chr -> "'" + chr + "'")
                                .collect(Collectors.joining(", "))
                                + " but found '\" + lookahead + \"'\");\n"
                                + "    }\n"
                ).collect(Collectors.joining("\n"))
                + "}\n";
    }

    public Map<String[], Set<Character>> getFirstSets(String grammar) {
        final char deduct = grammar.charAt(0);
        final char separate = grammar.charAt(1);
        final List<String[]> rules = Stream.of(grammar.split("\\Q" + separate + "\\E"))
                .skip(1)
                .map(string -> string.split("\\Q" + deduct + "\\E", 2))
                .collect(Collectors.toList());
        final Map<String[], Set<Character>> firstSets = new HashMap<>();
        rules.forEach(rule -> firstSets.put(rule, new HashSet<>()));

        boolean[] changed = {true};
        while (changed[0]) {
            changed[0] = false;
            for (String[] rule : rules) {
                final char first = rule[1].charAt(0);
                if (first >= 'A' && first <= 'Z') {
                    firstSets.keySet().stream()
                            .filter(otherRule -> otherRule[0].charAt(0) == first)
                            .forEach(otherRule ->
                                    changed[0] = firstSets.get(rule).addAll(firstSets.get(otherRule)) || changed[0]);
                } else
                    changed[0] = firstSets.get(rule).add(first) || changed[0];
            }
        }
        return firstSets;
    }
}
