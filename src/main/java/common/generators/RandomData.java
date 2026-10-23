package common.generators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {

    private RandomData() {
    }

    public static BigDecimal generateTransferAmount(BigDecimal balance) {
        // Переводим баланс в центы
        BigDecimal maxCents = balance.movePointRight(2)
                .setScale(0, RoundingMode.DOWN);

        if (maxCents.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException(
                    "Balance must be at least 0.01"
            );
        }

        // Генерируем случайное количество центов
        long cents = ThreadLocalRandom.current()
                .nextLong(1, maxCents.longValueExact() + 1);

        // Преобразуем центы обратно в денежную сумму
        return BigDecimal.valueOf(cents, 2);
    }


}
