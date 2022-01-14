package se.sundsvall.disturbance.service;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_FEEDBACK_ALREADY_EXISTS;
import static se.sundsvall.disturbance.service.ServiceConstants.ERROR_FEEDBACK_NOT_FOUND;
import static se.sundsvall.disturbance.service.mapper.FeedbackMapper.toFeedbackEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;

@ApplicationScoped
public class FeedbackService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);

	@Inject
	FeedbackRepository feedbackRepository;

	@Transactional
	public void createFeedback(FeedbackCreateRequest request) throws ServiceException {

		LOGGER.debug("Executing createFeedback() with parameters: request:'{}'", request);

		// Check that no existing feedback already exists for provided parameters.
		if (feedbackRepository.findByPartyIdOptional(request.getPartyId()).isPresent()) {
			throw ServiceException.create(format(ERROR_FEEDBACK_ALREADY_EXISTS, request.getPartyId()), CONFLICT);
		}

		feedbackRepository.persist(toFeedbackEntity(request));
	}

	@Transactional
	public void deleteFeedback(String partyId) throws ServiceException {

		LOGGER.debug("Executing deleteFeedback() with parameters: partyId:'{}'", partyId);

		final var feedbackEntity = feedbackRepository.findByPartyIdOptional(partyId)
			.orElseThrow(() -> ServiceException.create(format(ERROR_FEEDBACK_NOT_FOUND, partyId), NOT_FOUND));

		feedbackRepository.delete(feedbackEntity);
	}
}
