package common.storage;

import api.models.UserRequest;
import api.requests.steps.UserSteps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
  /* Thread Local - способ сделать SessionStorage потокобезопасным

  Каждый поток обращаясь к INSTANCE.get() получают свою КОПИЮ

  Map<Thread, SessionStorage>
  Тест1 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ1), работает с ними
  Тест2 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ2), работает с ними
  Тест3 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ3), работает с ними
   */

  private static final ThreadLocal<SessionStorage> INSTANCE =
      ThreadLocal.withInitial(SessionStorage::new);

  private final LinkedHashMap<UserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

  private SessionStorage() {}

  public static void addUsers(List<UserRequest> users) {
    for (UserRequest user : users) {
      INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
    }
  }

  /**
   * Возвращаем объект UserRequest по его порядковому номеру в списке созданных пользователей.
   *
   * @param number Порядковый номер, начиная с 1 (а не с 0).
   * @return Объект UserRequest, соответствующий указанному порядковому номеру.
   */
  public static UserRequest getUser(int number) {
    return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number - 1);
  }

  public static UserRequest getUser() {
    return getUser(1);
  }

  public static UserSteps getSteps(int number) {
    return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number - 1);
  }

  public static UserSteps getSteps() {
    return getSteps(1);
  }

  public static void clear() {
    INSTANCE.get().userStepsMap.clear();
    INSTANCE.remove();
  }
}
