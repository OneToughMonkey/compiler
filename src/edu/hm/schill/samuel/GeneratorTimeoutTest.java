package edu.hm.schill.samuel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class GeneratorTimeoutTest {

	/**
	 * Factor for adapting the timeouts to other environments.
	 * Set to 1.0 by default.
	 * The user is advised to set the value between 0.5 and 2.0.
	 */
	public static final double TIMEOUT_FACTOR = 1.5;

	private final LanguageGenerator generator;
	
	public GeneratorTimeoutTest() {
		generator = new MyLanguageGenerator();
	}

	/**
	 * Test method for testing the duration of calling the generate method of a LanguageGenerator.
	 * Verifies whether the method returns the right number of words (deterministic) and whether
	 * it terminates within the time limit [ms].
	 * Depending on the OS and the background processes, the result of timeout may vary with
	 * each test run. Therefore, the author recommends running this test method multiple times and
	 * evaluating the timeout of each test case on a statistical basis.
	 * @param numberOfWords the expected number of words. If the timeout does not cancel the test,
	 * 						the assert which compares the output of LanguageGenerator#generate to
	 * 						numberOfWords should succeed in deterministic fashion.
	 * @param milliseconds  the timeout in milliseconds.
	 * @param limit			the constraint concerning the maximum number of terminal symbols of
	 * 						each word returned by calling LanguageGenerator#generate.
	 * @param inputGrammar  the grammar according to which LanguageGenerator#generate returns its
	 * 						output.
	 */
	public void testTimeout(final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		final Stream<String[]> grammar = generator.read(inputGrammar);
		final long actualTimeout = (long)(TIMEOUT_FACTOR * milliseconds);
		assertTimeout(java.time.Duration.ofMillis(actualTimeout), () -> testWordNumber(numberOfWords, grammar, limit));
	}
	
	public void testTimeout(final boolean ignoreTimeout, final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		if(ignoreTimeout) {
			testWordNumber(numberOfWords, generator.read(inputGrammar), limit);
		} else {
			testTimeout(numberOfWords, milliseconds, limit, inputGrammar);
		}
	}

	@ParameterizedTest
	@MethodSource("provideGivenExamples")
	public void testGivenExamples(final String grammar, final int limit, final String[] samples) {
		assertEquals(Arrays.asList(samples), generator.generate(generator.read(grammar), limit).collect(Collectors.toList()));
	}

	@ParameterizedTest
	@MethodSource("provideEasy")
	public void testEasyCases(final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		testTimeout(numberOfWords, milliseconds, limit, inputGrammar);
	}
	@ParameterizedTest
	@MethodSource("provideMedium")
	public void testMediumCases(final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		testTimeout(numberOfWords, milliseconds, limit, inputGrammar);
	}
	@ParameterizedTest
	@MethodSource("provideHard")
	public void testHardCases(final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		testTimeout(numberOfWords, milliseconds, limit, inputGrammar);
	}
	@ParameterizedTest
	@MethodSource("provideExtreme")
	public void testExtremeCases(final long numberOfWords, final long milliseconds, final int limit, final String inputGrammar) {
		testTimeout(numberOfWords, milliseconds, limit, inputGrammar);
	}
	private void testWordNumber(final long numberOfWords, final Stream<String[]> grammar, final int limit) {
		final long actualCount = generator.generate(grammar, limit).count();
		assertEquals(numberOfWords, actualCount);
	}
	
	public static Stream<Arguments> provideGivenExamples() {
		return Stream.of(
				Arguments.of("=;S=();S=(S);S=()S;S=(S)S", 4, new String[] {"()", "(())", "()()"}),
				Arguments.of(":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb", 4, new String[] {"", "a", "b", "aa", "bb", "aaa", "aba", "bab", "bbb", "aaaa", "abba", "baab", "bbbb"}),
				Arguments.of(">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab", 6, new String[] {"abc", "aabbcc"}),
				Arguments.of(":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n", 4, new String[] {"", "pi;", "pn;", "i=i;", "i=n;", "p-i;", "p-n;"}),
				Arguments.of("=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC", 4, new String[] {"e", "oc", "oec", "otc", "oeec", "oetc", "oocc", "otec", "ottc"}),
				Arguments.of("=,S=AA,A=d,Ad=cd", 2, new String[] {"cd", "dd"}),
				Arguments.of("=,S=ABABA,ABA=aaa,BAB=bbb,ABa=ccc,aBA=ddd,Ab=ee,bA=ff", 5, new String[] {"aaddd", "cccaa", "eebff"})
				);
	}
	public static Stream<Arguments> provideEasy() {
		return Stream.of(
				Arguments.of(13, 100, 4, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(21, 120, 5, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(29, 130, 6, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(45, 150, 7, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(61, 280, 8, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(93, 400, 9, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(1, 40, 4, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(1, 60, 5, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(2, 90, 6, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(2, 140, 7, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(2, 210, 8, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(3, 300, 9, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab"),
				Arguments.of(17, 100, 5, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(43, 130, 6, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(115, 150, 7, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(305, 200, 8, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(819, 350, 9, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(9, 50, 4, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(23, 50, 5, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(65, 50, 6, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(197, 100, 7, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(626, 150, 8, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(2056, 200, 9, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(21, 50, 4, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(21, 50, 5, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(156, 20, 6, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(156, 20, 7, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(1290, 200, 8, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(1290, 200, 9, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(11496, 250, 10, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S"),
				Arguments.of(791, 20, 4, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=2;T=3;T=4;T=5;T=6;T=7;T=8;T=9;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(63, 20, 4, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(395, 25, 5, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(938, 50, 6, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(4818, 300, 7, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(40, 100, 4, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(121, 100, 5, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(364, 100, 6, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(1093, 150, 7, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(3280, 150, 8, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(9841, 200, 9, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(5, 20, 4, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(21, 20, 5, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(21, 20, 6, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(101, 80, 7, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(101, 80, 8, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(19731, 300, 5, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=2;T=3;T=4;T=5;T=6;T=7;T=8;T=9;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(29524, 300, 10, "=;A=aB;A=aC;A=aD;A=a;B=bA;B=bC;B=bD;B=b;C=cA;C=cB;C=cD;C=c;D=dA;D=dB;D=dC;D=d"),
				Arguments.of(3, 300, 10, ">/S>aSBc/S>aBc/cB>Bc/bB>Bb/aB>ab")
				);
	}
	
	public static Stream<Arguments> provideMedium() {
		return Stream.of(
				Arguments.of(125, 600, 10, ":,P:,P:Q,Q:a,Q:b,Q:aa,Q:bb,Q:aQa,Q:bQb"),
				Arguments.of(6918, 700, 10, "=,D=E,E=e,E=oc,E=oCc,C=t,C=tC,C=E,C=EC"),
				Arguments.of(7, 700, 4, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(2191, 700, 10, ":,P:,P:pE;,P:i=E;,P:SS,S=SS,S:pE;,S:i=E;,E:E+E,E:(E),E:-E,E:i,E:n"),
				Arguments.of(49266, 900, 6, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=2;T=3;T=4;T=5;T=6;T=7;T=8;T=9;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(14952, 1000, 8, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(549, 1000, 10, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d")
				);
		}

	public static Stream<Arguments> provideHard() {
		return Stream.of(
				Arguments.of(549, 2000, 9, "-:A-BCD:CD-AC:BC-AC:A-a:B-b:C-c:D-d"),
				Arguments.of(63956, 2000, 9, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(1022, 2500, 18, "=;S=aCF;S=bCG;S=aa;S=bb;C=aCA;C=bCB;C=aA;C=bB;C=E;AF=aF;AG=bF;BF=aG;BG=bG;Aa=aA;Ab=bA;Ba=aB;Bb=bB;aaF=aaa;abF=aba;baF=baa;bbF=bba;aaG=aab;abG=abb;baG=bab;bbG=bbb"),
				Arguments.of(1398101, 7000, 11, "=;A=aB;A=aC;A=aD;A=aE;A=a;B=bA;B=bC;B=bD;B=bE;B=b;C=cA;C=cB;C=cD;C=cE;C=c;D=dA;D=dB;D=dC;D=dE;D=d;E=eA;E=eB;E=eC;E=eD;E=e"),
				Arguments.of(239570, 9000, 10, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(830266, 9000, 7, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=2;T=3;T=4;T=5;T=6;T=7;T=8;T=9;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(2046, 10000, 20, "=;S=aCF;S=bCG;S=aa;S=bb;C=aCA;C=bCB;C=aA;C=bB;AF=aF;AG=bF;BF=aG;BG=bG;Aa=aA;Ab=bA;Ba=aB;Bb=bB;aaF=aaa;abF=aba;baF=baa;bbF=bba;aaG=aab;abG=abb;baG=bab;bbG=bbb"));
	}
	
	public static Stream<Arguments> provideExtreme() {
		return Stream.of(
				Arguments.of(82499, 30000, 22, "=;S=();S=(S);S=()S;S=(S)S"),
				Arguments.of(5592405, 50000, 12, "=;A=aB;A=aC;A=aD;A=aE;A=a;B=bA;B=bC;B=bD;B=bE;B=b;C=cA;C=cB;C=cD;C=cE;C=c;D=dA;D=dB;D=dC;D=dE;D=d;E=eA;E=eB;E=eC;E=eD;E=e"),
				Arguments.of(2784400, 50000, 8, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=2;T=3;T=4;T=5;T=6;T=7;T=8;T=9;T=0;T=TOT;O=+;O=-;O=*;O=/"),
				Arguments.of(941562, 100000, 11, "=;S=();S=(S);S=()S;S=(S)S;S=[];S=[S];S=[]S;S=[S]S;S={};S={S};S={}S;S={S}S;S=T;T=1;T=0;T=TOT;O=+;O=-;O=*;O=/")
				);
	}

}
