package dev.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.edit.EditorSession;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Rendering semantics that follow VLC, not intuition: hidden items, clipped
 * text, and the radial slider frame formula.
 */
class VlcSemanticsTest {

    private EditorSession open(Path folder, String themeXml) throws Exception {
        Files.createDirectories(folder);
        Path file = folder.resolve("theme.xml");
        Files.writeString(file, themeXml, StandardCharsets.UTF_8);
        return EditorSession.open(file);
    }

    private BufferedImage render(EditorSession session) {
        return session.renderer().render(session.currentLayout(),
                session.renderOptions().withoutCheckerboard().withoutOverlays());
    }

    private static int redPixels(BufferedImage image, int fromX, int toX) {
        int count = 0;
        for (int x = fromX; x < Math.min(toX, image.getWidth()); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y), true);
                if (color.getAlpha() > 128 && color.getRed() > 180
                        && color.getGreen() < 100 && color.getBlue() < 100) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void invisibleItemsAreNotDrawn(@TempDir Path folder) throws Exception {
        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="hidden"/>
                  <Window id="w">
                    <Layout id="l" width="60" height="20">
                      <Text id="t" x="0" y="0" text="ABC" font="defaultfont" color="#FF0000"
                            visible="vlc.isPlaying"/>
                    </Layout>
                  </Window>
                </Theme>
                """);
        assertThat(redPixels(render(session), 0, 60)).isZero();

        session.variables().setBoolean("vlc.isPlaying", true);
        assertThat(redPixels(render(session), 0, 60)).isGreaterThan(0);
    }

    @Test
    void unresolvedVisibilityIsVisibleLikeVlc(@TempDir Path folder) throws Exception {
        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="unresolved"/>
                  <Window id="w">
                    <Layout id="l" width="60" height="20">
                      <Text id="t" x="0" y="0" text="ABC" font="defaultfont" color="#FF0000"
                            visible="notvlc.isPlaying"/>
                    </Layout>
                  </Window>
                </Theme>
                """);
        // VLC cannot resolve the token and leaves the control visible.
        assertThat(redPixels(render(session), 0, 60)).isGreaterThan(0);

