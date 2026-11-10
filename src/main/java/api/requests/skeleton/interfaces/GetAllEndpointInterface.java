package api.requests.skeleton.interfaces;

import java.util.Map;

public interface GetAllEndpointInterface {

  Object getAll(Class<?> clazz);

  Object getAllByPathParams(Map<String, ?> pathParams);
}
