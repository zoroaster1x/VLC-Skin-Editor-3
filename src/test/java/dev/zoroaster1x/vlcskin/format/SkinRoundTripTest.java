package dev.zoroaster1x.vlcskin.format;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.MenuItemEntry;
import dev.zoroaster1x.vlcskin.model.resource.MenuSeparatorEntry;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * A theme with every element survives write, parse and write again.
 */
class SkinRoundTripTest {

    @Test
    void everyElementSurvivesARoundTrip() {
        SkinTheme theme = buildFullTheme();
        String xml = SkinWriter.toXml(theme);
        SkinParser.Result result = SkinParser.parse(xml.getBytes(StandardCharsets.UTF_8), "test.xml");
        assertThat(result.issues()).noneMatch(issue -> issue.severity() == ParseIssue.Severity.ERROR);
        SkinTheme parsed = result.theme();

        assertThat(parsed.getThemeInfo().getName()).isEqualTo("Round trip");
        assertThat(parsed.getResources()).hasSize(6);
        assertThat(parsed.getWindows()).hasSize(1);

        SkinLayout layout = parsed.getWindows().get(0).getLayouts().get(0);
        assertThat(layout.getWidth()).isEqualTo(640);
        assertThat(layout.getHeight()).isEqualTo(480);
        assertThat(layout.getItems()).hasSize(12);

        assertThat(layout.getItems().get(0)).isInstanceOf(AnchorItem.class);
        assertThat(layout.getItems().get(1)).isInstanceOf(ButtonItem.class);
        assertThat(layout.getItems().get(2)).isInstanceOf(CheckboxItem.class);
        assertThat(layout.getItems().get(3)).isInstanceOf(GroupItem.class);
        assertThat(layout.getItems().get(4)).isInstanceOf(ImageItem.class);
        assertThat(layout.getItems().get(5)).isInstanceOf(PanelItem.class);
        assertThat(layout.getItems().get(6)).isInstanceOf(PlaytreeItem.class);
        assertThat(layout.getItems().get(7)).isInstanceOf(RadialSliderItem.class);
        assertThat(layout.getItems().get(8)).isInstanceOf(SliderItem.class);
        assertThat(layout.getItems().get(9)).isInstanceOf(TextItem.class);
        assertThat(layout.getItems().get(10)).isInstanceOf(VideoItem.class);
        assertThat(layout.getItems().get(11)).isInstanceOf(GroupItem.class);

        GroupItem group = (GroupItem) layout.getItems().get(3);
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems().get(0)).isInstanceOf(TextItem.class);

        SliderItem slider = (SliderItem) layout.getItems().get(8);
        assertThat(slider.getBackground()).isNotNull();
        assertThat(slider.getBackground().getNbhoriz()).isEqualTo(3);
        assertThat(slider.getPoints()).isEqualTo("(0,0),(200,50),(200,200)");
        assertThat(slider.getThickness()).isEqualTo(12);

        PlaytreeItem playtree = (PlaytreeItem) layout.getItems().get(6);
        assertThat(playtree.getSlider()).isNotNull();
        assertThat(playtree.isPlaylistSyntax()).isTrue();

