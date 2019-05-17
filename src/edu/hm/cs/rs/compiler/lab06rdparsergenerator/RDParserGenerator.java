package edu.hm.cs.rs.compiler.lab06rdparsergenerator;

/** Ein Generator fuer Recursive-Descent-Parser.
 * @author R. Schiedermeier, rs@cs.hm.edu
 * @version 2017-04-23
 */
public interface RDParserGenerator {
    /** Generiert den Sourcecode eines Recursive-Descent-Parsers aus einer LL(1)-Grammatik.
     * Die Grammatik ist epsilon-frei.
     * @param grammar Grammatik als String.
     * @return Java-Source eines Recursive-Descent-Parsers fuer die Grammatik.
     */
    String generate(String grammar);

    /** Sourcecode der Knotenklasse, aus denen Parsebaeume bestehen.
     * Dieser Code erfordert eine Importklausel von java.util.*.
     * @return Sourcecode der Klasse Node.
     */
    default String getNodeSourcecode() {
        return "class Node extends ArrayList<Node> {\n"
               + "    private final String name;\n"
               + "    public Node(String name, Node... children) {\n"
               + "        this.name = Objects.requireNonNull(name);\n"
               + "        addAll(Arrays.asList(children));\n"
               + "    }\n"
               + "    @Override public String toString() {\n"
               + "        return isEmpty()? name: name + super.toString();\n"
               + "    }\n"
               + "    void prettyPrint() {\n"
               + "        prettyPrint(1);\n"
               + "    }\n"
               + "    void prettyPrint(int depth) {\n"
               + "        System.out.printf(\"%\" + (depth * 4) + \"s%s%n\", \"\", name);\n"
               + "        forEach(child -> child.prettyPrint(depth + 1));\n"
               + "    }\n"
               + "}\n";
    }

    /** Sourcecode der Exceptionklasse,
     * die der generierte Parser bei Syntaxfehlern wirft.
     * Die Excpetion-Message sollte sinngemaess lauten "Expected (one of) x, y, z, but found a."
     * @return Sourcecode der Klasse SyntaxErrorException.
     */
    default String getSyntaxErrorExceptionSourcecode() {
        return "class SyntaxErrorException extends Exception {\n"
               + "    SyntaxErrorException(String message) {\n"
               + "        super(message);\n"
               + "    }\n"
               + "}\n";
    }

}
