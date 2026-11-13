package common.generators;

import api.models.BaseModel;
import api.models.CreatedEntity;
import api.models.DeletionCredentials;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TestDataStorage {

  private static final ThreadLocal<TestDataStorage> INSTANCE =
      ThreadLocal.withInitial(TestDataStorage::new);

  private final ConcurrentLinkedDeque<CreatedEntity> createdEntities =
      new ConcurrentLinkedDeque<>();

  private TestDataStorage() {}

  public static TestDataStorage getStorage() {
    return INSTANCE.get();
  }

  public void addCreatedEntity(
      Endpoint endpoint, BaseModel model, DeletionCredentials credentials) {
    Long id = getEntityId(model);

    if (id == null) {
      return;
    }

    DeletionCredentials deletionCredentials =
        credentials != null ? credentials : DeletionCredentials.admin();

    createdEntities.addLast(new CreatedEntity(endpoint, id, deletionCredentials));
  }

  public void deleteCreatedEntities() {
    deleteCreatedEntities(false);
  }

  public void deleteCreatedEntitiesExceptAccounts() {
    deleteCreatedEntities(true);
  }

  private void deleteCreatedEntities(boolean skipAccounts) {
    try {
      List<CreatedEntity> snapshot = new ArrayList<>(createdEntities);

      Collections.reverse(snapshot);

      for (CreatedEntity entity : snapshot) {
        if (skipAccounts && entity.getEndpoint() == Endpoint.ACCOUNT_DELETION) {
          continue;
        }

        try {
          var requestSpec =
              RequestSpecs.authAsUser(
                  entity.getCredentials().getUsername(), entity.getCredentials().getPassword());

          new CrudRequester(requestSpec, entity.getEndpoint(), ResponseSpecs.requestReturnsOK())
              .delete(entity.getId());

        } catch (Exception e) {
          // Здесь можно добавить логирование ошибки очистки.
        }
      }
    } finally {
      createdEntities.clear();
      INSTANCE.remove();
    }
  }

  private Long getEntityId(BaseModel model) {
    try {
      var idField = model.getClass().getDeclaredField("id");
      idField.setAccessible(true);

      try {
        Object value = idField.get(model);

        return value == null ? null : Long.valueOf(value.toString());

      } finally {
        idField.setAccessible(false);
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("Cannot get id of entity", e);
    }
  }
}
