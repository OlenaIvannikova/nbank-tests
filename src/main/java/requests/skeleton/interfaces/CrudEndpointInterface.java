package requests.skeleton.interfaces;

import models.BaseModel;

import java.util.Map;

public interface CrudEndpointInterface {

    Object post(BaseModel model);

    Object get(Long id);

    Object get();

    Object getByPathParams(Map<String, ?> pathParams);

    Object update(Long id, BaseModel model);

    Object update(BaseModel model);

    Object delete(Long id);
}
