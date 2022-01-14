package se.sundsvall.disturbance.service.message;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.Sender;
import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackHistoryRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.messaging.ApiMessagingClient;
import se.sundsvall.disturbance.service.message.configuration.MessageConfiguration;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@ExtendWith(MockitoExtension.class)
class SendMessageLogicTest {

	private static final Category CATEGORY = Category.ELECTRICITY;
	private static final String DISTURBANCE_ID = "disturbanceId";
	private static final String DESCRIPTION = "Major disturbance in the central parts of town";
	private static final OffsetDateTime PLANNED_START_DATE = LocalDateTime.of(2021, 11, 1, 12, 0, 6).atOffset(now().getOffset());
	private static final OffsetDateTime PLANNED_STOP_DATE = LocalDateTime.of(2021, 11, 10, 18, 30, 8).atOffset(now().getOffset());
	private static final String STATUS = Status.OPEN.toString();
	private static final String TITLE = "Disturbance";

	@Captor
	private ArgumentCaptor<MessageRequest> messageRequestCaptor;

	@Captor
	private ArgumentCaptor<DisturbanceFeedbackEntity> disturbanceFeedbackEntityCaptor;

	@Mock
	private DisturbanceFeedbackRepository disturbanceFeedBackRepositoryMock;

	@Mock
	private DisturbanceFeedbackHistoryRepository disturbanceFeedBackHistoryRepositoryMock;

	@Mock
	private MessageConfiguration messageConfigurationMock;

	@Mock
	private ApiMessagingClient apiMessagingClientMock;

	@InjectMocks
	private SendMessageLogic sendMessageLogic;

