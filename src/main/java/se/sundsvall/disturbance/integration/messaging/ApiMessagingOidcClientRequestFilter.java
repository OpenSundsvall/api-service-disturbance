package se.sundsvall.disturbance.integration.messaging;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.Tokens;

@Priority(Priorities.AUTHENTICATION)
@RequestScoped
public class ApiMessagingOidcClientRequestFilter implements ClientRequestFilter {

	@Inject
	@NamedOidcClient("api-messaging")
	Tokens tokens;

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add(AUTHORIZATION, "Bearer " + tokens.getAccessToken());
	}
}