        // Write again; the second output must equal the first because nothing was lost.
        String second = SkinWriter.toXml(parsed);
        assertThat(second).isEqualTo(xml);
    }

    @Test
    void htmlCharactersAreEscapedAndComeBack() {
        SkinTheme theme = buildFullTheme();
        theme.getThemeInfo().setName("A & B <\"quoted\">");
        String xml = SkinWriter.toXml(theme);
        assertThat(xml).contains("A &amp; B &lt;&quot;quoted&quot;&gt;");
        SkinTheme parsed = SkinParser.parse(xml.getBytes(StandardCharsets.UTF_8), "x.xml").theme();
        assertThat(parsed.getThemeInfo().getName()).isEqualTo("A & B <\"quoted\">");
    }

    @Test
    void unknownAttributesAndChildrenAreKept() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE Theme PUBLIC "-//VideoLAN//DTD VLC Skins V2.0//EN" "skin.dtd">
                <Theme version="2.0" futureattr="42">
                  <ThemeInfo name="Keep"/>
                  <Bitmap id="b" file="b.png" alphacolor="#FF00FF" newschool="yes"/>
                  <FutureThing kind="mystery"/>
                  <Window id="main" futurewin="1">
                    <Layout id="l" width="10" height="10">
                      <Text id="t" text="hi" font="defaultfont" explicitextra="ok"/>
                      <FutureControl foo="bar"/>
                    </Layout>
                  </Window>
                </Theme>
                """;
        SkinParser.Result result = SkinParser.parse(xml.getBytes(StandardCharsets.UTF_8), "keep.xml");
        SkinTheme theme = result.theme();
        assertThat(theme.foreignAttributes()).containsEntry("futureattr", "42");
        assertThat(theme.getResources().get(0).foreignAttributes()).containsEntry("newschool", "yes");
        assertThat(theme.unknownChildren()).anyMatch(child -> child.contains("FutureThing"));
        SkinLayout layout = theme.getWindows().get(0).getLayouts().get(0);
        assertThat(layout.unknownChildren()).anyMatch(child -> child.contains("FutureControl"));
        assertThat(layout.getItems()).hasSize(1);
        assertThat(layout.getItems().get(0).foreignAttributes()).containsEntry("explicitextra", "ok");

        String written = SkinWriter.toXml(theme);
        assertThat(written).contains("futureattr=\"42\"");
        assertThat(written).contains("FutureThing");
        assertThat(written).contains("FutureControl");
    }

    @Test
    void idNoneBecomesAGeneratedUniqueId() {
        String xml = """
                <Theme version="2.0">
                  <Window id="main">
                    <Layout id="l" width="10" height="10">
                      <Text id="none" text="a" font="defaultfont"/>
                      <Text id="none" text="b" font="defaultfont"/>
                    </Layout>
                  </Window>
                </Theme>
                """;
        SkinTheme theme = SkinParser.parse(xml.getBytes(StandardCharsets.UTF_8), "ids.xml").theme();
        SkinLayout layout = theme.getWindows().get(0).getLayouts().get(0);
        assertThat(layout.getItems().get(0).getId()).isNotEqualTo("none");
        assertThat(layout.getItems().get(1).getId()).isNotEqualTo("none");
        assertThat(layout.getItems().get(0).getId()).isNotEqualTo(layout.getItems().get(1).getId());
    }

    private SkinTheme buildFullTheme() {
        SkinTheme theme = new SkinTheme();
        theme.getThemeInfo().setName("Round trip");
        theme.getThemeInfo().setAuthor("Tester");
        theme.getThemeInfo().setEmail("test@example.com");
        theme.getThemeInfo().setWebpage("https://example.com");
        theme.setMagnet(20);
        theme.setAlpha(240);

        BitmapResource bitmap = new BitmapResource();
        bitmap.setId("main_bitmap");
        bitmap.setFile("assets/main.png");
        bitmap.setNbframes(4);
        bitmap.setFps(12);
        SubBitmap sub = new SubBitmap();
        sub.setId("icon_play");
        sub.setX(4);
        sub.setY(8);
        sub.setWidth(24);
        sub.setHeight(24);
        bitmap.getSubBitmaps().add(sub);
        theme.getResources().add(bitmap);

        FontResource font = new FontResource();
        font.setId("big_font");
        font.setFile("assets/font.ttf");
        font.setSize(28);
        theme.getResources().add(font);

        BitmapFontResource bitmapFont = new BitmapFontResource();
        bitmapFont.setId("digits");
        bitmapFont.setFile("assets/digits.png");
        theme.getResources().add(bitmapFont);

        PopupMenuResource menu = new PopupMenuResource();
        menu.setId("main_menu");
        MenuItemEntry item = new MenuItemEntry();
        item.setLabel("Open");
        item.setAction("dialogs.file()");
        menu.getEntries().add(item);
        menu.getEntries().add(new MenuSeparatorEntry());
        MenuItemEntry quit = new MenuItemEntry();
        quit.setLabel("Quit");
        quit.setAction("vlc.quit()");
        menu.getEntries().add(quit);
        theme.getResources().add(menu);

        IniFileResource ini = new IniFileResource();
        ini.setId("settings");
        ini.setFile("settings.ini");
        theme.getResources().add(ini);

        FontResource unused = new FontResource();
        unused.setId("small_font");
        unused.setFile("assets/small.ttf");
        unused.setSize(10);
        theme.getResources().add(unused);

        SkinWindow window = new SkinWindow();
        window.setId("main");
        window.setX(10);
        window.setY(20);
        window.setDragdrop(false);
        SkinLayout layout = new SkinLayout();
        layout.setId("default");
        layout.setWidth(640);
        layout.setHeight(480);
        layout.setMinwidth(320);
        layout.setMaxheight(720);

        AnchorItem anchor = new AnchorItem();
        anchor.setId("anchor1");
        anchor.setX(5);
        anchor.setY(6);
        anchor.setPoints("(0,0),(10,10)");
        anchor.setPriority(1);
        anchor.setRange(25);
        layout.getItems().add(anchor);

        ButtonItem button = new ButtonItem();
        button.setId("play");
        button.setUp("icon_play");
        button.setOver("icon_play");
        button.setDown("icon_play");
        button.setX(30);
        button.setY(40);
        button.setAction("vlc.play()");
        button.setTooltiptext("Play");
        layout.getItems().add(button);

        CheckboxItem checkbox = new CheckboxItem();
        checkbox.setId("shuffle");
        checkbox.setState("playlist.isRandom");
        checkbox.setUp1("icon_play");
        checkbox.setUp2("icon_play");
        checkbox.setX(70);
        checkbox.setY(40);
        layout.getItems().add(checkbox);

        GroupItem group = new GroupItem();
        group.setId("group1");
        group.setX(100);
        group.setY(100);
        TextItem nested = new TextItem();
        nested.setId("nested");
        nested.setText("Nested $T");
        nested.setFont("small_font");
        nested.setX(2);
        nested.setY(3);
        group.getItems().add(nested);
        layout.getItems().add(group);

        ImageItem image = new ImageItem();
        image.setId("background");
        image.setImage("main_bitmap");
        image.setResize("scale");
        image.setX(0);
        image.setY(0);
        layout.getItems().add(image);

        PanelItem panel = new PanelItem();
        panel.setId("panel1");
        panel.setX(200);
        panel.setY(200);
        panel.setWidth(300);
        panel.setHeight(150);
        panel.setRightbottom("rightbottom");
        layout.getItems().add(panel);

        PlaytreeItem playtree = new PlaytreeItem();
        playtree.setId("playlist1");
        playtree.setPlaylistSyntax(true);
        playtree.setX(10);
        playtree.setY(250);
        playtree.setWidth(200);
        playtree.setHeight(200);
        playtree.setFont("small_font");
        SliderItem playtreeSlider = new SliderItem();
        playtreeSlider.setId("playlist_scroll");
        playtreeSlider.setUp("icon_play");
        playtreeSlider.setInPlaytree(true);
        playtree.setSlider(playtreeSlider);
        layout.getItems().add(playtree);

        RadialSliderItem radial = new RadialSliderItem();
        radial.setId("volume_knob");
        radial.setSequence("main_bitmap");
        radial.setNbimages(4);
        radial.setMinangle(-135);
        radial.setMaxangle(135);
        radial.setValue("volume");
        radial.setX(300);
        radial.setY(300);
        layout.getItems().add(radial);

        SliderItem slider = new SliderItem();
        slider.setId("seek");
        slider.setUp("icon_play");
        slider.setPoints("(0,0),(200,50),(200,200)");
        slider.setThickness(12);
        slider.setValue("time");
        slider.setX(50);
        slider.setY(50);
        SliderBackground background = new SliderBackground();
        background.setId("seek_bg");
        background.setImage("main_bitmap");
        background.setNbhoriz(3);
        background.setNbvert(2);
        background.setPadhoriz(1);
        background.setPadvert(1);
        slider.setBackground(background);
        layout.getItems().add(slider);

        TextItem text = new TextItem();
        text.setId("clock");
        text.setText("$T");
        text.setFont("big_font");
        text.setColor("#FFFFFF");
        text.setWidth(120);
        text.setAlignment("right");
        text.setScrolling("none");
        text.setX(500);
        text.setY(400);
        layout.getItems().add(text);

        VideoItem video = new VideoItem();
        video.setId("video_out");
        video.setWidth(640);
        video.setHeight(360);
        layout.getItems().add(video);

        GroupItem emptyGroup = new GroupItem();
        emptyGroup.setId("empty_group");
        emptyGroup.setX(1);
        emptyGroup.setY(2);
        layout.getItems().add(emptyGroup);

        window.getLayouts().add(layout);
        theme.getWindows().add(window);
        return theme;
    }
}
