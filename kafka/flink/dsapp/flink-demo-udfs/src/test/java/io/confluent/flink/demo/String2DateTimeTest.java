package io.confluent.flink.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author venky
 */
public class String2DateTimeTest {
    @Test
    public void testNullOrEmpty() {
        assertThat(new String2DateTime().eval((byte[])null)).isNull();
        assertThat(new String2DateTime().eval((String)null)).isNull();
        assertThat(new String2DateTime().eval("")).isNull();
        assertThat(new String2DateTime().eval("".getBytes())).isNull();
        assertThat(new String2DateTime().eval(" ")).isNull();
        assertThat(new String2DateTime().eval(" ".getBytes())).isNull();
    }

    @Test
    public void testBadInput() {
        assertThat(new String2DateTime().eval("abcd")).isNull();
        assertThat(new String2DateTime().eval("abcd".getBytes())).isNull();
        assertThat(new String2DateTime().eval("1234")).isNull();
        assertThat(new String2DateTime().eval("1234".getBytes())).isNull();
        assertThat(new String2DateTime().eval("20250721 011040")).isNull();
        assertThat(new String2DateTime().eval("20250721 01:10:40")).isNull();
        assertThat(new String2DateTime().eval("2025-07-21 01:10:40")).isNull();
        assertThat(new String2DateTime().eval("-1")).isNull();
        assertThat(new String2DateTime().eval("1753074640234000000")).isNull();
        assertThat(new String2DateTime().eval("1753074640234000000".getBytes())).isNull();
    }

    @Test
    public void testyyyyMMdd() {
        assertThat(new String2DateTime().eval("20250721"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(0)
            .hasMinute(0)
            .hasSecond(0)
            .hasMillisecond(0);
        assertThat(new String2DateTime().eval("20250721".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(0)
            .hasMinute(0)
            .hasSecond(0)
            .hasMillisecond(0);
    }

    @Test
    public void testyyyyMMddWithDash() {
        assertThat(new String2DateTime().eval("2025-07-21"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(0)
            .hasMinute(0)
            .hasSecond(0)
            .hasMillisecond(0);
        assertThat(new String2DateTime().eval("2025-07-21".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(0)
            .hasMinute(0)
            .hasSecond(0)
            .hasMillisecond(0);
    }

    @Test
    public void testyyyyMMddhhmmssSSS() {
        assertThat(new String2DateTime().eval("20250721 011040.234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
        assertThat(new String2DateTime().eval("20250721 011040.234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
    }

    @Test
    public void testyyyyMMddhhmmssSSSWithDash() {
        assertThat(new String2DateTime().eval("2025-07-21 01:10:40.234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
        assertThat(new String2DateTime().eval("2025-07-21 01:10:40.234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
    }

    @Test
    public void testEpochDecimal() {
        assertThat(new String2DateTime().eval("1753074640.234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);

        assertThat(new String2DateTime().eval("1753074640.234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);

        assertThat(new String2DateTime().eval("1753074640000.234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(0);

        assertThat(new String2DateTime().eval("1753074640000.234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(0);
    }

    @Test
    public void testEpoch() {
        assertThat(new String2DateTime().eval("1753074640"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(0);
        assertThat(new String2DateTime().eval("1753074640".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(0);

        assertThat(new String2DateTime().eval("1753074640234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
        assertThat(new String2DateTime().eval("1753074640234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);

        assertThat(new String2DateTime().eval("1753074640234"))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
        assertThat(new String2DateTime().eval("1753074640234".getBytes()))
            .hasYear(2025)
            .hasMonth(7)
            .hasDayOfMonth(21)
            .hasHourOfDay(1)
            .hasMinute(10)
            .hasSecond(40)
            .hasMillisecond(234);
    }
}
