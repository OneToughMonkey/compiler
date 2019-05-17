package edu.hm.schill.samuel;

import edu.hm.cs.rs.compiler.lab06rdparsergenerator.RDParserGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyParserGenerator implements RDParserGenerator {

    public static void main(String[] args) {
        RDParserGenerator pg = new MyParserGenerator();

        String grammar = "=,E=(EOE),E=F,O=+,O=-,F=n,F=-E";
        System.out.println(pg.generate(grammar));
    }
    @Override
    public String generate(String grammar) {
        final Map<String[], Set<Character>> firstSets = getFirstSets(grammar);
        final char deduct = grammar.charAt(0);
        final char separate = grammar.charAt(1);
        final char start = grammar.charAt(2);
        final Set<Character> tokens = grammar.chars()
                .filter(chr -> chr != deduct && chr != separate)
                .mapToObj(chr -> Character.valueOf((char) chr))
                .collect(Collectors.toSet());

        return "import java.util.*;\n"
                + "\n"
                + "public class RDParser" + start + " {\n"
                + "    static " + getNodeSourcecode()
                + "\n"
                + "    static " + getSyntaxErrorExceptionSourcecode()
                + "\n"
                + "    public static void main(String... args) throws SyntaxErrorException {\n"
                + "        Node parseTree = new RDParser" + start + "().parse(args[0]);\n"
                + "        System.out.println(parseTree);  // Parsebaum in einer Zeile\n"
                + "        parseTree.prettyPrint();        // Parsebaum gekippt, mehrzeilig\n"
                + "    }"
                + "\n"
                + "    private char lookahead;\n"
                + "    private String input;\n"
                + "\n"
                + "    public Node parse(String newInput) throws SyntaxErrorException {\n"
                + "        input = newInput;"
                + "        return parse" + (int) start + "();\n"
                + "    }\n"
                + "\n"
                + tokens.parallelStream().map(token -> {
            final Set<Character> combinedFirstSet = firstSets.keySet().stream().filter(arr -> arr[0].charAt(0)==token).flatMap(rule -> firstSets.get(rule).stream()).collect(Collectors.toSet());

            return "    private Node parse"+(int) token+"() throws SyntaxErrorException {\n"
                    + "    lookahead = input.charAt(0);\n"
                    + "\n"
                    +
                    (Character.isUpperCase(token)?
                            "        if(" + combinedFirstSet.stream().map(chr -> "lookahead != '"+chr+"'").collect(Collectors.joining(" && "))+") \n"
                                    +		"    	    	throw new SyntaxErrorException(\"Expected one of"
                                    + combinedFirstSet.stream().map(String::valueOf).collect(Collectors.joining(", "))
                                    + " but found \" + lookahead);\n"
                                    +		" 	     Node result = null;\n"
                                    +		firstSets.keySet().stream().filter(arr -> arr[0].charAt(0)== token).map(rule ->
                                    "        if("+firstSets.get(rule).stream().map(chr ->
                                            "lookahead == '"+chr+"'").collect(Collectors.joining(" || ")) + ")\n "
                                            +		"			result = new Node(\""+token+"\", "+rule[1].chars().mapToObj(chr -> "parse"+chr+"()").collect(Collectors.joining(", "))+");\n"
                            ).collect(Collectors.joining(""))
                                    +       "        return result;\n"

                            :
                            "        if(lookahead != '"+token+"')\n"
                                    +		"    	        throw new SyntaxErrorException(\"Expected "+token+" but found \"+lookahead);\n\n"
                                    +       "        input = input.substring(1);\n"
                                    +		"        return new Node(\""+token+"\");\n")
                    +		"    }\n";
        }).collect(Collectors.joining("\n"))
                + "}\n";
    }

    public Map<String[], Set<Character>> getFirstSets (String grammar) {
        final char deduct = grammar.charAt(0);
        final char separate = grammar.charAt(1);
        final List<String[]> rules = Stream.of(grammar.split("\\Q" + separate + "\\E"))
                .skip(1)
                .map(string -> string.split("\\Q" + deduct + "\\E", 2))
                .collect(Collectors.toList());
        final Map<String[], Set<Character>> firstSets = new HashMap<>();
        rules.stream()
                .forEach(rule -> firstSets.put(rule, new HashSet<>()));

        boolean[] changed = {true};
        while (changed[0]) {
            changed[0] = false;
            for (String[] rule : rules) {
                char first = rule[1].charAt(0);
                if (first >= 'A' && first <= 'Z') {
                    firstSets.keySet().stream()
                            .filter(otherRule -> otherRule[0].charAt(0) == first)
                            .forEach(otherRule ->
                                    changed[0] = firstSets.get(rule).addAll(firstSets.get(otherRule)) || changed[0]);
                } else {
                    changed[0] = firstSets.get(rule).add(first) || changed[0];
                }
            }
        }
        return firstSets;
    }
}
