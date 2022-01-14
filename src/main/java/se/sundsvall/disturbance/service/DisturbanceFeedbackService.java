package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static se.sundsvall.disturbance.service.DisturbanceService.hasStatusClosed;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_CLOSED;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_FEEDBACK_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_DISTURBANCE_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

@ApplicationScoped
public class DisturbanceFeedbackService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceFeedbackService.class);

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Inject
	DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Transactional
	public void createDisturbanceFeedback(Category category, String disturbanceId, DisturbanceFeedbackCreateRequest request) throws ServiceException {

		LOGGER.debug("Executing createDisturbanceFeedback() with parameters: category:'{}', disturbanceId:'{}', request:'{}'", category, disturbanceId, request);

		// Check that disturbance exists.
		final var disturbanceEntity = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)
			.orElseThrow(() -> ServiceException.create(format(ERROR_DISTURBANCE_NOT_FOUND, category, disturbanceId), NOT_FOUND));

		// Check that disturbance is not CLOSED.
		if (hasStatusClosed(disturbanceEntity)) {
			throw ServiceException.create(format(ERROR_DISTURBANCE_CLOSED, category, disturbanceId), CONFLICT);
		}

		// Check that no existing disturbance feedback already exists for provided parameters.
		if (disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyIdOptional(category, disturbanceId, request.getPartyId()).isPresent()) {
			throw ServiceException.create(format(ERROR_DISTURBANCE_FEEDBACK_ALREADY_EXISTS, category, disturbanceId, request.getPartyId()), CONFLICT);
		}

		disturbanceFeedbackRepository.persist(toDisturbanceFeedbackEntity(category, disturbanceId, request));
	}
}
