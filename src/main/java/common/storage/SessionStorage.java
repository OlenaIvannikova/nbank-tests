package common.storage;

import api.models.UserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {

    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<UserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<UserRequest> users) {
        for (UserRequest user: users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Возвращаем объект UserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект UserRequest, соответствующий указанному порядковому номеру.
     */
    public static UserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number-1);
    }

    public static UserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number-1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
    }


}
