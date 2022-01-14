package se.sundsvall.disturbance.service.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

	private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private DateUtils() {}

	/**
	 * This method converts the provided OffsetDateTime object into a new OffsetDateTime with the local offset.
	 * 
	 * E.g. if local timezone is "Europe/Stockholm", this would be the result for the provided dates:
	 * 
	 * <pre>
	 * Zulu time zone into time with local offset +1h.
	 * 2021-11-10T09:23:42.500Z -> 2021-11-10T10:23:42.500+01:00
	 * 
	 * Zulu time zone into time with local offset +2h, with DST.
	 * 2021-06-10T09:23:42.500Z -> 2021-06-10T11:23:42.500+02:00
	 * 
	 * Time with offset +4h into time with local offset +1h. 
	 * 2021-11-10T12:23:42.500+04:00 -> 2021-11-10T09:23:42.500+01:00
	 * </pre>
	 * 
	 * @param offsetDateTime
	 * @return a new offsetDateTime with the local offset. If the date is null, null is returned.
	 */
	public static OffsetDateTime toOffsetDateTimeWithLocalOffset(OffsetDateTime offsetDateTime) {
		if (isNull(offsetDateTime)) {
			return null;
		}

		// Calculate local offset based on provided offsetDateTime, for this systems zoneId.
		final var localOffset = ZoneId.systemDefault().getRules().getOffset(offsetDateTime.toInstant());
		return offsetDateTime.withOffsetSameInstant(localOffset);
	}

	/**
	 * Formats a date on the format "yyyy-MM-dd HH:mm" to be used in the Message to send. If the date is null, "N/A" is
	 * returned.
	 * 
	 * @param date
	 * @return A formatted date on format "yyyy-MM-dd HH:mm".
	 */
	public static String toMessageDateFormat(OffsetDateTime date) {
		return nonNull(date) ? MESSAGE_DATE_FORMAT.format(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())) : "N/A";
	}
}
