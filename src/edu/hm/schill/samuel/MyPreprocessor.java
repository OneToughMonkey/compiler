package edu.hm.schill.samuel;

import edu.hm.cs.rs.compiler.toys.base.LexicalError;
import edu.hm.cs.rs.compiler.toys.base.Source;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Praktikumsaufgabe 1
 * Loescht Kommentare aus der Eingabe und gibt den Rest weiter.
 * @author Sam Schill, samuel.schill@hm.edu
 * @version 1.0
 */

public class MyPreprocessor implements edu.hm.cs.rs.compiler.toys.base.Preprocessor {
    /**
     * Possible States.
     */
    private enum State {
        CODE, EXPECT_COMMENT, SIMPLE_COMMENT, BLOCK_COMMENT, EXPECT_BLOCK_END
    }

    /**
     * Maps a State and a Char to a BiFunction that processes a Character
     * and a Source object and returns a new State.
     */
    private static final
    Map<State, Map<Character, BiFunction<Character, Source, State>>>
            PROCESSORS = new EnumMap<>(State.class);
    /**
     * Maps a State to an optional BiFunction that takes potentially necessary
     * finalization actions on a Source object for the State.
     * An empty Optional indicates an invalid terminal State
     */
    private static final
    Map<State, Optional<Consumer<Source>>>
            FINALIZERS = new EnumMap<>(State.class);

    /**
     * The current State.
     */
    private State state = State.CODE;

    @Override public Source process(Source incoming) throws LexicalError {
        final Source outgoing = new Source();
        try {
            Stream.generate(incoming::hasMore)
                    .takeWhile(Boolean::booleanValue)
                    .map(token -> incoming.getNextChar())
                    .forEach(chr -> state = PROCESSORS.get(state)
                            .getOrDefault(chr, PROCESSORS.get(state).get('.'))
                            .apply(chr, outgoing));

            FINALIZERS.get(state).orElseThrow(LexicalError::new).accept(outgoing);
        } finally {
            state = State.CODE;
        }
        return outgoing;
    }

    static {
        // initialize the maps
        Stream.of(State.values()).forEach(state -> {
            PROCESSORS.put(state, new HashMap<>());
            FINALIZERS.put(state, Optional.empty());
        });

        final BiConsumer<Character, Source> charAppender = (chr, src) -> src.append(chr);

        /* concatenates a BiConsumer that accepts a Character and a Source object
         * with a State return value to a BiFunction */
        final BiFunction<BiConsumer<Character, Source>, State, BiFunction<Character, Source, State>>
                processorMaker = (appender, newState) -> (chr, src) -> {
            Objects.requireNonNullElse(appender, (chr2, src2) -> {}).accept(chr, src); // fall back to a no-op appender
            return newState;
        };

        // a '/' triggers the EXPECT_COMMENT state and does not get printed right away
        PROCESSORS.get(State.CODE).put('/', processorMaker.apply(null, State.EXPECT_COMMENT));
        // '.' is our default/wildcard
        PROCESSORS.get(State.CODE).put('.', processorMaker.apply(charAppender, State.CODE));

        PROCESSORS.get(State.EXPECT_COMMENT).put('/', processorMaker.apply(null, State.SIMPLE_COMMENT));
        PROCESSORS.get(State.EXPECT_COMMENT).put('*', processorMaker.apply(null, State.BLOCK_COMMENT));
        PROCESSORS.get(State.EXPECT_COMMENT).put('.', processorMaker.apply( // not a comment after all
                (chr, src) -> src.append('/' + chr), // print the '/' we skipped followed by current char
                State.CODE                          // back to business as usual
        ));

        PROCESSORS.get(State.SIMPLE_COMMENT).put('\n', processorMaker.apply(charAppender, State.CODE));
        PROCESSORS.get(State.SIMPLE_COMMENT).put('.', processorMaker.apply(null, State.SIMPLE_COMMENT));

        // conserve newlines in block comments
        PROCESSORS.get(State.BLOCK_COMMENT).put('\n', processorMaker.apply(charAppender, State.BLOCK_COMMENT));
        PROCESSORS.get(State.BLOCK_COMMENT).put('*', processorMaker.apply(null, State.EXPECT_BLOCK_END));
        PROCESSORS.get(State.BLOCK_COMMENT).put('.', processorMaker.apply(null, State.BLOCK_COMMENT));

        PROCESSORS.get(State.EXPECT_BLOCK_END).put('/', processorMaker.apply(
                (chr, src) -> src.append(' '), // insert space after end of block comment
                State.CODE
        ));
        PROCESSORS.get(State.EXPECT_BLOCK_END).put('.', processorMaker.apply(null, State.BLOCK_COMMENT));

        FINALIZERS.put(State.CODE, Optional.of(src -> {})); // no finalization action necessary
        FINALIZERS.put(State.EXPECT_COMMENT, Optional.of(src -> src.append('/'))); // print the '/' we skipped
    }
}