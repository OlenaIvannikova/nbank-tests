package common.generators;

import api.models.BaseModel;
import api.models.DeletionCredentials;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.*;

public class TestDataStorage {

    private static TestDataStorage testDataStorage;
    private final EnumMap<Endpoint, Map<Long, DeletionCredentials>>
            createdEntitiesMap = new EnumMap<>(Endpoint.class);

    private TestDataStorage() {
    }

    public static TestDataStorage getStorage() {
        if (testDataStorage == null) {
            testDataStorage = new TestDataStorage();
        }
        return testDataStorage;
    }

    public void addCreatedEntity(
            Endpoint endpoint,
            BaseModel model,
            DeletionCredentials credentials
    ) {
        addCreatedEntity(endpoint, getEntityId(model), credentials);
    }

    private void addCreatedEntity(
            Endpoint endpoint,
            Long id,
            DeletionCredentials credentials
    ) {
        if (id != null) {
            createdEntitiesMap
                    .computeIfAbsent(endpoint, key -> new HashMap<>())
                    .put(id, credentials);
        }
    }

    public void deleteCreatedEntities() {
        createdEntitiesMap.forEach((endpoint, entities) ->
                entities.forEach((id, credentials) -> {
                    var requestSpec = credentials == null
                            ? RequestSpecs.adminSpec()
                            : RequestSpecs.authAsUser(
                            credentials.getUsername(),
                            credentials.getPassword()
                    );

                    new CrudRequester(
                            requestSpec,
                            endpoint,
                            ResponseSpecs.requestReturnsOK()
                    ).delete(id);
                })
        );

        createdEntitiesMap.clear();
    }


    private Long getEntityId(BaseModel model) {
        try {
            var idField = model.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            var idFieldValue = Long.valueOf(
                    Objects.toString(idField.get(model)));
            idField.setAccessible(false);
            return idFieldValue;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot get id of entity", e);
        }
    }


}
