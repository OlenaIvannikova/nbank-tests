package requests.skeleton.interfaces;

import models.BaseModel;

import java.util.List;

public interface GetAllEndpointInterface {
    <T extends BaseModel> List<T> getAll(Class<T> clazz);
}
