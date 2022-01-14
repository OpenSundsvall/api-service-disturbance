package se.sundsvall.disturbance.service.message.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.List;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.Sender;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;

public class SendMessageUtils {

	private SendMessageUtils() {}

	/**
	 * Return the reference attribute value from the AffectedEntity that matches the provided partyId. The reference
	 * attribute holds the customer specific information. Such as street name connection-point, etc.
	 * 
	 * @param affectedEntities
	 * @param partyId          (same as personId or organizationId)
	 * @return The matching reference value or an empty string if nothing was found.
	 */
	public static String getReferenceByPartyId(List<AffectedEntity> affectedEntities, String partyId) {
		return ofNullable(affectedEntities).orElse(emptyList()).stream()
			.filter(affectedEntity -> affectedEntity.getPartyId().equalsIgnoreCase(partyId))
			.map(AffectedEntity::getReference)
			.findFirst()
			.orElse(EMPTY);
	}

	/**
	 * Create a Message object from provided parameters.
	 * 
	 * @param senderName
	 * @param senderEmailAdress
	 * @param partyId           (same as personId or organizationId)
	 * @param subject
	 * @param messageText
	 * @return A Message object.
	 */
	public static Message createMessage(Sender sender, String partyId, String subject, String messageText) {
		return new Message()
			.sender(sender)
			.partyId(partyId)
			.message(messageText)
			.subject(subject);
	}
}
