package se.sundsvall.disturbance.apptest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.matching.UrlPattern.fromOneOf;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.javacrumbs.jsonunit.JsonAssert;
import se.sundsvall.disturbance.apptest.support.annotation.InjectWireMock;

abstract class AbstractAppTest {

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Executors.newFixedThreadPool(3))
		.version(HttpClient.Version.HTTP_2)
		.build();

	private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
	private static final String X_TESTCASE_HEADER_NAME = "x-testCase";

	private static final String FILES_DIRECTORY = "__files/";
	private static final String COMMON_MAPPING_DIRECTORY = "common";
	private static final String MAPPING_DIRECTORY = "/mappings";
	private static final String MOCKING_DIRECTORY = "/mocking";

	private String servicePath;
	private String method;
	private String requestBody;
	private String mappingPath;
	private Map<String, Object> attributeValues;
	private Map<String, String> headerValues;
	private Response.Status expectedResponseStatus;
	private Map<String, List<String>> expectedResponseHeaders;
	private String expectedResponseBody;
	private boolean expectedResponseBodyIsNull;

	@InjectWireMock
	private WireMockServer wireMock;

	protected AbstractAppTest setupCall() throws Exception {
		final var testCaseName = getTestMethodName();
		final var testClassName = getTestClassName();
		mappingPath = testClassName;
		if (!mappingPath.endsWith("/")) {
			mappingPath += "/";
		}

		wireMock.loadMappingsUsing(new JsonFileMappingsSource(
			new ClasspathFileSource(FILES_DIRECTORY + mappingPath + COMMON_MAPPING_DIRECTORY + MAPPING_DIRECTORY)));
		if (nonNull(testCaseName)) {
			wireMock.loadMappingsUsing(new JsonFileMappingsSource(
				new ClasspathFileSource(FILES_DIRECTORY + mappingPath + testCaseName + MAPPING_DIRECTORY)));
		}

		return this;
	}

	protected AbstractAppTest withHttpMethod(final String method) {
		this.method = method;
		return this;
	}

	protected AbstractAppTest withAttributeReplacement(final String jsonPattern, final Object replacement) {
		if (attributeValues == null) {
			attributeValues = new HashMap<>();
		}
		attributeValues.put(jsonPattern, replacement);
		return this;
	}

	protected AbstractAppTest withExpectedResponseStatus(final Response.Status expectedResponseStatus) {
		this.expectedResponseStatus = expectedResponseStatus;
		return this;
	}

	protected AbstractAppTest withHeader(final String key, final String value) {
		if (headerValues == null) {
			headerValues = new HashMap<>();
		}
		headerValues.put(key, value);
		return this;
	}

	protected AbstractAppTest withExpectedResponseHeader(final String expectedHeaderKey, final List<String> expectedHeaderValues) {
		if (expectedResponseHeaders == null) {
			expectedResponseHeaders = new HashMap<>();
		}
		expectedResponseHeaders.put(expectedHeaderKey, expectedHeaderValues);
		return this;
	}

	protected AbstractAppTest withExpectedResponse(final String expectedResponse) throws Exception {
		return withExpectedResponse(expectedResponse, null);
	}

	protected AbstractAppTest withExpectedResponse(final String expectedResponse, final Map<String, Object> replacements) throws Exception {
		final var contentFromFile = fromTestFile(expectedResponse);
		if (nonNull(contentFromFile)) {
			expectedResponseBody = contentFromFile;
		} else {
			expectedResponseBody = expectedResponse;
		}

		if (nonNull(replacements)) {
			DocumentContext doc = JsonPath.parse(expectedResponseBody);
			replacements.forEach(doc::set);
			expectedResponseBody = doc.jsonString();
		}

		return this;
	}

	protected AbstractAppTest withExpectedResponseBodyIsNull() {
		this.expectedResponseBodyIsNull = true;
		return this;
	}

	protected AbstractAppTest withServicePath(final String servicePath) {
		this.servicePath = servicePath;
		return this;
	}

	protected AbstractAppTest withRequest(final String request) throws Exception {
		final var contentFromFile = fromTestFile(request);
		if (nonNull(contentFromFile)) {
			requestBody = contentFromFile;
		} else {
			requestBody = request;
		}
		return this;
	}

	protected void sendRequestAndVerifyResponse(final MediaType mediaType) throws Exception {
		final var request = httpClientRequest(method, servicePath, mediaType, modifyRequestAttributes(requestBody));
		final var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

		verifyAllStubs();

		final var responseBody = response.body();

		if (nonNull(expectedResponseHeaders)) {
			for (Map.Entry<String, List<String>> header : expectedResponseHeaders.entrySet()) {
				final var headerMap = response.headers().map();
				assertThat(headerMap).containsKey(header.getKey());
				assertThat(headerMap.get(header.getKey())).containsAll(header.getValue());
			}
		}
		assertThat(response.statusCode()).isEqualTo(expectedResponseStatus.getStatusCode());
		if (nonNull(expectedResponseBody) && nonNull(responseBody)) {
			JsonAssert.assertJsonEquals(expectedResponseBody, responseBody);
		}
		if (expectedResponseBodyIsNull) {
			assertThat(responseBody).isNull();
		}
	}

	protected AbstractAppTest sendRequestAndVerifyResponse() throws Exception {
		sendRequestAndVerifyResponse(MediaType.APPLICATION_JSON_TYPE);
		return this;
	}

	protected String deleteJsonAttributes(final String originalJson, final String... pathToExclude) {
		final var document = JsonPath.parse(originalJson);
		for (String path : pathToExclude) {
			document.delete(path);
		}
		return document.jsonString();
	}

	private HttpRequest httpClientRequest(final String method, final String servicePath, final MediaType mediaType, final String body) throws Exception {
		final var builder = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:8081" + servicePath))
			.header(CONTENT_TYPE_HEADER_NAME, mediaType.toString())
			.header(X_TESTCASE_HEADER_NAME, getClass().getSimpleName() + "." + getTestMethodName());

		if (headerValues != null && !headerValues.isEmpty()) {
			headerValues.forEach(builder::header);
		}

		switch (method) {
		case HttpMethod.GET:
			return builder.GET().build();
		case HttpMethod.POST:
			return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
		case HttpMethod.PUT:
			return builder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
		case HttpMethod.PATCH:
			return builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body)).build();
		case HttpMethod.DELETE:
			return builder.DELETE().build();
		default:
			throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		}
	}

	private String modifyRequestAttributes(final String originalRequest) {
		var request = originalRequest;

		final var optionalAttributes = ofNullable(attributeValues);
		if (optionalAttributes.isPresent()) {
			DocumentContext doc = JsonPath.parse(requestBody);
			optionalAttributes.get().forEach(doc::set);
			request = doc.jsonString();
		}

		return request;
	}

	/*
	 * Verifies that all stubs setup has been called. Will throw {@link VerificationException} if verification fails.
	 */
	protected void verifyAllStubs() {
		try {
			// Verify all stubs by url.
			wireMock.listAllStubMappings().getMappings().forEach(stub -> {
				RequestPattern requestPattern = stub.getRequest();
				wireMock
					.verify(anyRequestedFor(fromOneOf(requestPattern.getUrl(), requestPattern.getUrlPattern(), requestPattern.getUrlPath(), requestPattern.getUrlPathPattern())));
			});

			if (!wireMock.findAllUnmatchedRequests().isEmpty()) {
				List<String> unmatchedUrls = wireMock.findAllUnmatchedRequests()
					.stream()
					.map(LoggedRequest::getUrl)
					.collect(toList());
				throw new AssertionError(String.format("The following requests was not matched: %s", unmatchedUrls));
			}
		} finally {
			wireMock.resetAll();
		}
	}

	protected AbstractAppTest withSoapStub(final String path, final String expectedRequestBodyFileName,
		final String responseFileName) throws Exception {
		return withSoapStub(path, expectedRequestBodyFileName, responseFileName, Status.OK, 0);
	}

	protected AbstractAppTest withSoapStub(final String path, final String expectedRequestBodyFileName,
		final String expectedResponseBodyFileName, final Status responseStatus, final int delayInSeconds) throws Exception {
		wireMock.stubFor(post(path)
			.withRequestBody(equalToXml(fromTestMockFile(expectedRequestBodyFileName), true))
			.willReturn(aResponse()
				.withHeader(CONTENT_TYPE_HEADER_NAME, "text/xml;charset=utf-8")
				.withStatus(responseStatus.getStatusCode())
				.withFixedDelay(delayInSeconds * 1000)
				.withBody(fromTestMockFile(expectedResponseBodyFileName))
				.withTransformers("response-template")));
		return this;
	}

	protected AbstractAppTest withRestStub(final String path, final String expectedRequestBodyFileName,
		final String responseFileName) throws Exception {
		return withRestStub(path, expectedRequestBodyFileName, responseFileName, Status.OK, HttpMethod.POST);
	}

	public AbstractAppTest withRestStub(final String path, final String expectedRequestBodyFileName,
		final String responseFileName, final String httpMethod) throws Exception {
		return withRestStub(path, expectedRequestBodyFileName, responseFileName, Status.OK, httpMethod);
	}

	public AbstractAppTest withRestStub(final String path, final String expectedFileName, final String responseFileName,
		final Status responseStatus, final String httpMethod) throws Exception {
		switch (httpMethod) {
		case HttpMethod.GET:
			wireMock.stubFor(get(urlMatching(path + "?" + fromTestMockFile(expectedFileName)))
				.willReturn(aResponse()
					.withHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON)
					.withStatus(responseStatus.getStatusCode())
					.withBody(fromTestMockFile(responseFileName))));
			break;
		case HttpMethod.POST:
			wireMock.stubFor(post(urlMatching(path))
				.withRequestBody(toStringValuePattern(expectedFileName))
				.willReturn(aResponse()
					.withHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON)
					.withStatus(responseStatus.getStatusCode())
					.withBody(fromTestMockFile(responseFileName))));
			break;
		case HttpMethod.PUT:
			wireMock.stubFor(put(urlMatching(path))
				.withRequestBody(toStringValuePattern(expectedFileName))
				.willReturn(aResponse()
					.withHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON)
					.withStatus(responseStatus.getStatusCode())
					.withBody(fromTestMockFile(responseFileName))));
			break;
		case HttpMethod.PATCH:
			wireMock.stubFor(patch(urlMatching(path))
				.withRequestBody(toStringValuePattern(expectedFileName))
				.willReturn(aResponse()
					.withHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON)
					.withStatus(responseStatus.getStatusCode())
					.withBody(fromTestMockFile(responseFileName))));
			break;
		case HttpMethod.DELETE:
			wireMock.stubFor(delete(urlMatching(path))
				.withRequestBody(toStringValuePattern(expectedFileName))
				.willReturn(aResponse()
					.withHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON)
					.withStatus(responseStatus.getStatusCode())
					.withBody(fromTestMockFile(responseFileName))));
			break;
		default:
			throw new IllegalArgumentException("HTTP method not allowed: " + httpMethod);
		}

		return this;
	}

	private String fromTestMockFile(final String fileName) throws Exception {
		return fromClasspath(FILES_DIRECTORY + mappingPath + getTestMethodName() + MOCKING_DIRECTORY + "/" + fileName);
	}

	protected String fromTestFile(final String fileName) throws Exception {
		return fromClasspath(FILES_DIRECTORY + mappingPath + getTestMethodName() + "/" + fileName);
	}

	private String fromClasspath(final String path) {
		try (InputStream is = getClasspathResourceAsStream(path.startsWith("/") ? path.substring(1) : path)) {
			return convertStreamToString(is);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot load classpath resource: '" + path + "'", e);
		}
	}

	private InputStream getClasspathResourceAsStream(final String resourceName) {
		final var classLoader = Thread.currentThread().getContextClassLoader();

		return ofNullable(classLoader.getResourceAsStream(resourceName))
			.orElseThrow(() -> new IllegalArgumentException("Resource not found with name: " + resourceName));
	}

	private String convertStreamToString(final InputStream is) {
		return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
			.lines()
			.collect(joining(lineSeparator()));
	}

	private String getTestMethodName() throws Exception {
		return Arrays.stream(Thread.currentThread().getStackTrace())
			.map(StackTraceElement::getMethodName)
			.filter(methodName -> methodName.startsWith("test"))
			.findFirst()
			.orElseThrow(() -> new Exception("Could not find method name! Test method must start with 'test'"));
	}

	private String getTestClassName() {
		final var className = getClass().getSimpleName();
		final var indexOfClassNameSuffixStart = className.indexOf("_");

		// Remove Quarkus generated sub class name suffixes (e.g. TestClass_SubClass -> TestClass)
		if (indexOfClassNameSuffixStart > 0) {
			return className.substring(0, indexOfClassNameSuffixStart);
		}

		return className;
	}

	private StringValuePattern toStringValuePattern(String jsonContentFileName) throws Exception {
		return isNull(jsonContentFileName) ? absent() : equalToJson(fromTestMockFile(jsonContentFileName));
	}
}
