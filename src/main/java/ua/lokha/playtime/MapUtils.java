package ua.lokha.playtime;

public class MapUtils {

    /**
     * Вычислить ожидаемый размер для Map'ов
     */
    public static int calculateExpectedSize(int expectedSize) {
        if (expectedSize < 3) {
            if (expectedSize < 0) {
                throw new IllegalArgumentException("expectedSize is negative " + expectedSize);
            }
            return expectedSize + 1;
        } else {
            return expectedSize < 1073741824 ? (int)((float)expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }
}
