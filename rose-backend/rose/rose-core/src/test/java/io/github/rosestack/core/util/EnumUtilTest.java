package io.github.rosestack.core.util;

import static io.github.rosestack.core.util.EnumUtilTest.Flag.CACHED_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.AssertionErrors.assertNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.AssertionErrors;

class EnumUtilTest {
    private static final Logger log = LoggerFactory.getLogger(EnumUtilTest.class);

    private EnumUtilTest() {}

    private static void assertBitSet(long bitSet, int startIdx, int range) {
        IntStream.range(0, startIdx).forEach(idx -> assertNotFlag(bitSet, CACHED_VALUES[idx]));
        IntStream.range(startIdx, startIdx + range).forEach(idx -> assertFlag(bitSet, CACHED_VALUES[idx]));
        IntStream.range(startIdx + range, CACHED_VALUES.length)
                .forEach(idx -> assertNotFlag(bitSet, CACHED_VALUES[idx]));
    }

    private static void assertFlag(long bitset, Flag flag) {
        AssertionErrors.assertTrue("Flag " + flag + " should be in bitset!", EnumUtil.hasEnum(bitset, flag));
    }

    private static void assertNotFlag(long bitset, Flag flag) {
        AssertionErrors.assertFalse("Flag " + flag + " should not be in bitset!", EnumUtil.hasEnum(bitset, flag));
    }

    private static void assertEquals(Object var0, Object var1) {
        AssertionErrors.assertEquals(null, var0, var1);
    }

    private static void assertTrue(boolean var0) {
        AssertionErrors.assertTrue(null, var0);
    }

    private static void assertFalse(boolean var0) {
        AssertionErrors.assertFalse(null, var0);
    }

    @Test
    public void testBitSetOf() {
        int startIdx = ThreadLocalRandom.current().nextInt(CACHED_VALUES.length - 4);
        Flag f1 = CACHED_VALUES[startIdx];
        Flag f2 = CACHED_VALUES[startIdx + 1];
        Flag f3 = CACHED_VALUES[startIdx + 2];
        Flag f4 = CACHED_VALUES[startIdx + 3];

        log.info("Flags: {}, {}, {}, {}", f1, f2, f3, f4);

        assertBitSet(EnumUtil.bitSetOf(f1), startIdx, 1);
        assertBitSet(EnumUtil.bitSetOf(f1, f2), startIdx, 2);
        assertBitSet(EnumUtil.bitSetOf(f1, f2, f3), startIdx, 3);
        assertBitSet(EnumUtil.bitSetOf(f1, f2, f3, f4), startIdx, 4);
    }

    @Test
    public void testEnumFromBitSet() {
        int startIdx = ThreadLocalRandom.current().nextInt(CACHED_VALUES.length - 4);
        Flag f1 = CACHED_VALUES[startIdx];
        Flag f2 = CACHED_VALUES[startIdx + 1];
        Flag f3 = CACHED_VALUES[startIdx + 2];
        Flag f4 = CACHED_VALUES[startIdx + 3];

        log.info("Flags: {}, {}, {}, {}", f1, f2, f3, f4);

        assertEquals(EnumSet.of(f1), EnumUtil.enumSetOf(EnumUtil.bitSetOf(f1), Flag.class));
        assertEquals(EnumSet.of(f1, f2), EnumUtil.enumSetOf(EnumUtil.bitSetOf(f1, f2), Flag.class));
        assertEquals(EnumSet.of(f1, f2, f3), EnumUtil.enumSetOf(EnumUtil.bitSetOf(f1, f2, f3), Flag.class));
        assertEquals(EnumSet.of(f1, f2, f3, f4), EnumUtil.enumSetOf(EnumUtil.bitSetOf(f1, f2, f3, f4), Flag.class));
    }

