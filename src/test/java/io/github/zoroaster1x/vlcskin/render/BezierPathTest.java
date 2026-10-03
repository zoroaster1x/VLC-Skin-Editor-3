package io.github.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;

/**
 * The bezier port must behave like VLC's utility.
 */
class BezierPathTest {

    @Test
    void straightLineInterpolates() {
        BezierPath path = new BezierPath(new int[] {0, 100}, new int[] {0, 0});
        assertThat(path.pointAt(0f).x).isEqualTo(0f);
        assertThat(path.pointAt(0.5f).x).isBetween(49f, 51f);
        assertThat(path.pointAt(1f).x).isEqualTo(100f);
        assertThat(path.width()).isEqualTo(101);
        assertThat(path.height()).isEqualTo(1);
    }

    @Test
    void singlePointIsDuplicated() {
        BezierPath path = new BezierPath(new int[] {7}, new int[] {9});
        assertThat(path.pointAt(0f).x).isEqualTo(7f);
        assertThat(path.pointAt(1f).y).isEqualTo(9f);
        assertThat(path.sampleCount()).isEqualTo(2);
    }

    @Test
    void curveSamplesAreMonotonicInPercent() {
        BezierPath path = new BezierPath(new int[] {0, 50, 100}, new int[] {0, 100, 0});
        float previous = -1f;
        for (int i = 0; i < path.sampleCount(); i++) {
            assertThat(path.percentAtIndex(i)).isGreaterThanOrEqualTo(previous);
            previous = path.percentAtIndex(i);
        }
        assertThat(path.percentAtIndex(path.sampleCount() - 1)).isEqualTo(1f);
        Point2D.Float top = path.pointAt(0.5f);
        assertThat(top.y).isGreaterThan(40f);
    }

    @Test
    void parsesSpacesAndSeparators() {
        BezierPath path = BezierPath.parse(" ( 1 , 2 ) ,(3, 4) ");
        assertThat(path.controlCount()).isEqualTo(2);
        assertThat(path.controlX(0)).isEqualTo(1);
        assertThat(path.controlY(1)).isEqualTo(4);
    }

    @Test
    void rejectsGarbage() {
        assertThatThrownBy(() -> BezierPath.parse("nonsense"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatsBackToTheVlcSyntax() {
        BezierPath path = BezierPath.parse("(0,0),(10,20)");
        String formatted = BezierPath.format(new int[] {path.controlX(0), path.controlX(1)},
                new int[] {path.controlY(0), path.controlY(1)});
        assertThat(formatted).isEqualTo("(0,0),(10,20)");
    }
}
