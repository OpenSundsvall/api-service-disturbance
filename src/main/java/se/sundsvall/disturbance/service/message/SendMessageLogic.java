package se.sundsvall.disturbance.service.message;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static se.sundsvall.disturbance.service.message.util.SendMessageUtils.createMessage;
import static se.sundsvall.disturbance.service.message.util.SendMessageUtils.getReferenceByPartyId;
import static se.sundsvall.disturbance.service.util.DateUtils.toMessageDateFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.Sender;
import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackHistoryRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.messaging.ApiMessagingClient;
import se.sundsvall.disturbance.service.message.configuration.MessageConfiguration;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@ApplicationScoped
public class SendMessageLogic {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageLogic.class);

	// Message template variable name definitions.
	private static final String MSG_TITLE = "title";
	private static final String MSG_NEWLINE = "newline";
	private static final String MSG_DESCRIPTION = "description";
	private static final String MSG_PLANNED_START_DATE = "plannedStartDate";
	private static final String MSG_PLANNED_STOP_DATE = "plannedStopDate";
	private static final String MSG_AFFECTED_REFERENCE = "affected.reference";

	@Inject
	DisturbanceFeedbackRepository disturbanceFeedBackRepository;

	@Inject
	DisturbanceFeedbackHistoryRepository disturbanceFeedBackHistoryRepository;

	@Inject
	MessageConfiguration messageConfiguration;

	@Inject
	@RestClient
	ApiMessagingClient apiMessagingClient;

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations in a disturbance with an existing
	 * disturbanceFeedback. The affectedEntities will get a message if a disturbanceFeedback exists for this disturbance.
	 * 
	 * @param disturbanceEntity
	 */
	@Transactional
	public void sendCloseMessageToAllApplicableAffecteds(DisturbanceEntity disturbanceEntity) {
		sendCloseMessage(disturbanceEntity, disturbanceEntity.getAffectedEntities());
	}

	/**
	 * Send a "closed disturbance" message to all affected persons/organizations with an existing disturbanceFeedback, in
	 * the provided affectedEntities list. This will override the existing affectedEntities in the DisturbanceEntity.
	 * 
	 * The affectedEntities will get a message if a disturbanceFeedback exists for this disturbance.
	 * 
	 * @param disturbanceEntity The entity that is closed.
	 * @param affectedEntities  The affectedEntities that will get a message (if a disturbanceFeedback exists)
	 */
	@Transactional
	public void sendCloseMessageToProvidedApplicableAffecteds(DisturbanceEntity disturbanceEntity, List<AffectedEntity> affectedEntities) {
		sendCloseMessage(disturbanceEntity, affectedEntities);
	}

	/**
	 * Send a "new disturbance" message to all affected persons/organizations with an existing disturbanceFeedback in a
	 * disturbance.
	 * 
	 * @param createdDisturbanceEntity
	 */
	@Transactional
	public void sendCreateMessage(DisturbanceEntity createdDisturbanceEntity) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository
			.findByCategoryAndDisturbanceId(Category.valueOf(createdDisturbanceEntity.getCategory()), createdDisturbanceEntity.getDisturbanceId());

		final var messageRequest = new MessageRequest()
			.messages(disturbanceFeedbackEntities.stream()
				.map(feedbackEntity -> mapToNewMessage(feedbackEntity, createdDisturbanceEntity))
				.filter(Objects::nonNull)
				.collect(toList()));

		// Send messages.
		sendMessages(messageRequest);
	}

	/**
	 * Send a "updated disturbance" message to all affected persons/organizations with an existing disturbanceFeedback in a
	 * disturbance.
	 * 
	 * @param updatedDisturbanceEntity
	 */
	@Transactional
	public void sendUpdateMessage(DisturbanceEntity updatedDisturbanceEntity) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository
			.findByCategoryAndDisturbanceId(Category.valueOf(updatedDisturbanceEntity.getCategory()), updatedDisturbanceEntity.getDisturbanceId());

		final var messageRequest = new MessageRequest()
			.messages(disturbanceFeedbackEntities.stream()
				.map(feedbackEntity -> mapToUpdateMessage(feedbackEntity, updatedDisturbanceEntity))
				.filter(Objects::nonNull)
				.collect(toList()));

		// Send messages.
		sendMessages(messageRequest);
	}

	private void sendCloseMessage(DisturbanceEntity disturbanceEntity, List<AffectedEntity> affectedEntities) {

		// Fetch all feedbackEntities for this disturbance.
		final var disturbanceFeedbackEntities = disturbanceFeedBackRepository.findByCategoryAndDisturbanceId(Category.valueOf(disturbanceEntity.getCategory()),
			disturbanceEntity.getDisturbanceId());

		final var messageRequest = new MessageRequest()
			.messages(disturbanceFeedbackEntities.stream()
				/**
				 * The filter below is necessary in order to handle scenario when call is made from
				 * sendCloseMessageToProvidedApplicableAffecteds(). I.e. when some persons/organizations are removed. We don't want to
				 * match all disturbanceFeedbackEntities, since this will send mail to all persons/organizations in the disturbance.
				 */
				.filter(feedbackEntity -> affectedEntities.stream().anyMatch(affectedEntity -> affectedEntity.getPartyId().equalsIgnoreCase(feedbackEntity.getPartyId())))
				.map(feedbackEntity -> mapToCloseMessage(feedbackEntity, disturbanceEntity, affectedEntities))
				.filter(Objects::nonNull)
				.collect(toList()));

		// Send messages.
		sendMessages(messageRequest);
	}

	private void persistFeedbackHistory(DisturbanceFeedbackEntity disturbanceFeedbackEntity) {
		disturbanceFeedBackHistoryRepository.persistWithStatusSent(disturbanceFeedbackEntity);
	}

	private Message mapToUpdateMessage(DisturbanceFeedbackEntity disturbanceFeedbackEntity, DisturbanceEntity disturbanceEntity) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory());
		if (!messageConfig.active()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, getReferenceByPartyId(disturbanceEntity.getAffectedEntities(), disturbanceFeedbackEntity.getPartyId())));

		// Assemble message and subject based on the properties.
		final var sender = new Sender()
			.emailName(messageConfig.senderEmailName())
			.emailAddress(messageConfig.senderEmailAddress())
			.smsName(messageConfig.senderSmsName());
		final var subject = propertyResolver.replace(messageConfig.subjectUpdate());
		final var message = propertyResolver.replace(messageConfig.messageUpdate());

		// Store feedback history.
		persistFeedbackHistory(disturbanceFeedbackEntity);

		return createMessage(sender, disturbanceFeedbackEntity.getPartyId(), subject, message);
	}

	private Message mapToNewMessage(DisturbanceFeedbackEntity disturbanceFeedbackEntity, DisturbanceEntity disturbanceEntity) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory());
		if (!messageConfig.active()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, getReferenceByPartyId(disturbanceEntity.getAffectedEntities(), disturbanceFeedbackEntity.getPartyId())));

		// Assemble message and subject based on the properties.
		final var sender = new Sender()
			.emailName(messageConfig.senderEmailName())
			.emailAddress(messageConfig.senderEmailAddress())
			.smsName(messageConfig.senderSmsName());
		final var subject = propertyResolver.replace(messageConfig.subjectNew());
		final var message = propertyResolver.replace(messageConfig.messageNew());

		// Store feedback history.
		persistFeedbackHistory(disturbanceFeedbackEntity);

		return createMessage(sender, disturbanceFeedbackEntity.getPartyId(), subject, message);
	}

	private Message mapToCloseMessage(DisturbanceFeedbackEntity disturbanceFeedbackEntity, DisturbanceEntity disturbanceEntity, List<AffectedEntity> affectedEntities) {

		// Fetch message properties by category.
		final var messageConfig = getMessageConfigByCategory(disturbanceEntity.getCategory());
		if (!messageConfig.active()) {
			return null;
		}

		final var propertyResolver = new StringSubstitutor(Map.of(
			MSG_NEWLINE, lineSeparator(),
			MSG_TITLE, disturbanceEntity.getTitle(),
			MSG_DESCRIPTION, disturbanceEntity.getDescription(),
			MSG_PLANNED_START_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStartDate()),
			MSG_PLANNED_STOP_DATE, toMessageDateFormat(disturbanceEntity.getPlannedStopDate()),
			MSG_AFFECTED_REFERENCE, getReferenceByPartyId(affectedEntities, disturbanceFeedbackEntity.getPartyId())));

		// Assemble message and subject based on the properties.
		final var sender = new Sender()
			.emailName(messageConfig.senderEmailName())
			.emailAddress(messageConfig.senderEmailAddress())
			.smsName(messageConfig.senderSmsName());
		final var subject = propertyResolver.replace(messageConfig.subjectClose());
		final var message = propertyResolver.replace(messageConfig.messageClose());

		// Store feedback history.
		persistFeedbackHistory(disturbanceFeedbackEntity);

		return createMessage(sender, disturbanceFeedbackEntity.getPartyId(), subject, message);
	}

	private void sendMessages(MessageRequest messageRequest) {
		try {

			LOGGER.debug("Messages to send to api-messaging-service: '{}'", messageRequest);

			// Send messageRequest to api-messaging-service service (if it contains messages).
			if (isNotEmpty(messageRequest.getMessages())) {
				LOGGER.info("apiMessagingClient: Sending '{}' messages to api-messaging-service...", messageRequest.getMessages().size());
				apiMessagingClient.sendMessage(messageRequest);
				LOGGER.info("apiMessagingClient: Messages sent!");
			}
		} catch (ServiceException e) {
			throw e.asRuntimeException();
		}
	}

	private CategoryConfig getMessageConfigByCategory(String category) {
		return messageConfiguration.getCategoryConfig(Category.valueOf(category));
	}
}