    @Test
    public void testEnumSet() {
        int startIdx = ThreadLocalRandom.current().nextInt(CACHED_VALUES.length - 4);
        Flag f1 = CACHED_VALUES[startIdx];
        Flag f2 = CACHED_VALUES[startIdx + 1];
        Flag f3 = CACHED_VALUES[startIdx + 2];
        Flag f4 = CACHED_VALUES[startIdx + 3];

        log.info("Flags: {}, {}, {}, {}", f1, f2, f3, f4);

        assertBitSet(EnumUtil.setEnum(EnumUtil.bitSetOf(f1), f2), startIdx, 2);
        assertBitSet(EnumUtil.setEnums(EnumUtil.bitSetOf(f1), Arrays.asList(f2, f3, f4)), startIdx, 4);
    }

    @Test
    public void testEnumUnset() {
        int startIdx = ThreadLocalRandom.current().nextInt(CACHED_VALUES.length - 4);
        Flag f1 = CACHED_VALUES[startIdx];
        Flag f2 = CACHED_VALUES[startIdx + 1];
        Flag f3 = CACHED_VALUES[startIdx + 2];

        log.info("Flags: {}, {}, {}", f1, f2, f3);

        assertBitSet(EnumUtil.unsetEnum(EnumUtil.bitSetOf(f1, f2, f3), f3), startIdx, 2);
    }

