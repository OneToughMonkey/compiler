package edu.hm.schill.samuel;

import edu.hm.cs.rs.compiler.toys.base.BaseScanner;

/**
 * Scanner fuer die dritte Praktikumsaufgabe.
 * @version 0.1
 */
public class MyScanner extends BaseScanner {
    /**
     * Lowercase letters.
     */
    public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
    /**
     * Digits.
     */
    public static final String DIGITS = "0123456789";
    /**
     * Alphanumeric characters.
     */
    public static final String ALNUM = LETTER + DIGITS;
    /**
     * Whitespace characters.
     */
    public static final String WHITESPACE_CHARS = " \t\n\f";
    /**
     * Alphanumeric characters without "p" or "i".
     */
    public static final String LETTER_BUT_PI = LETTER.substring(0, 'i' - 'a') + LETTER.substring('i' - 'a' + 1, 'p' - 'a')
            + LETTER.substring('p' - 'a' + 1);
    /**
     * Alphanumeric characters without "r".
     */
    public static final String ALNUM_BUT_R = ALNUM.substring(0, 'r' - 'a') + ALNUM.substring('r' - 'a' + 1);
    /**
     * Alphanumeric characters without "i".
     */
    public static final String ALNUM_BUT_I = ALNUM.substring(0, 'i' - 'a') + ALNUM.substring('i' - 'a' + 1);
    /**
     * Alphanumeric characters without "n".
     */
    public static final String ALNUM_BUT_N = ALNUM.substring(0, 'n' - 'a') + ALNUM.substring('n' - 'a' + 1);
    /**
     * Alphanumeric characters without "t".
     */
    public static final String ALNUM_BUT_T = ALNUM.substring(0, 't' - 'a') + ALNUM.substring('t' - 'a' + 1);
    /**
     * The identifier token string.
     */
    public static final String ID_TOKEN = "identifier";

    /**
     * Possible states.
     */
    public enum State {
        START(null),
        INT1(ID_TOKEN), INT2(ID_TOKEN), INT3("int"),
        PRINT1(ID_TOKEN), PRINT2(ID_TOKEN), PRINT3(ID_TOKEN), PRINT4(ID_TOKEN), PRINT5("print"),
        IDENTIFIER(ID_TOKEN), NUMERAL("numeral"),
        ASSIGN1(null), ASSIGN2("assign"),
        ADD("add"), SUB("sub"), MULT("mult"), POT("pot"), DIV("div"), MOD("mod"), OPEN("open"), CLOSE("close"),
        SEMICOLON("semicolon"), WHITESPACE("whitespace");

        /**
         * The token that this State can generate.
         */
        private final String token;
        State(String token) { this.token = token; }

        /**
         * Returns the token that this State can generate.
         * @return The token that this State can generate.
         */
        public String getToken() {
            return token;
        }
    }

    /**
     * Configures the Scanner.
     */
    public MyScanner() {
        start(State.START.name());

        transition(State.START.name(), LETTER_BUT_PI, State.IDENTIFIER.name());
        transition(State.IDENTIFIER.name(), ALNUM, State.IDENTIFIER.name());

        transition(State.START.name(), "i", State.INT1.name());
        transition(State.INT1.name(), "n", State.INT2.name());
        transition(State.INT1.name(), ALNUM_BUT_N, State.IDENTIFIER.name());
        transition(State.INT2.name(), "t", State.INT3.name());
        transition(State.INT2.name(), ALNUM_BUT_T, State.IDENTIFIER.name());
        transition(State.INT3.name(), ALNUM, State.IDENTIFIER.name());

        transition(State.START.name(), "p", State.PRINT1.name());
        transition(State.PRINT1.name(), "r", State.PRINT2.name());
        transition(State.PRINT1.name(), ALNUM_BUT_R, State.IDENTIFIER.name());
        transition(State.PRINT2.name(), "i", State.PRINT3.name());
        transition(State.PRINT2.name(), ALNUM_BUT_I, State.IDENTIFIER.name());
        transition(State.PRINT3.name(), "n", State.PRINT4.name());
        transition(State.PRINT3.name(), ALNUM_BUT_N, State.IDENTIFIER.name());
        transition(State.PRINT4.name(), "t", State.PRINT5.name());
        transition(State.PRINT4.name(), ALNUM_BUT_T, State.PRINT5.name());
        transition(State.PRINT5.name(), ALNUM, State.IDENTIFIER.name());

        transition(State.START.name(), DIGITS, State.NUMERAL.name());
        transition(State.NUMERAL.name(), DIGITS, State.NUMERAL.name());

        transition(State.START.name(), ":", State.ASSIGN1.name());
        transition(State.ASSIGN1.name(), "=", State.ASSIGN2.name());

        transition(State.START.name(), "+", State.ADD.name());
        transition(State.START.name(), "-", State.SUB.name());

        transition(State.START.name(), "*", State.MULT.name());
        transition(State.MULT.name(), "*", State.POT.name());

        transition(State.START.name(), "/", State.DIV.name());
        transition(State.START.name(), "%", State.MOD.name());
        transition(State.START.name(), "(", State.OPEN.name());
        transition(State.START.name(), ")", State.CLOSE.name());
        transition(State.START.name(), ";", State.SEMICOLON.name());

        transition(State.START.name(), WHITESPACE_CHARS, State.WHITESPACE.name());
        transition(State.WHITESPACE.name(), WHITESPACE_CHARS, State.WHITESPACE.name());

        for (State state : State.values())
            if (state.equals(State.WHITESPACE))
                acceptAndIgnore(state.name());
            else if (state.getToken() != null)
                accept(state.name(), state.getToken());
        accept(ID_TOKEN, true);
        accept("numeral", true);
    }
}
