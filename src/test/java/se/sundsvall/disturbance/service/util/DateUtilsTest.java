package se.sundsvall.disturbance.service.util;

import static java.time.OffsetDateTime.now;
import static java.time.OffsetDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DateUtilsTest {

	@Test
	void toOffsetDateTimeWithLocalOffset() {

		// Zulu time zone into time with local offset +1h.
		assertThat(DateUtils.toOffsetDateTimeWithLocalOffset(parse("2021-11-10T09:23:42.500Z"))).hasToString("2021-11-10T10:23:42.500+01:00");

		// Zulu time zone into time with local offset +2h, with DST.
		assertThat(DateUtils.toOffsetDateTimeWithLocalOffset(parse("2021-06-10T09:23:42.500Z"))).hasToString("2021-06-10T11:23:42.500+02:00");

		// Time with offset +4h into time with local offset +1h.
		assertThat(DateUtils.toOffsetDateTimeWithLocalOffset(parse("2021-11-10T12:23:42.500+04:00"))).hasToString("2021-11-10T09:23:42.500+01:00");

		// Time with offset -1h into time with local offset +1h.
		assertThat(DateUtils.toOffsetDateTimeWithLocalOffset(parse("2021-11-10T12:23:42.500-01:00"))).hasToString("2021-11-10T14:23:42.500+01:00");

		// Null input renders null output.
		assertThat(DateUtils.toOffsetDateTimeWithLocalOffset(null)).isNull();
	}

	@Test
	void toMessageDateFormat() {
		final var offsetDateTime = LocalDateTime.of(2022, 10, 11, 12, 13, 14).atOffset(now().getOffset());
		assertThat(DateUtils.toMessageDateFormat(offsetDateTime)).isEqualTo("2022-10-11 13:13");
		assertThat(DateUtils.toMessageDateFormat(null)).isEqualTo("N/A");
	}
}