        EditorSession explicit = open(folder.resolve("explicit"), """
                <Theme version="2.0">
                  <ThemeInfo name="hidden"/>
                  <Window id="w">
                    <Layout id="l" width="60" height="20">
                      <Text id="t" x="0" y="0" text="ABC" font="defaultfont" color="#FF0000"
                            visible="false"/>
                    </Layout>
                  </Window>
                </Theme>
                """);
        assertThat(redPixels(render(explicit), 0, 60)).isZero();
    }

    @Test
    void textIsClippedAndAlignmentSelectsTheVisiblePart(@TempDir Path folder) throws Exception {
        String left = """
                <Theme version="2.0">
                  <ThemeInfo name="left"/>
                  <Window id="w">
                    <Layout id="l" width="40" height="20">
                      <Text id="t" x="0" y="0" width="10" text="A         B" font="defaultfont"
                            color="#FF0000" alignment="left"/>
                    </Layout>
                  </Window>
                </Theme>
                """;
        EditorSession leftSession = open(folder.resolve("left"), left);
        BufferedImage leftImage = render(leftSession);
        assertThat(redPixels(leftImage, 0, 10)).isGreaterThan(0);
        assertThat(redPixels(leftImage, 10, 40)).isZero();

        String right = left.replace("alignment=\"left\"", "alignment=\"right\"")
                .replace("name=\"left\"", "name=\"right\"");
        EditorSession rightSession = open(folder.resolve("right"), right);
        BufferedImage rightImage = render(rightSession);
        assertThat(redPixels(rightImage, 6, 10)).isGreaterThan(0);
    }

    @Test
    void iniFileConstantsResolveColors(@TempDir Path folder) throws Exception {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("colors.ini"), "[text]\nfg=#00FF00\n");
        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="ini"/>
                  <IniFile id="colors" file="colors.ini"/>
                  <Window id="w">
                    <Layout id="l" width="40" height="20">
                      <Text id="t" x="0" y="0" text="AB" font="defaultfont" color="colors.text.fg"/>
                    </Layout>
                  </Window>
                </Theme>
                """);
        // VLC keys ini constants as id.section.key, lowercased.
        assertThat(session.index().constant("COLORS.Text.FG")).isEqualTo("#00FF00");
        assertThat(session.index().constant("colors.text.fg")).isEqualTo("#00FF00");

        BufferedImage image = render(session);
        int green = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y), true);
                if (color.getAlpha() > 128 && color.getGreen() > 180
                        && color.getRed() < 100 && color.getBlue() < 100) {
                    green++;
                }
            }
        }
        assertThat(green).as("text painted with the ini color").isGreaterThan(0);
        assertThat(dev.zoroaster1x.vlcskin.format.SkinValidator.validate(session.theme()))
                .as("no false color warning for an ini constant")
                .noneMatch(issue -> issue.message().contains("not #RRGGBB"));
    }

    @Test
    void playlistShowsVlcIdleRowsAndScrollPosition(@TempDir Path folder) throws Exception {
        Files.createDirectories(folder);
        BufferedImage thumb = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                thumb.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        ImageIO.write(thumb, "png", folder.resolve("thumb.png").toFile());

        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="playlist"/>
                  <Bitmap id="thumb" file="thumb.png" alphacolor="#FF00FF"/>
                  <Window id="w">
                    <Layout id="l" width="120" height="120">
                      <Playlist id="pl" font="defaultfont" bgcolor1="#000000" bgcolor2="#000000"
                                fgcolor="#FFFFFF" x="0" y="0" width="120" height="110">
                        <Slider id="scroll" x="112" y="5" points="(0,90),(0,4)" up="thumb"/>
                      </Playlist>
                    </Layout>
                  </Window>
                </Theme>
                """);
        BufferedImage image = render(session);

        // The two idle rows have white text near the top.
        int whiteTop = 0;
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 100; x++) {
                Color color = new Color(image.getRGB(x, y), true);
                if (color.getRed() > 200 && color.getGreen() > 200 && color.getBlue() > 200) {
                    whiteTop++;
                }
            }
        }
        assertThat(whiteTop).as("playlist row text").isGreaterThan(20);

        // VLC initialises the playlist scroll to 1.0, which puts the thumb on
        // the second curve point near the top, not at the bottom.
        int thumbTop = 0;
        int thumbBottom = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 105; x < 120; x++) {
                Color color = new Color(image.getRGB(x, y), true);
                if (color.getRed() > 200 && color.getGreen() > 200 && color.getBlue() > 200) {
                    if (y < 40) {
                        thumbTop++;
                    }
                    if (y > 70) {
                        thumbBottom++;
                    }
                }
            }
        }
        assertThat(thumbTop).as("thumb near the top").isGreaterThan(0);
        assertThat(thumbBottom).as("thumb not at the bottom").isZero();
    }

    @Test
    void sliderBackgroundUsesTheSubBitmapRegion(@TempDir Path folder) throws Exception {
        Files.createDirectories(folder);
        // A sheet whose top half is red, lower half green and blue bands; the
        // slider background names only the lower half through a SubBitmap.
        BufferedImage sheet = new BufferedImage(20, 40, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 40; y++) {
            for (int x = 0; x < 20; x++) {
                Color color = y < 20 ? Color.RED : (y < 30 ? Color.GREEN : Color.BLUE);
                sheet.setRGB(x, y, color.getRGB());
            }
        }
        ImageIO.write(sheet, "png", folder.resolve("sheet.png").toFile());
        BufferedImage thumb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(thumb, "png", folder.resolve("thumb.png").toFile());

        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="sliderbg"/>
                  <Bitmap id="sheet" file="sheet.png" alphacolor="#FF00FF">
                    <SubBitmap id="band" x="0" y="20" height="20" width="20"/>
                  </Bitmap>
                  <Bitmap id="thumbFile" file="thumb.png" alphacolor="#FF00FF"/>
                  <Window id="w">
                    <Layout id="l" width="40" height="40">
                      <Slider id="s" x="0" y="0" points="(0,0),(20,0)" up="thumbFile">
                        <SliderBackground id="sbg" image="band" nbvert="2"/>
                      </Slider>
                    </Layout>
                  </Window>
                </Theme>
                """);

        session.variables().setSliderValue(0f);
        Color first = new Color(render(session).getRGB(5, 5), true);
        assertThat(first.getGreen()).as("frame 0 is the sub bitmap's first half").isGreaterThan(180);
        assertThat(first.getRed()).isLessThan(100);

        session.variables().setSliderValue(1f);
        Color second = new Color(render(session).getRGB(5, 5), true);
        assertThat(second.getBlue()).as("frame 1 is the sub bitmap's second half").isGreaterThan(180);
        assertThat(second.getRed()).isLessThan(100);
    }

    @Test
    void radialSliderUsesTheLastFrameIndexOfTheSequence(@TempDir Path folder) throws Exception {        BufferedImage frames = new BufferedImage(4, 6, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                Color color = switch (y / 2) {
                    case 0 -> Color.RED;
                    case 1 -> Color.GREEN;
                    default -> Color.BLUE;
                };
                frames.setRGB(x, y, color.getRGB());
            }
        }
        ImageIO.write(frames, "png", folder.resolve("knob.png").toFile());

        EditorSession session = open(folder, """
                <Theme version="2.0">
                  <ThemeInfo name="radial"/>
                  <Bitmap id="knob" file="knob.png" alphacolor="#FF00FF"/>
                  <Window id="w">
                    <Layout id="l" width="10" height="10">
                      <RadialSlider id="r" x="0" y="0" sequence="knob" nbimages="3"
                                    minangle="0" maxangle="360" value="volume"/>
                    </Layout>
                  </Window>
                </Theme>
                """);

        // VLC: position = (int)(value * (nbimages - 1)); 0.8 picks frame 1, not 2.
        session.variables().setSliderValue(0.8f);
        assertThat(new Color(render(session).getRGB(1, 1), true).getGreen()).isGreaterThan(180);
        session.variables().setSliderValue(1f);
        assertThat(new Color(render(session).getRGB(1, 1), true).getBlue()).isGreaterThan(180);
        session.variables().setSliderValue(0f);
        assertThat(new Color(render(session).getRGB(1, 1), true).getRed()).isGreaterThan(180);
    }
}
