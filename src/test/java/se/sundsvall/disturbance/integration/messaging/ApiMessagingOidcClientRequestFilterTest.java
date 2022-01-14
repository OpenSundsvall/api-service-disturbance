package se.sundsvall.disturbance.integration.messaging;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.oidc.client.Tokens;

@ExtendWith(MockitoExtension.class)
class ApiMessagingOidcClientRequestFilterTest {

	private static final String ACCESS_TOKEN = "access-token";

	@Mock
	private Tokens tokensMock;

	@Mock
	private ClientRequestContext requestContextMock;

	@InjectMocks
	private ApiMessagingOidcClientRequestFilter filter;

	@Test
	void testFilter() throws IOException {

		when(tokensMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
		when(requestContextMock.getHeaders()).thenReturn(new MultivaluedHashMap<>());

		assertThat(requestContextMock.getHeaders()).doesNotContainKey(AUTHORIZATION);

		filter.filter(requestContextMock);

		assertThat(requestContextMock.getHeaders())
			.isNotNull()
			.containsEntry(AUTHORIZATION, List.of("Bearer " + ACCESS_TOKEN));
	}
}
