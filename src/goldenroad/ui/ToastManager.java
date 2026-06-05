package goldenroad.ui;

public class ToastManager {
    private static final long DURATION_NANOS = 2_000_000_000L;

    private String message;
    private long expireAtNanos;

    public void show(String message) {
        this.message = message;
        this.expireAtNanos = System.nanoTime() + DURATION_NANOS;
    }

    public String currentMessage() {
        if (message != null && System.nanoTime() >= expireAtNanos) {
            message = null;
        }

        return message;
    }
}