	@Test
	void sendCloseMessageToAllApplicableAffecteds() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList("2", "4", "6"));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).persistWithStatusSent(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-2")
				.subject("Close subject for reference-2")
				.message("Close message for reference-2"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-4")
				.subject("Close subject for reference-4")
				.message("Close message for reference-4"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-6")
				.subject("Close subject for reference-6")
				.message("Close message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackEntityCaptor.getAllValues()).hasSize(3);

		final var feedbackHistory1 = disturbanceFeedbackEntityCaptor.getAllValues().get(0);
		assertThat(feedbackHistory1.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory1.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory1.getPartyId()).isEqualTo("partyId-2");

		final var feedbackHistory2 = disturbanceFeedbackEntityCaptor.getAllValues().get(1);
		assertThat(feedbackHistory2.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory2.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory2.getPartyId()).isEqualTo("partyId-4");

		final var feedbackHistory3 = disturbanceFeedbackEntityCaptor.getAllValues().get(2);
		assertThat(feedbackHistory3.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory3.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory3.getPartyId()).isEqualTo("partyId-6");
	}

	@Test
	void sendCloseMessageToAllApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() throws ServiceException {

		// Set up disturbanceEntity with 6 affected affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffecteds() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let 3 of these affecteds have an disturbanceFeedbackEntity.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList("2", "4", "6"));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		// AffectedEntity1. This entity have a disturbanceFeedbackEntity.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-4");
		affectedEntity1.setReference("reference-4");

		// AffectedEntity1. This entity doesn't have a disturbanceFeedbackEntity.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-5");
		affectedEntity2.setReference("reference-5");

		// Define a AffectedEntity-override list with two elements, where one of them has a disturbanceFeedbackEntity (id=4).
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(messageConfigurationMock).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock).persistWithStatusSent(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(1);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-4")
				.subject("Close subject for reference-4")
				.message("Close message for reference-4"));
		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackEntityCaptor.getAllValues()).hasSize(1);

		final var feedbackHistory1 = disturbanceFeedbackEntityCaptor.getValue();
		assertThat(feedbackHistory1.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory1.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory1.getPartyId()).isEqualTo("partyId-4");
	}

	@Test
	void sendCloseMessageToProvidedApplicableAffectedsWhenNoAffectedsHasDisturbanceFeedback() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		// Define a AffectedEntity-override list with two elements.
		final var affectedEntitiesOverride = new ArrayList<AffectedEntity>();

		// AffectedEntity1.
		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		// AffectedEntity1.
		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		affectedEntitiesOverride.addAll(List.of(affectedEntity1, affectedEntity2));

		sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(disturbanceEntity, affectedEntitiesOverride);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock, messageConfigurationMock);
	}

	@Test
	void sendUpdateMessage() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList("2", "4", "6"));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).persistWithStatusSent(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-2")
				.subject("Update subject for reference-2")
				.message("Update message for reference-2. Planned stop date 2021-11-10 18:30"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-4")
				.subject("Update subject for reference-4")
				.message("Update message for reference-4. Planned stop date 2021-11-10 18:30"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-6")
				.subject("Update subject for reference-6")
				.message("Update message for reference-6. Planned stop date 2021-11-10 18:30"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackEntityCaptor.getAllValues()).hasSize(3);

		final var feedbackHistory1 = disturbanceFeedbackEntityCaptor.getAllValues().get(0);
		assertThat(feedbackHistory1.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory1.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory1.getPartyId()).isEqualTo("partyId-2");

		final var feedbackHistory2 = disturbanceFeedbackEntityCaptor.getAllValues().get(1);
		assertThat(feedbackHistory2.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory2.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory2.getPartyId()).isEqualTo("partyId-4");

		final var feedbackHistory3 = disturbanceFeedbackEntityCaptor.getAllValues().get(2);
		assertThat(feedbackHistory3.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory3.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory3.getPartyId()).isEqualTo("partyId-6");
	}

	@Test
	void sendUpdateMessageWherePlannedStartAndStopDatesAreNotSet() throws ServiceException {

		// Set up disturbanceEntity with 2 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2");
		disturbanceEntity.setPlannedStartDate(null);
		disturbanceEntity.setPlannedStopDate(null);

		// Let all of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(setupDisturbanceFeedbackEntityList("1", "2"));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(2)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(2)).persistWithStatusSent(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(2);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-1")
				.subject("Update subject for reference-1")
				.message("Update message for reference-1. Planned stop date N/A"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-2")
				.subject("Update subject for reference-2")
				.message("Update message for reference-2. Planned stop date N/A"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackEntityCaptor.getAllValues()).hasSize(2);

		final var feedbackHistory1 = disturbanceFeedbackEntityCaptor.getAllValues().get(0);
		assertThat(feedbackHistory1.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory1.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory1.getPartyId()).isEqualTo("partyId-1");

		final var feedbackHistory2 = disturbanceFeedbackEntityCaptor.getAllValues().get(1);
		assertThat(feedbackHistory2.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory2.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory2.getPartyId()).isEqualTo("partyId-2");
	}

	@Test
	void sendUpdateMessageWhenNoAffectedsHasDisturbanceFeedback() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendUpdateMessage(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(messageConfigurationMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
	}

	@Test
	void sendCreateMessage() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList("2", "4", "6"));

		// Setup message properties mock
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(setupCategoryConfig());

		sendMessageLogic.sendCreateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verify(apiMessagingClientMock).sendMessage(messageRequestCaptor.capture());
		verify(disturbanceFeedBackHistoryRepositoryMock, times(3)).persistWithStatusSent(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock, apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);

		/**
		 * Assert sent messages.
		 */
		final var messageRequest = messageRequestCaptor.getValue();
		assertThat(messageRequest).isNotNull();
		assertThat(messageRequest.getMessages()).hasSize(3);
		assertThat(messageRequest.getMessages()).containsExactly(
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-2")
				.subject("New subject for reference-2")
				.message("New message for reference-2"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-4")
				.subject("New subject for reference-4")
				.message("New message for reference-4"),
			new Message()
				.sender(new Sender()
					.smsName("SenderSMSName")
					.emailName("SenderEmailName")
					.emailAddress("noreply@host.se"))
				.partyId("partyId-6")
				.subject("New subject for reference-6")
				.message("New message for reference-6"));

		/**
		 * Assert persisted feedbackHistory.
		 */
		assertThat(disturbanceFeedbackEntityCaptor.getAllValues()).hasSize(3);

		final var feedbackHistory1 = disturbanceFeedbackEntityCaptor.getAllValues().get(0);
		assertThat(feedbackHistory1.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory1.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory1.getPartyId()).isEqualTo("partyId-2");

		final var feedbackHistory2 = disturbanceFeedbackEntityCaptor.getAllValues().get(1);
		assertThat(feedbackHistory2.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory2.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory2.getPartyId()).isEqualTo("partyId-4");

		final var feedbackHistory3 = disturbanceFeedbackEntityCaptor.getAllValues().get(2);
		assertThat(feedbackHistory3.getCategory()).isEqualTo(CATEGORY.toString());
		assertThat(feedbackHistory3.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
		assertThat(feedbackHistory3.getPartyId()).isEqualTo("partyId-6");
	}

	@Test
	void sendCreateMessageWhenNoAffectedsHasDisturbanceFeedback() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let none of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any())).thenReturn(emptyList());

		sendMessageLogic.sendCreateMessage(disturbanceEntity);

		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoMoreInteractions(disturbanceFeedBackRepositoryMock);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock, messageConfigurationMock);
	}

	@Test
	void sendMessageWhenConfigIsNotActive() throws ServiceException {

		// Set up disturbanceEntity with 6 affecteds.
		final var disturbanceEntity = setupDisturbanceEntity("1", "2", "3", "4", "5", "6");

		// Let 3 of these affecteds have an disturbanceEntityFeedback.
		when(disturbanceFeedBackRepositoryMock.findByCategoryAndDisturbanceId(any(), any()))
			.thenReturn(setupDisturbanceFeedbackEntityList("2", "4", "6"));

		// Setup message properties mock
		final var categoryConfigMock = Mockito.mock(CategoryConfig.class);
		when(categoryConfigMock.active()).thenReturn(false);
		when(messageConfigurationMock.getCategoryConfig(CATEGORY)).thenReturn(categoryConfigMock);

		sendMessageLogic.sendCreateMessage(disturbanceEntity);

		verify(messageConfigurationMock, times(3)).getCategoryConfig(CATEGORY);
		verify(disturbanceFeedBackRepositoryMock).findByCategoryAndDisturbanceId(CATEGORY, DISTURBANCE_ID);
		verifyNoInteractions(apiMessagingClientMock, disturbanceFeedBackHistoryRepositoryMock);
		verifyNoMoreInteractions(messageConfigurationMock, disturbanceFeedBackRepositoryMock);
	}

	private DisturbanceEntity setupDisturbanceEntity(String... idNumbersOnAffecteds) {

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setCategory(CATEGORY.toString());
		disturbanceEntity.setDisturbanceId(DISTURBANCE_ID);
		disturbanceEntity.setDescription(DESCRIPTION);
		disturbanceEntity.setPlannedStartDate(PLANNED_START_DATE);
		disturbanceEntity.setPlannedStopDate(PLANNED_STOP_DATE);
		disturbanceEntity.setStatus(STATUS);
		disturbanceEntity.setTitle(TITLE);

		final var affectedEntities = new ArrayList<AffectedEntity>();
		for (var idNumberOnAffected : idNumbersOnAffecteds) {
			final var affectedEntity = new AffectedEntity();
			affectedEntity.setPartyId("partyId-" + idNumberOnAffected);
			affectedEntity.setReference("reference-" + idNumberOnAffected);
			affectedEntities.add(affectedEntity);
		}
		disturbanceEntity.addAffectedEntities(affectedEntities);

		return disturbanceEntity;
	}

	private List<DisturbanceFeedbackEntity> setupDisturbanceFeedbackEntityList(String... idNumbersOnAffecteds) {

		final var disturbanceFeedbackEntityList = new ArrayList<DisturbanceFeedbackEntity>();
		for (var idNumberOnAffected : idNumbersOnAffecteds) {
			final var disturbanceFeedbackEntity = new DisturbanceFeedbackEntity();
			disturbanceFeedbackEntity.setCategory(CATEGORY.toString());
			disturbanceFeedbackEntity.setDisturbanceId(DISTURBANCE_ID);
			disturbanceFeedbackEntity.setPartyId("partyId-" + idNumberOnAffected);

			disturbanceFeedbackEntityList.add(disturbanceFeedbackEntity);
		}

		return disturbanceFeedbackEntityList;
	}

	private CategoryConfig setupCategoryConfig() {
		return new CategoryConfig() {

			@Override
			public boolean active() {
				return true;
			}

			@Override
			public String subjectClose() {
				return "Close subject for ${affected.reference}";
			}

			@Override
			public String subjectNew() {
				return "New subject for ${affected.reference}";
			}

			@Override
			public String subjectUpdate() {
				return "Update subject for ${affected.reference}";
			}

			@Override
			public String messageClose() {
				return "Close message for ${affected.reference}";
			}

			@Override
			public String messageNew() {
				return "New message for ${affected.reference}";
			}

			@Override
			public String messageUpdate() {
				return "Update message for ${affected.reference}. Planned stop date ${plannedStopDate}";
			}

			@Override
			public String senderEmailName() {
				return "SenderEmailName";
			}

			@Override
			public String senderEmailAddress() {
				return "noreply@host.se";
			}

			@Override
			public String senderSmsName() {
				return "SenderSMSName";
			}
		};
	}
}
