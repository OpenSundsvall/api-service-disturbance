package se.sundsvall.disturbance.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.noContent;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.api.validation.ValidUuid;
import se.sundsvall.disturbance.service.FeedbackService;

@Path("/feedback")
@Tag(name = "Feedback", description = "Feedback operations")
public class FeedbackResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackResource.class);

	@Inject
	FeedbackService feedbackService;

	@POST
	@Path("/")
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Create continuous feedback for a person or an organization. I.e. subscribe on notifications for all new future disturbances.")
	@APIResponse(responseCode = "204", description = "Successful operation")
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response createFeedback(
		@RequestBody(required = true, content = @Content(schema = @Schema(implementation = FeedbackCreateRequest.class))) @NotNull @Valid FeedbackCreateRequest body)
		throws ServiceException {
		LOGGER.debug("Received createFeedback request: body='{}'", body);

		feedbackService.createFeedback(body);

		return noContent().build();
	}

	@DELETE
	@Path("/{partyId}")
	@Operation(summary = "Delete feedback for a partyId (e.g. a person or an organization). I.e. remove subscription on notifications for any new future disturbances.")
	@APIResponse(responseCode = "204", description = "Successful operation")
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response deleteFeedback(
		@Parameter(name = "partyId", description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathParam("partyId") String partyId)
		throws ServiceException {
		LOGGER.debug("Received deleteFeedback request: partyId='{}'", partyId);

		feedbackService.deleteFeedback(partyId);

		return noContent().build();
	}
}
