package requests.skeleton.requesters;

import generators.TestDataStorage;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.DeletionCredentials;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;
import requests.skeleton.interfaces.GetAllEndpointInterface;
import specs.RequestSpecs;

import java.util.List;
import java.util.Map;

// Необходим для десериализации в объект в случае успешных запросов с непустым телом ответа
public class ValidatedCrudRequester<M extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private CrudRequester crudRequester;
    private final DeletionCredentials deletionCredentials;

    public ValidatedCrudRequester(
            RequestSpecification requestSpec,
            Endpoint endpoint,
            ResponseSpecification responseSpec
    ) {
        this(requestSpec, endpoint, responseSpec, null);
    }

    private ValidatedCrudRequester(
            RequestSpecification requestSpec,
            Endpoint endpoint,
            ResponseSpecification responseSpec,
            DeletionCredentials deletionCredentials
    ) {
        super(requestSpec, endpoint, responseSpec);
        this.crudRequester = new CrudRequester(
                requestSpec, endpoint, responseSpec
        );
        this.deletionCredentials = deletionCredentials;
    }

    public static <M extends BaseModel> ValidatedCrudRequester<M> authenticatedAsUser(
            String username,
            String password,
            Endpoint endpoint,
            ResponseSpecification responseSpec
    ) {
        return new ValidatedCrudRequester<>(
                RequestSpecs.authAsUser(username, password),
                endpoint,
                responseSpec,
                new DeletionCredentials(username, password)
        );
    }


    @Override
    public M post(BaseModel model) {
        var createdModel = (M) crudRequester.post(model)
                .extract()
                .as(endpoint.getResponseModel());

        TestDataStorage.getStorage()
                .addCreatedEntity(endpoint, createdModel, deletionCredentials);

        return createdModel;
    }

    @Override
    public M get(Long id) {
        return (M) crudRequester.get(id)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M get() {
        return (M) crudRequester.get()
                .extract()
                .as(endpoint.getResponseModel());
    }

    @SuppressWarnings("unchecked")
    private Class<M> getResponseModel() {
        return (Class<M>) endpoint.getResponseModel();
    }

    @Override
    public List<M> getByPathParams(Map<String, ?> pathParams) {
        return ((ValidatableResponse) crudRequester.getByPathParams(pathParams))
                .extract()
                .jsonPath()
                .getList("", getResponseModel());
    }

    @Override
    public M update(Long id, BaseModel model) {
        return (M) crudRequester.update(id, model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M update(BaseModel model) {
        return (M) crudRequester.update(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M delete(Long id) {
        return (M) crudRequester.delete(id)
                .extract()
                .as(endpoint.getResponseModel());
    }

    public String deleteAsString(Long id) {
        return ((ValidatableResponse) crudRequester.delete(id))
                .extract()
                .asString();
    }

    @Override
    public <M extends BaseModel> List<M> getAll(Class<M> clazz) {
        return crudRequester.getAll()
                .extract()
                .jsonPath()
                .getList("", clazz);
    }
}
