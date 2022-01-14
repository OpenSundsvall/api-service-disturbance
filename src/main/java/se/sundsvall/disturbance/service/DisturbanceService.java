package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbance;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbanceEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbances;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toMergedDisturbanceEntity;
import static se.sundsvall.disturbance.service.util.DisturbanceUtils.getRemovedAffectedEntities;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@ApplicationScoped
public class DisturbanceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceService.class);

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Inject
	FeedbackRepository feedbackRepository;

	@Inject
	DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Inject
	SendMessageLogic sendMessageLogic;

	public Disturbance findByCategoryAndDisturbanceId(Category category, String disturbanceId) throws ServiceException {

		LOGGER.debug("Executing findByCategoryAndDisturbanceId() with parameters: category:'{}', disturbanceId:'{}'", category, disturbanceId);

		return toDisturbance(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)
			.orElseThrow(() -> ServiceException.create(format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId), NOT_FOUND)));
	}

	public List<Disturbance> findByPartyIdAndCategoryAndStatus(String partyId, List<Category> categoryFilter, List<Status> statusFilter) {

		LOGGER.debug("Executing findByPartyIdAndCategoryAndStatus() with parameters: partyId:'{}', categoryFilter:'{}', statusFilter:'{}'",
			partyId, categoryFilter, statusFilter);

		return toDisturbances(disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(partyId, categoryFilter, statusFilter));
	}

	@Transactional
	public Disturbance createDisturbance(DisturbanceCreateRequest disturbanceCreateRequest) throws ServiceException {

		LOGGER.debug("Executing createDisturbance() with parameters: request:'{}'", disturbanceCreateRequest);

		// Check if disturbance already exists.
		if (disturbanceRepository.findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId()).isPresent()) {
			throw ServiceException.create(format(ERROR_DISTURBANCE_ALREADY_EXISTS, disturbanceCreateRequest.getCategory(),
				disturbanceCreateRequest.getId()), CONFLICT);
		}

		// Persist disturbance entity.
		final var persistedDisturbanceEntity = disturbanceRepository.persistAndFetch(toDisturbanceEntity(disturbanceCreateRequest));

		if (isNotEmpty(persistedDisturbanceEntity.getAffectedEntities()) && !hasStatusClosed(persistedDisturbanceEntity)) {
			persistedDisturbanceEntity.getAffectedEntities().stream().forEach(affected -> {

				// Create disturbance-feedback entity, if the affected has an existing feedback-entry in DB.
				if (feedbackRepository.findByPartyIdOptional(affected.getPartyId()).isPresent()) {
					disturbanceFeedbackRepository.persist(toDisturbanceFeedbackEntity(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId(),
						DisturbanceFeedbackCreateRequest.create().withPartyId(affected.getPartyId())));
				}
			});

			// Send message to the created disturbance feedback recipients.
			if (hasStatusOpen(persistedDisturbanceEntity)) {
				sendMessageLogic.sendCreateMessage(persistedDisturbanceEntity);
			}
		}

		return toDisturbance(persistedDisturbanceEntity);
	}

	@Transactional
	public Disturbance updateDisturbance(Category category, String disturbanceId, DisturbanceUpdateRequest disturbanceUpdateRequest) throws ServiceException {

		LOGGER.debug("Executing updateDisturbance() with parameters: category:'{}', disturbanceId:'{}', request:'{}'", category, disturbanceId, disturbanceUpdateRequest);

		// Get existing disturbance entity.
		final var existingDisturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)
			.orElseThrow(() -> ServiceException.create(format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId), NOT_FOUND));

		// Get new (incoming) disturbance entity.
		final var incomingDisturbanceEntity = toDisturbanceEntity(category, disturbanceId, disturbanceUpdateRequest);

		// No updates allowed on closed disturbance.
		if (hasStatusClosed(existingDisturbanceEntity)) {
			throw ServiceException.create(format(ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED, category, disturbanceId), CONFLICT);
		}

		// Diff list of affecteds in existing and new (updated) disturbance.
		final var removedAffecteds = getRemovedAffectedEntities(existingDisturbanceEntity, incomingDisturbanceEntity);

		// Send "close" message if status is changed to CLOSED.
		if (isChangedToStatusClosed(existingDisturbanceEntity, incomingDisturbanceEntity)) {
			LOGGER.info("Disturbance status was changed to CLOSED: '{}'", incomingDisturbanceEntity);
			sendMessageLogic.sendCloseMessageToAllApplicableAffecteds(existingDisturbanceEntity);

			// Return since there is no need to continue after this.
			return toDisturbance(disturbanceRepository.persistAndFetch(toMergedDisturbanceEntity(existingDisturbanceEntity, incomingDisturbanceEntity)));
		}
		// Send "close" message to affecteds that was removed from the disturbance (but not if status is PLANNED).
		if (isNotEmpty(removedAffecteds) && !hasStatusPlanned(existingDisturbanceEntity)) {
			LOGGER.info("Removed affecteds was discovered in updateDisturbance: '{}'", removedAffecteds);
			sendMessageLogic.sendCloseMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, removedAffecteds);
		}

		/**
		 * Perform attribute value checks. These checks must be performed before the toMergedDisturbanceEntity-call, since the
		 * old disturbance entity will be modified with the new values.
		 */
		final var disturbanceContentIsChanged = contentIsChanged(existingDisturbanceEntity, incomingDisturbanceEntity);
		final var disturbanceStatusIsChangedFromPlannedToOpen = hasStatusPlanned(existingDisturbanceEntity) && hasStatusOpen(incomingDisturbanceEntity);

		// Merge new and old entities.
		final var mergedDisturbanceEntity = toMergedDisturbanceEntity(existingDisturbanceEntity, incomingDisturbanceEntity);

		// Send "create" message to all affecteds, if the disturbance status is changed from PLANNED TO OPEN.
		if (disturbanceStatusIsChangedFromPlannedToOpen) {
			sendMessageLogic.sendCreateMessage(mergedDisturbanceEntity);
		}
		// Send "update" message to all affecteds, if the disturbance content is updated (but not for status PLANNED).
		else if (disturbanceContentIsChanged && !hasStatusPlanned(mergedDisturbanceEntity)) {
			sendMessageLogic.sendUpdateMessage(mergedDisturbanceEntity);
		}

		return toDisturbance(disturbanceRepository.persistAndFetch(mergedDisturbanceEntity));
	}

	@Transactional
	public void deleteDisturbance(Category category, String disturbanceId) throws ServiceException {

		LOGGER.debug("Executing deleteDisturbance() with parameters: category:'{}', disturbanceId:'{}'", category, disturbanceId);

		final var disturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)
			.orElseThrow(() -> ServiceException.create(format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId), NOT_FOUND));

		// Delete all related disturbanceFeedback-entities.
		disturbanceFeedbackRepository.deleteByCategoryAndDisturbanceId(category, disturbanceId);

		// "Soft delete" disturbance entity.
		disturbanceEntity.setDeleted(true);
		disturbanceRepository.persist(disturbanceEntity);
	}

	private boolean isChangedToStatusClosed(DisturbanceEntity oldDisturbanceEntity, DisturbanceEntity newDisturbanceEntity) {
		return !hasStatusClosed(oldDisturbanceEntity) && hasStatusClosed(newDisturbanceEntity);
	}

	protected static boolean hasStatusClosed(DisturbanceEntity disturbanceEntity) {
		return Status.CLOSED.toString().equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusOpen(DisturbanceEntity disturbanceEntity) {
		return Status.OPEN.toString().equals(disturbanceEntity.getStatus());
	}

	protected static boolean hasStatusPlanned(DisturbanceEntity disturbanceEntity) {
		return Status.PLANNED.toString().equals(disturbanceEntity.getStatus());
	}

	/**
	 * Check if parameters in the newEntity are not null (i.e. they are set in the PATCH request). If set (i.e. not null):
	 * Check if the values differs from the existing ones that are stored in the oldEntity.
	 * 
	 * The attributes that are checked are: description, title, plannedStartDate and plannedStopDate (and also if status is
	 * changed from PLANNED to OPEN).
	 * 
	 * @param oldEntity
	 * @param newEntity
	 * @return true if the content is changed, false otherwise.
	 */
	private boolean contentIsChanged(DisturbanceEntity oldEntity, DisturbanceEntity newEntity) {
		final var contentIsChanged = (nonNull(newEntity.getDescription()) && !equalsIgnoreCase(oldEntity.getDescription(), newEntity.getDescription())) ||
			(nonNull(newEntity.getTitle()) && !equalsIgnoreCase(oldEntity.getTitle(), newEntity.getTitle())) ||
			(nonNull(newEntity.getPlannedStartDate()) && !equalsIgnoreCase(valueOf(oldEntity.getPlannedStartDate()), valueOf(newEntity.getPlannedStartDate()))) ||
			(nonNull(newEntity.getPlannedStopDate()) && !equalsIgnoreCase(valueOf(oldEntity.getPlannedStopDate()), valueOf(newEntity.getPlannedStopDate())) ||
				(hasStatusOpen(newEntity) && hasStatusPlanned(oldEntity)));

		if (contentIsChanged) {
			LOGGER.debug("Disturbance content update was discovered. Old:'{}' New:'{}'", oldEntity, newEntity);
		}

		return contentIsChanged;
	}
}