    @Test
    public void testBitSetOperations() {
        int startIdx = ThreadLocalRandom.current().nextInt(CACHED_VALUES.length - 4);
        Flag f1 = CACHED_VALUES[startIdx];
        Flag f2 = CACHED_VALUES[startIdx + 1];
        Flag f3 = CACHED_VALUES[startIdx + 2];
        Flag f4 = CACHED_VALUES[startIdx + 3];

        log.info("Flags: {}, {}, {}, {}", f1, f2, f3, f4);

        assertBitSet(EnumUtil.mergeBitSets(EnumUtil.bitSetOf(f1), EnumUtil.bitSetOf(f2, f3)), startIdx, 3);
        assertBitSet(EnumUtil.diffBitSets(EnumUtil.bitSetOf(f1, f2, f3, f4), EnumUtil.bitSetOf(f4)), startIdx, 3);

        assertTrue(EnumUtil.containsAll(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1, f2)));
        assertTrue(EnumUtil.containsAll(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1)));
        assertFalse(EnumUtil.containsAll(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1, f3)));
        assertFalse(EnumUtil.containsAll(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f4)));

        assertTrue(EnumUtil.containsAny(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1, f2)));
        assertTrue(EnumUtil.containsAny(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1)));
        assertTrue(EnumUtil.containsAny(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f1, f3)));
        assertFalse(EnumUtil.containsAny(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f3, f4)));
        assertFalse(EnumUtil.containsAny(EnumUtil.bitSetOf(f1, f2), EnumUtil.bitSetOf(f3)));
    }

    @Test
    public void testUniqueness() {
        Map<Long, Flag> bits = new HashMap<>(CACHED_VALUES.length);

        for (Flag flag : CACHED_VALUES) {
            Flag existing = bits.putIfAbsent(EnumUtil.bitSetOf(flag), flag);
            assertNull("Conflict flags: " + existing + " and " + flag, existing);
        }
    }

    @Test
    void testBitSetOfCollection() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.C);
        log.info("bitSet: {}", bitSet);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.A)).isTrue();
        assertThat(EnumUtil.hasEnum(bitSet, Flag.B)).isFalse();
        assertThat(EnumUtil.hasEnum(bitSet, Flag.C)).isTrue();
    }

    @Test
    void testBitSetOfSingleEnum() {
        long bitSet = EnumUtil.bitSetOf(Flag.B);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.B)).isTrue();
        assertThat(Long.bitCount(bitSet)).isEqualTo(1);
    }

    @Test
    void testBitSetOfTwoEnums() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.D);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.A)).isTrue();
        assertThat(EnumUtil.hasEnum(bitSet, Flag.D)).isTrue();
    }

    @Test
    void testBitSetOfVarargs() {
        long bitSet = EnumUtil.bitSetOf(CACHED_VALUES);
        for (Flag e : Flag.values()) {
            assertThat(EnumUtil.hasEnum(bitSet, e)).isTrue();
        }
    }

    @Test
    void testEnumSetOf() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.C);
        EnumSet<Flag> set = EnumUtil.enumSetOf(bitSet, Flag.class);
        assertThat(set).containsExactlyInAnyOrder(Flag.A, Flag.C);
    }

    @Test
    void testHasEnum() {
        long bitSet = EnumUtil.bitSetOf(Flag.B);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.B)).isTrue();
        assertThat(EnumUtil.hasEnum(bitSet, Flag.A)).isFalse();
    }

    @Test
    void testSetEnum() {
        long bitSet = EnumUtil.setEnum(0, Flag.D);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.D)).isTrue();
    }

    @Test
    void testUnsetEnum() {
        long bitSet = EnumUtil.bitSetOf(Flag.A);
        bitSet = EnumUtil.unsetEnum(bitSet, Flag.A);
        assertThat(EnumUtil.hasEnum(bitSet, Flag.A)).isFalse();
    }

    @Test
    void testMergeBitSets() {
        long bitSet1 = EnumUtil.bitSetOf(Flag.A);
        long bitSet2 = EnumUtil.bitSetOf(Flag.B);
        long merged = EnumUtil.mergeBitSets(bitSet1, bitSet2);
        assertThat(EnumUtil.hasEnum(merged, Flag.A)).isTrue();
        assertThat(EnumUtil.hasEnum(merged, Flag.B)).isTrue();
    }

    @Test
    void testDiffBitSets() {
        long bitSet1 = EnumUtil.bitSetOf(Flag.A, Flag.B);
        long bitSet2 = EnumUtil.bitSetOf(Flag.B);
        long diff = EnumUtil.diffBitSets(bitSet1, bitSet2);
        assertThat(EnumUtil.hasEnum(diff, Flag.A)).isTrue();
        assertThat(EnumUtil.hasEnum(diff, Flag.B)).isFalse();
    }

    @Test
    void testContainsAll() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.B);
        long testSet = EnumUtil.bitSetOf(Flag.A);
        assertThat(EnumUtil.containsAll(bitSet, testSet)).isTrue();
    }

    @Test
    void testContainsAny() {
        long bitSet = EnumUtil.bitSetOf(Flag.A);
        long testSet = EnumUtil.bitSetOf(Flag.A, Flag.B);
        assertThat(EnumUtil.containsAny(bitSet, testSet)).isTrue();
    }

    @Test
    void testNoneOf() {
        long bitSet = EnumUtil.bitSetOf(Flag.A);
        long testSet = EnumUtil.bitSetOf(Flag.B);
        assertThat(EnumUtil.noneOf(bitSet, testSet)).isTrue();
    }

    @Test
    void testBitSetSize() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.B);
        assertThat(EnumUtil.bitSetSize(bitSet)).isEqualTo(2);
    }

    @Test
    void testEnumArrayOf() {
        long bitSet = EnumUtil.bitSetOf(Flag.A, Flag.C);
        Flag[] array = EnumUtil.enumArrayOf(bitSet, Flag.class);
        assertThat(array).containsExactlyInAnyOrder(Flag.A, Flag.C);
    }

    @Test
    void testPrettyPrintBitSet() {
        long bitSet = EnumUtil.bitSetOf(Flag.B, Flag.D);
        String result = EnumUtil.prettyPrintBitSet(bitSet, Flag.class);
        assertThat(result).isEqualTo("[B, D]");
    }

    public enum Flag {
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        M,
        N,
        O,
        P,
        Q;

        public static final Flag[] CACHED_VALUES = Flag.values();

        private static Flag valueOf(int ordinal) {
            return CACHED_VALUES[ordinal];
        }
    }
}
