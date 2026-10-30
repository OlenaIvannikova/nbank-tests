package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
В новом образе with_database_with_fix бекенда не поддерживается DELETE /accounts.
Поэтому сделаем аннотацию APIVersion:
 - при @APIVersion("with_database") отключается удаление аккаунтов,
 - при @APIVersion("with_validation_fix") или отсутствии этой аннотации очистка аккаунтов продолжает работать
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface APIVersion {
    String value();
}
