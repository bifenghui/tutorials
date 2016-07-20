package com.baeldung.restassured;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.get;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.tomakehurst.wiremock.WireMockServer;

public class RestAssuredTest {

	private WireMockServer wireMockServer = new WireMockServer();
	private static final String EVENTS_PATH = "/events?id=390";
	private static final String APPLICATION_JSON = "application/json";
	private static final String GAME_ODDS = getEventJson();

	@Before
	public void before() throws Exception {
		System.out.println("Setting up!");
		wireMockServer.start();
		configureFor("localhost", 8080);
		stubFor(get(urlEqualTo(EVENTS_PATH)).willReturn(
				aResponse().withStatus(200)
						.withHeader("Content-Type", APPLICATION_JSON)
						.withBody(GAME_ODDS)));
	}

	@Test
	public void givenUrl_whenCheckingFloatValuePasses_thenCorrect() {
		get("/events?id=390").then().assertThat()
				.body("odd.ck", equalTo(12.2f));
	}

	@Test
	public void givenUrl_whenSuccessOnGetsResponse_andJsonHasRequiredKV_thenCorrect() {

		get("/events?id=390").then().statusCode(200).assertThat()
				.body("id", equalTo("390"));

	}

	@Test
	public void givenUrl_whenJsonResponseHasArrayWithGivenValuesUnderKey_thenCorrect() {
		get("/events?id=390").then().assertThat()
				.body("odds.price", hasItems("1.30", "5.25", "2.70", "1.20"));
	}

	@Test
	public void givenUrl_whenJsonResponseConformsToSchema_thenCorrect() {

		get("/events?id=390").then().assertThat()
				.body(matchesJsonSchemaInClasspath("event_0.json"));
	}

	@Test
	public void givenUrl_whenValidatesResponseWithInstanceSettings_thenCorrect() {
		JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory
				.newBuilder()
				.setValidationConfiguration(
						ValidationConfiguration.newBuilder()
								.setDefaultVersion(SchemaVersion.DRAFTV4)
								.freeze()).freeze();

		get("/events?id=390")
				.then()
				.assertThat()
				.body(matchesJsonSchemaInClasspath("event_0.json").using(
						jsonSchemaFactory));

	}

	@Test
	public void givenUrl_whenValidatesResponseWithStaticSettings_thenCorrect() {

		get("/events?id=390")
				.then()
				.assertThat()
				.body(matchesJsonSchemaInClasspath("event_0.json").using(
						settings().with().checkedValidation(false)));
	}

	@After
	public void after() throws Exception {
		System.out.println("Running: tearDown");
		wireMockServer.stop();
	}

	private static String getEventJson() {
		return Util.inputStreamToString(new RestAssuredTest().getClass()
				.getResourceAsStream("/event_0.json"));
	}

}