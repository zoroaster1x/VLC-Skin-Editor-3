package dev.zoroaster1x.vlcskin.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ThemeGalleryClientTest {

    @Test
    void parsesTheShowSkinBoxRows() {
        String html = """
                <div class="skin-container" onclick="showSkinBox(267,'eDark Vlc','Aaron A','2010-12-19',\
                '1380895','eDark Vlc.vlt','271.2KiB',9.0883084577114428,22512,\
                'http://images.videolan.org/vlc/skins2/Preview.jpg','1','0.9.0')">
                <div class="skin-container" onclick="showSkinBox(12,'Neon','Someone','2020-01-02',\
                '42','Neon.vlt','12KiB',4.5,7,'','0','2.0')">
                """;

        List<GalleryTheme> themes = ThemeGalleryClient.parse(html);

        assertThat(themes).hasSize(2);
        GalleryTheme first = themes.get(0);
        assertThat(first.id()).isEqualTo(267);
        assertThat(first.name()).isEqualTo("eDark Vlc");
        assertThat(first.author()).isEqualTo("Aaron A");
        assertThat(first.file()).isEqualTo("eDark Vlc.vlt");
        assertThat(first.size()).isEqualTo("271.2KiB");
        assertThat(first.downloads()).isEqualTo(1380895);
        assertThat(first.previewUrl()).contains("Preview.jpg");
        assertThat(first.version()).isEqualTo("0.9.0");
        assertThat(first.folderName()).isEqualTo("eDark_Vlc");
    }

    @Test
    void keepsApostrophesInNames() {
        String html = "showSkinBox(1,'O\\'Neil\\'s Skin','D\\'Arcy','2024-01-01','5','one.vlt','1KiB',"
                + "5.0,1,'','0','1.0')";

        List<GalleryTheme> themes = ThemeGalleryClient.parse(html);

        assertThat(themes).hasSize(1);
        assertThat(themes.get(0).name()).isEqualTo("O'Neil's Skin");
        assertThat(themes.get(0).author()).isEqualTo("D'Arcy");
    }
}
