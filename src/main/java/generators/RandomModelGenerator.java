package generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RandomModelGenerator {

    private static final Random random = new Random();

    public static <T> T generate(Class<T> clazz) {
        try {
            // Создаём объект: Reflection вызывает объявленный конструктор без параметров и создает объект
            T instance = clazz.getDeclaredConstructor().newInstance();
            // Получаем все поля
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);

                Object value;
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                // Выбираем способ генерации:
                // Если есть @GeneratingRule, то генерация по regex
                if (rule != null) {
                    value = generateFromRegex(rule.regex(), field.getType());
                }
                // Нет аннотации, то случайное значение по типу
                else {
                    value = generateRandomValue(field);
                }
                // Записываем значения (value) в соответствующее поле объекта instance
                field.set(instance, value);
            }
            // Когда все поля обработаны возвращаем готовый заполненный объект
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity", e);
        }
    }

    // Метод getAllFields(clazz) возвращает список полей класса. Цикл последовательно обрабатывает каждое поле
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        // // Цикл продолжается, пока:
        // clazz != null — текущий класс существует
        // clazz != Object.class — мы ещё не дошли до базового класса Java Object
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    // Метод выбирает генератор в зависимости от типа поля
    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            return UUID.randomUUID().toString().substring(0, 8);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return random.nextInt(1000); // число в диапазоне от 0 до 999
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return random.nextLong(); // случайное значение, которое может быть отрицательным или положительным и иметь очень большую абсолютную величину.
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return random.nextDouble() * 100; // диапазон от 0.0 до значения, меньшего 100.0
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return random.nextBoolean();
        } else if (type.equals(List.class)) {
            return generateRandomList(field);
        } else if (type.equals(Date.class)) {
            return new Date(System.currentTimeMillis() - random.nextInt(1000000000)); // создаёт дату от текущего момента до примерно 11,6 дня назад
        } else {
            // Вложенный объект
            return generate(type);
        }
    }

    // Метод умеет преобразовывать результат только в Integer и Long. Для остальных типов он возвращает String
    // Поскольку метод вернёт String, а field.set() не сможет записать его в Double -код упадёт
    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String result = rgxGen.generate();
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);
        } else {
            return result;
        }
    }

    private static List<String> generateRandomList(Field field) {
        // Пытаемся определить generic-параметр списка
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType == String.class) {
                return List.of(UUID.randomUUID().toString().substring(0, 5),
                        UUID.randomUUID().toString().substring(0, 5));
            }
        }
        return Collections.emptyList();
    }
}