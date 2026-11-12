package api.requests.skeleton.requesters;

import static io.restassured.RestAssured.given;

import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import api.validation.PathParamValidator;
import common.configs.Config;
import common.configs.Property;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Map;

// Необходим в случае неуспешных запросов
public class CrudRequester extends HttpRequest
    implements CrudEndpointInterface, GetAllEndpointInterface {
  private static final String API_VERSION = Config.getProperty(Property.API_VERSION);

  public CrudRequester(
      RequestSpecification requestSpecification,
      Endpoint endpoint,
      ResponseSpecification responseSpecification) {
    super(requestSpecification, endpoint, responseSpecification);
  }

  @Override
  public ValidatableResponse post(BaseModel model) {

    var body = model == null ? "" : model;

    return given()
        .spec(requestSpecification)
        .body(body)
        .post(API_VERSION + endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse get(Long id) {
    return given()
        .spec(requestSpecification)
        .get(API_VERSION + endpoint.getUrl() + "/" + id)
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse get() {
    return given()
        .spec(requestSpecification)
        .get(API_VERSION + endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse getAllByPathParams(Map<String, ?> pathParams) {
    String url = API_VERSION + endpoint.getUrl();

    PathParamValidator.validate(url, pathParams);

    return given()
        .spec(requestSpecification)
        .pathParams(pathParams)
        .get(url)
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse update(Long id, BaseModel model) {
    return given()
        .spec(requestSpecification)
        .body(model)
        .put(API_VERSION + endpoint.getUrl() + "/" + id)
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse update(BaseModel model) {
    return given()
        .spec(requestSpecification)
        .body(model)
        .put(API_VERSION + endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse delete(Long id) {
    return given()
        .spec(requestSpecification)
        .delete(API_VERSION + endpoint.getUrl() + "/" + id)
        .then()
        .assertThat()
        .spec(responseSpecification);
  }

  @Override
  public ValidatableResponse getAll(Class<?> clazz) {
    return given()
        .spec(requestSpecification)
        .get(API_VERSION + endpoint.getUrl())
        .then()
        .assertThat()
        .spec(responseSpecification);
  }
}
