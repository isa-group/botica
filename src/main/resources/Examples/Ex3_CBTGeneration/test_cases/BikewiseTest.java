package restest;

import org.junit.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.FixMethodOrder;
import static org.junit.Assert.fail;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.assertTrue;
import org.junit.runners.MethodSorters;
import io.qameta.allure.restassured.AllureRestAssured;
import es.us.isa.restest.writers.restassured.filters.StatusCode5XXFilter;
import es.us.isa.restest.writers.restassured.filters.NominalOrFaultyTestCaseFilter;
import es.us.isa.restest.writers.restassured.filters.StatefulFilter;
import java.io.File;
import es.us.isa.restest.writers.restassured.filters.ResponseValidationFilter;
import es.us.isa.restest.writers.restassured.filters.CSVFilter;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BikewiseTest {

	private static final String OAI_JSON_URL = "src/main/resources/Examples/specs/Bikewise/spec_bikewise.yaml";
	private static final StatusCode5XXFilter statusCode5XXFilter = new StatusCode5XXFilter();
	private static final NominalOrFaultyTestCaseFilter nominalOrFaultyTestCaseFilter = new NominalOrFaultyTestCaseFilter();
	private static final ResponseValidationFilter validationFilter = new ResponseValidationFilter(OAI_JSON_URL);
	private static final AllureRestAssured allureFilter = new AllureRestAssured();
	private static final String APIName = "Example3";
	private static final String testId = "Example3";
	private static final CSVFilter csvFilter = new CSVFilter(APIName, testId);

	@BeforeClass
	public static void setUp() {
		RestAssured.baseURI = "https://bikewise.org/api";

		statusCode5XXFilter.setAPIName(APIName);
		statusCode5XXFilter.setTestId(testId);
		nominalOrFaultyTestCaseFilter.setAPIName(APIName);
		nominalOrFaultyTestCaseFilter.setTestId(testId);
		validationFilter.setAPIName(APIName);
		validationFilter.setTestId(testId);
	}

	@Test
	public void test_to2amvjj3qeb_GETversionincidentsformat() {
		String testResultId = "test_to2amvjj3qeb_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter page from '54' to string 'VztQQJPdfzMslUey'");
		statusCode5XXFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter page from '54' to string 'VztQQJPdfzMslUey'");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("proximity", "silique")
				.queryParam("occurred_after", "48")
				.queryParam("proximity_square", "12")
				.queryParam("page", "VztQQJPdfzMslUey")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_rhlna7ufjdyc_GETversionincidentsformat() {
		String testResultId = "test_rhlna7ufjdyc_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("per_page", "49")
				.queryParam("occurred_after", "65")
				.queryParam("query", "bird-nest")
				.queryParam("proximity_square", "72")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_skmrqjrv5i5s_GETversionincidentsformat() {
		String testResultId = "test_skmrqjrv5i5s_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "crash")
				.queryParam("proximity", "impunity")
				.queryParam("occurred_before", "51")
				.queryParam("query", "distort")
				.queryParam("proximity_square", "40")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_t4l4u2ehgqpk_GETversionincidentsformat() {
		String testResultId = "test_t4l4u2ehgqpk_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("proximity", "salmo")
				.queryParam("occurred_after", "41")
				.queryParam("proximity_square", "13")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_sl3we8ank4f8_GETversionincidentsformat() {
		String testResultId = "test_sl3we8ank4f8_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "unconfirmed")
				.queryParam("occurred_before", "43")
				.queryParam("occurred_after", "72")
				.queryParam("page", "86")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jefgqppuwx0y_GETversionincidentsformat() {
		String testResultId = "test_1jefgqppuwx0y_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("per_page", "48")
				.queryParam("incident_type", "crash")
				.queryParam("proximity_square", "70")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_uruo7vg84t45_GETversionincidentsformat() {
		String testResultId = "test_uruo7vg84t45_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("per_page", "92")
				.queryParam("incident_type", "crash")
				.queryParam("proximity", "innkeeper")
				.queryParam("occurred_before", "37")
				.queryParam("occurred_after", "41")
				.queryParam("query", "speckle")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_to7vf8uysunp_GETversionincidentsformat() {
		String testResultId = "test_to7vf8uysunp_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("query", "vault")
				.queryParam("proximity_square", "35")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_s0x8d1dqlmqr_GETversionincidentsformat() {
		String testResultId = "test_s0x8d1dqlmqr_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "crash")
				.queryParam("query", "lionize")
				.queryParam("proximity_square", "58")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_qxtb7fyjl0l1_GETversionincidentsformat() {
		String testResultId = "test_qxtb7fyjl0l1_GETversionincidentsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "crash")
				.queryParam("proximity", "twinkler")
				.queryParam("occurred_before", "26")
				.queryParam("occurred_after", "59")
				.queryParam("query", "diazotize")
				.queryParam("proximity_square", "15")
				.queryParam("page", "18")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jia26c6s233n_GETversionincidentsidformat() {
		String testResultId = "test_1jia26c6s233n_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter id from '40' to string 'mVcaukINWKKxdn'");
		statusCode5XXFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter id from '40' to string 'mVcaukINWKKxdn'");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "mVcaukINWKKxdn")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1iv1jskff4cbo_GETversionincidentsidformat() {
		String testResultId = "test_1iv1jskff4cbo_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "35")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_vf45o8rprmt2_GETversionincidentsidformat() {
		String testResultId = "test_vf45o8rprmt2_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "22")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1h7x05n2nfdip_GETversionincidentsidformat() {
		String testResultId = "test_1h7x05n2nfdip_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "22")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1ib8snr5qiu06_GETversionincidentsidformat() {
		String testResultId = "test_1ib8snr5qiu06_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "93")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1ib6hyrbr7a02_GETversionincidentsidformat() {
		String testResultId = "test_1ib6hyrbr7a02_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "41")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_rl5589jafyas_GETversionincidentsidformat() {
		String testResultId = "test_rl5589jafyas_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "38")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_s5054d9a7rjt_GETversionincidentsidformat() {
		String testResultId = "test_s5054d9a7rjt_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "52")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_toieyuaobq5v_GETversionincidentsidformat() {
		String testResultId = "test_toieyuaobq5v_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "37")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_qxttx6p9uque_GETversionincidentsidformat() {
		String testResultId = "test_qxttx6p9uque_GETversionincidentsidformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.pathParam("id", "41")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/incidents/{id}");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_rkwryucssunr_GETversionlocationsformat() {
		String testResultId = "test_rkwryucssunr_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter proximity_square from 'null' to string 'twYUHuvDCWNp'");
		statusCode5XXFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of integer parameter proximity_square from 'null' to string 'twYUHuvDCWNp'");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "false")
				.queryParam("occurred_before", "50")
				.queryParam("occurred_after", "20")
				.queryParam("proximity_square", "twYUHuvDCWNp")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jy4xdw49ujc5_GETversionlocationsformat() {
		String testResultId = "test_1jy4xdw49ujc5_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("occurred_before", "55")
				.queryParam("proximity_square", "75")
				.queryParam("limit", "15")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jelizoqaebzl_GETversionlocationsformat() {
		String testResultId = "test_1jelizoqaebzl_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("occurred_before", "56")
				.queryParam("occurred_after", "14")
				.queryParam("query", "resolvable")
				.queryParam("proximity_square", "36")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_rkwtzj13nhpi_GETversionlocationsformat() {
		String testResultId = "test_rkwtzj13nhpi_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("proximity", "undiscoverable")
				.queryParam("occurred_after", "26")
				.queryParam("limit", "24")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1hrlyyr6xyjw6_GETversionlocationsformat() {
		String testResultId = "test_1hrlyyr6xyjw6_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "false")
				.queryParam("proximity", "tawny")
				.queryParam("occurred_after", "67")
				.queryParam("proximity_square", "92")
				.queryParam("limit", "25")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_to4kxu8tky0l_GETversionlocationsformat() {
		String testResultId = "test_to4kxu8tky0l_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("occurred_before", "92")
				.queryParam("occurred_after", "8")
				.queryParam("query", "curatorial")
				.queryParam("limit", "71")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1hv8adyz7fgok_GETversionlocationsformat() {
		String testResultId = "test_1hv8adyz7fgok_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("incident_type", "hazard")
				.queryParam("occurred_before", "66")
				.queryParam("occurred_after", "66")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jyd93mncw1no_GETversionlocationsformat() {
		String testResultId = "test_1jyd93mncw1no_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "crash")
				.queryParam("occurred_after", "59")
				.queryParam("limit", "13")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1iun4npzi1vtu_GETversionlocationsformat() {
		String testResultId = "test_1iun4npzi1vtu_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "false")
				.queryParam("proximity", "unreadable")
				.queryParam("occurred_after", "94")
				.queryParam("query", "wagnerian")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_t4nfgvnjcbja_GETversionlocationsformat() {
		String testResultId = "test_t4nfgvnjcbja_GETversionlocationsformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("occurred_before", "38")
				.queryParam("proximity_square", "72")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1jeoadswxdzjp_GETversionlocationsmarkersformat() {
		String testResultId = "test_1jeoadswxdzjp_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of boolean parameter all from 'null' to number '7240.26268160095'");
		statusCode5XXFilter.updateFaultyData(true, true, "individual_parameter_constraint:Changed value of boolean parameter all from 'null' to number '7240.26268160095'");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "7240.26268160095")
				.queryParam("incident_type", "crash")
				.queryParam("proximity", "winker")
				.queryParam("occurred_before", "94")
				.queryParam("occurred_after", "50")
				.queryParam("query", "heathlike")
				.queryParam("proximity_square", "64")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1k1uifvz0o02p_GETversionlocationsmarkersformat() {
		String testResultId = "test_1k1uifvz0o02p_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "false")
				.queryParam("proximity", "mayday")
				.queryParam("occurred_before", "43")
				.queryParam("occurred_after", "4")
				.queryParam("proximity_square", "4")
				.queryParam("limit", "92")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_rhofg4euif6r_GETversionlocationsmarkersformat() {
		String testResultId = "test_rhofg4euif6r_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("proximity", "colloquialism")
				.queryParam("occurred_before", "46")
				.queryParam("occurred_after", "30")
				.queryParam("proximity_square", "72")
				.queryParam("limit", "70")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1iauw5weang3d_GETversionlocationsmarkersformat() {
		String testResultId = "test_1iauw5weang3d_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("incident_type", "theft")
				.queryParam("occurred_before", "33")
				.queryParam("occurred_after", "60")
				.queryParam("limit", "81")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_uboo9n0fpdmd_GETversionlocationsmarkersformat() {
		String testResultId = "test_uboo9n0fpdmd_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("all", "true")
				.queryParam("query", "nonphysical")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_1hruaa2so9fzm_GETversionlocationsmarkersformat() {
		String testResultId = "test_1hruaa2so9fzm_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("proximity", "nematocera")
				.queryParam("occurred_before", "5")
				.queryParam("occurred_after", "75")
				.queryParam("query", "neoclassical")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_u7wvpl7mvltf_GETversionlocationsmarkersformat() {
		String testResultId = "test_u7wvpl7mvltf_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("incident_type", "hazard")
				.queryParam("occurred_before", "57")
				.queryParam("occurred_after", "22")
				.queryParam("query", "unlucky")
				.queryParam("proximity_square", "4")
				.queryParam("limit", "56")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_vf97ooguhohi_GETversionlocationsmarkersformat() {
		String testResultId = "test_vf97ooguhohi_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("proximity_square", "53")
				.queryParam("limit", "72")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_sl3es7mmqpnp_GETversionlocationsmarkersformat() {
		String testResultId = "test_sl3es7mmqpnp_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("occurred_before", "58")
				.queryParam("query", "centenarian")
				.queryParam("limit", "3")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

	@Test
	public void test_s1gptmq2jg37_GETversionlocationsmarkersformat() {
		String testResultId = "test_s1gptmq2jg37_GETversionlocationsmarkersformat";

		nominalOrFaultyTestCaseFilter.updateFaultyData(false, true, "none");
		statusCode5XXFilter.updateFaultyData(false, true, "none");
		csvFilter.setTestResultId(testResultId);
		statusCode5XXFilter.setTestResultId(testResultId);
		nominalOrFaultyTestCaseFilter.setTestResultId(testResultId);
		validationFilter.setTestResultId(testResultId);

		try {
			Response response = RestAssured
			.given()
				.queryParam("limit", "90")
				.filter(allureFilter)
				.filter(statusCode5XXFilter)
				.filter(nominalOrFaultyTestCaseFilter)
				.filter(validationFilter)
				.filter(csvFilter)
			.when()
				.get("/v2/locations/markers");

			response.then();
			System.out.println("Test passed.");
		} catch (RuntimeException ex) {
			System.err.println(ex.getMessage());
			fail(ex.getMessage());
		}
	}

}
