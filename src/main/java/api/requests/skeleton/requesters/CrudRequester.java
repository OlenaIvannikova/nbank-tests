package api.requests.skeleton.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import api.validation.PathParamValidator;

import java.util.Map;

import static io.restassured.RestAssured.given;

// Необходим в случае неуспешных запросов
public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {

        var body = model == null ? "" : model;

        return given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(Long id) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }


    @Override
    public ValidatableResponse getAllByPathParams(Map<String, ?> pathParams) {
        String url = endpoint.getUrl();

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
                .put(endpoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(Long id) {
        return given()
                .spec(requestSpecification)
                .delete(endpoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
