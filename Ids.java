package domaine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Génère des ids simples pour le domaine (thread-safe).
 */
public final class Ids {
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private Ids() {}

    public static int next() {
        return SEQ.getAndIncrement();
    }
}

