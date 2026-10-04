package dev.zoroaster1x.vlcskin.launch;

import java.lang.reflect.Method;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * The jar entry point. This class is compiled for Java 8 on purpose: it is the
 * only thing an old JVM can load from the jar, so instead of dying with
 * UnsupportedClassVersionError it can explain which Java to install. On Java 25
 * or newer it hands control to the real application reflectively.
 */
public final class Launcher {

    public static final int REQUIRED_JAVA = 25;
    private static final String APP_MAIN = "dev.zoroaster1x.vlcskin.app.VlcSkinStudio";
    private static final String AZUL = "https://www.azul.com/downloads/#downloads-table-zulu";
    private static final String SDKMAN = "https://sdkman.io/";

    private Launcher() {
    }

    public static void main(String[] args) {
        int major = javaMajor(System.getProperty("java.version"));
        if (major < REQUIRED_JAVA) {
            reportOldJava(System.getProperty("java.version", "unknown"));
            System.exit(2);
        }
        try {
            Class<?> app = Class.forName(APP_MAIN);
            Method main = app.getMethod("main", String[].class);
            main.invoke(null, (Object) args);
        } catch (ReflectiveOperationException ex) {
            System.err.println("Could not start VLC Skin Studio: " + ex);
            System.exit(3);
        }
    }

    /**
     * The major version from a {@code java.version} property: {@code 1.8.0_402}
     * is 8, {@code 9} is 9, {@code 25.0.4} is 25.
     */
    public static int javaMajor(String version) {
        if (version == null) {
            return 0;
        }
        int index = 0;
        while (index < version.length() && !Character.isDigit(version.charAt(index))) {
            index++;
        }
        int start = index;
        while (index < version.length() && Character.isDigit(version.charAt(index))) {
            index++;
        }
        if (start == index) {
            return 0;
        }
        try {
            int major = Integer.parseInt(version.substring(start, index));
            if (major == 1 && index < version.length() && version.charAt(index) == '.') {
                int second = index + 1;
                int end = second;
                while (end < version.length() && Character.isDigit(version.charAt(end))) {
                    end++;
                }
                if (end > second) {
                    return Integer.parseInt(version.substring(second, end));
                }
            }
            return major;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * The install instructions, shared by the dialog and the console fallback.
     */
    public static String installMessage(String runningVersion) {
        return "VLC Skin Studio needs Java " + REQUIRED_JAVA + " or newer.\n"
                + "You are running Java " + runningVersion + ".\n\n"
                + "Windows: download the Zulu build of Java " + REQUIRED_JAVA + " from\n"
                + AZUL + "\n"
                + "After installing you may need to edit your PATH so the new java.exe is found\n"
                + "first. Open a new command prompt and check with: java -version\n\n"
                + "Linux and macOS: SDKMAN installs Java and selects the default in a few commands:\n"
                + "  curl -s \"https://get.sdkman.io\" | bash\n"
                + "  sdk install java " + REQUIRED_JAVA + "-zulu\n"
                + "  sdk default java " + REQUIRED_JAVA + "-zulu\n"
                + "It takes care of PATH and keeps several Java versions selectable.\n"
                + SDKMAN + "\n\n"
                + "Then start the editor again: java -jar vlc-skin-studio.jar";
    }

    private static void reportOldJava(String runningVersion) {
        String message = installMessage(runningVersion);
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println(message);
            return;
        }
        try {
            SwingUtilities.invokeAndWait(() -> showDialog(message));
        } catch (Exception ex) {
            System.err.println(message);
        }
    }

    private static void showDialog(String message) {
        JLabel body = new JLabel("<html><body style='width:520px;font-family:sans-serif'>"
                + message.replace("\n", "<br>")
                + "</body></html>");
        Object[] options = {"Open Azul downloads", "Open SDKMAN", "Close"};
        int choice = JOptionPane.showOptionDialog(null, body,
                "VLC Skin Studio needs Java " + REQUIRED_JAVA,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
        if (choice == 0) {
            open(AZUL);
        } else if (choice == 1) {
            open(SDKMAN);
        }
    }

    private static void open(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception ex) {
            // Opening the browser is best effort.
        }
    }
}
