package requests.skeleton.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {

    Object post(BaseModel model);

    Object get(Long id);

    Object update(Long id, BaseModel model);

    Object delete(Long id);
}
