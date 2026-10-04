package dev.zoroaster1x.vlcskin.launch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LauncherTest {

    @Test
    void readsTheMajorVersionFromEveryJavaScheme() {
        assertThat(Launcher.javaMajor("1.8.0_402")).isEqualTo(8);
        assertThat(Launcher.javaMajor("1.7.0_80")).isEqualTo(7);
        assertThat(Launcher.javaMajor("9")).isEqualTo(9);
        assertThat(Launcher.javaMajor("11.0.20")).isEqualTo(11);
        assertThat(Launcher.javaMajor("25.0.4")).isEqualTo(25);
        assertThat(Launcher.javaMajor(null)).isZero();
    }

    @Test
    void theInstallMessageNamesTheRightSources() {
        String message = Launcher.installMessage("1.8.0_402");
        assertThat(message).contains("Java 25");
        assertThat(message).contains("https://www.azul.com/downloads/#downloads-table-zulu");
        assertThat(message).contains("sdkman.io");
        assertThat(message).contains("PATH");
        assertThat(message).contains("1.8.0_402");
    }
}
