package requests.skeleton.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;

// Необходим для десериализации в объект в случае успешных запросов с непустым телом ответа
public class ValidatedCrudRequester<M extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }


    @Override
    public M post(BaseModel model) {
        return (M) crudRequester.post(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M get(Long id) {
        return (M) crudRequester.get(id)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M update(Long id, BaseModel model) {
        return (M) crudRequester.update(id, model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public M delete(Long id) {
        return (M) crudRequester.delete(id)
                .extract()
                .as(endpoint.getResponseModel());
    }
}
