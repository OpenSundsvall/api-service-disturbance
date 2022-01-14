package se.sundsvall.disturbance.integration.messaging;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageStatusResponse;
import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.integration.messaging.mappers.MessagingExceptionMapper;

@Path("/messages")
@RegisterRestClient(configKey = "api-messaging")
@RegisterProvider(MessagingExceptionMapper.class)
@RegisterProvider(ApiMessagingOidcClientRequestFilter.class)
@ApplicationScoped
public interface ApiMessagingClient {

	/**
	 * Send messages as email or SMS to a list of recipients, denoted by the partyId.
	 * 
	 * @param messageRequest with a list of messages.
	 * @return a MessageStatusResponse
	 * @throws ServiceException
	 */
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	MessageStatusResponse sendMessage(MessageRequest messageRequest) throws ServiceException;
}
