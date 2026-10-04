package dev.zoroaster1x.vlcskin.util;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The two lines that open and close a session. The startup line says where
 * this project comes from; the closing pair is the signature.
 *
 * <p>MCP servers write to stderr, because stdout is the JSON-RPC channel.
 * The goodbye is printed once, from a shutdown hook unless a close path
 * already printed it, so it really is the last thing a user sees.
 */
public final class Banner {

    private static final AtomicBoolean STARTUP_SAID = new AtomicBoolean();
    private static final AtomicBoolean GOODBYE_SAID = new AtomicBoolean();
    private static final AtomicBoolean HOOK_INSTALLED = new AtomicBoolean();
    private static volatile PrintStream out = System.out;

    private Banner() {
    }

    public static void startup() {
        startup(System.out);
    }

    public static void startup(PrintStream stream) {
        out = stream;
        if (!STARTUP_SAID.compareAndSet(false, true)) {
            return;
        }
        String ready = "Ready in " + Startup.elapsedText();
        stream.println("Biji Kurdistan");
        stream.println(ready);
        AppLog.line("SYSTEM", "Biji Kurdistan");
        AppLog.line("SYSTEM", ready);
        if (HOOK_INSTALLED.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(Banner::goodbyeOnce,
                    "vlc-skin-goodbye"));
        }
    }

    public static void goodbye() {
        goodbyeOnce();
    }

    /**
     * Forgets the once-only flags, for tests. The shutdown hook stays
     * installed; hooks cannot be removed.
     */
    public static void resetForTests() {
        STARTUP_SAID.set(false);
        GOODBYE_SAID.set(false);
    }

    private static void goodbyeOnce() {
        if (!GOODBYE_SAID.compareAndSet(false, true)) {
            return;
        }
        PrintStream stream = out;
        stream.println("Made with love in Kurdistan");
        stream.println("Good bye!");
        AppLog.line("SYSTEM", "Made with love in Kurdistan");
        AppLog.line("SYSTEM", "Good bye!");
        AppLog.close();
    }
}
