package dev.zoroaster1x.vlcskin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void theVersionComesFromTheBuildProperties() {
        assertThat(Version.VERSION).matches("\\d+\\.\\d+\\.\\d+");
        assertThat(Version.NAME).isNotBlank();
    }
}
