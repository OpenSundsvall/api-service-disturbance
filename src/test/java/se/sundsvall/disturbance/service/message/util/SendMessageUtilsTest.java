package se.sundsvall.disturbance.service.message.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import generated.se.sundsvall.messaging.Sender;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;

class SendMessageUtilsTest {

	@ParameterizedTest
	@CsvSource({
		// All these parameter rows will be processed in the test below, one by one.
		// Each row will trigger a new invocation of the test method, with the method params: partyId,reference
		"partyId-1,reference-1",
		"partyId-2,reference-2",
		"partyId-3,reference-3",
		"partyId-4,reference-4",
		"partyId-5,reference-5",
		"partyId-6,reference-6",
	})
	void getReferenceByPartyId(String partyId, String expectedReference) {

		// Set up a list of AffectedEntities.
		final var affected1 = new AffectedEntity();
		affected1.setPartyId("partyId-1");
		affected1.setReference("reference-1");

		final var affected2 = new AffectedEntity();
		affected2.setPartyId("partyId-2");
		affected2.setReference("reference-2");

		final var affected3 = new AffectedEntity();
		affected3.setPartyId("partyId-3");
		affected3.setReference("reference-3");

		final var affected4 = new AffectedEntity();
		affected4.setPartyId("partyId-4");
		affected4.setReference("reference-4");

		final var affected5 = new AffectedEntity();
		affected5.setPartyId("partyId-5");
		affected5.setReference("reference-5");

		final var affected6 = new AffectedEntity();
		affected6.setPartyId("partyId-6");
		affected6.setReference("reference-6");

		final var affectedEntityList = List.of(affected1, affected2, affected3, affected4, affected5, affected6);

		final var fetchedReference = SendMessageUtils.getReferenceByPartyId(affectedEntityList, partyId);

		assertThat(fetchedReference).isEqualTo(expectedReference);
	}

	@Test
	void getReferenceByPartyIdReturnsEmptyStringWhenNotFound() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var result = SendMessageUtils.getReferenceByPartyId(List.of(affectedEntity1, affectedEntity2), "does-not-exist-in-list");

		assertThat(result).isEmpty();
	}

	@Test
	void createMessage() {

		final var sender = new Sender()
			.emailAddress("senderEmailAddress")
			.emailName("senderEmailAddress")
			.smsName("smsName");
		final var partyId = "partyId";
		final var subject = "subject";
		final var messageText = "message";

		final var message = SendMessageUtils.createMessage(sender, partyId, subject, messageText);

		assertThat(message).isNotNull();
		assertThat(message.getSender()).isNotNull();
		assertThat(message.getSender().getEmailName()).isEqualTo(sender.getEmailName());
		assertThat(message.getSender().getEmailAddress()).isEqualTo(sender.getEmailAddress());
		assertThat(message.getSender().getSmsName()).isEqualTo(sender.getSmsName());
		assertThat(message.getPartyId()).isEqualTo(partyId);
		assertThat(message.getSubject()).isEqualTo(subject);
		assertThat(message.getMessage()).isEqualTo(messageText);
	}
}
