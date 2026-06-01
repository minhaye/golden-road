package goldenroad.entity.item;

public record ItemUseResult(boolean success, String message) {
    public static ItemUseResult ok(String message) {
        return new ItemUseResult(true, message);
    }

    public static ItemUseResult fail(String message) {
        return new ItemUseResult(false, message);
    }
}
