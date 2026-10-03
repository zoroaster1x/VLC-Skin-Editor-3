package io.github.zoroaster1x.vlcskin.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Writes an animated GIF with the JDK's own GIF writer; no dependencies.
 * Per-frame delays let action frames run at 30fps while key frames hold.
 */
public final class GifWriter {

    private GifWriter() {
    }

    /**
     * Writes every frame with the same delay.
     */
    public static void write(List<BufferedImage> frames, int delayMillis, Path target) throws IOException {
        write(frames, java.util.Collections.nCopies(frames.size(), delayMillis), target);
    }

    public static void write(List<BufferedImage> frames, List<Integer> delaysMillis, Path target) throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("no frames");
        }
        if (frames.size() != delaysMillis.size()) {
            throw new IllegalArgumentException("frames and delays must match");
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(new File(target.toString()))) {
            writer.setOutput(stream);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage frame = frames.get(i);
                ImageWriteParam params = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromRenderedImage(frame), params);
                configure(metadata, delaysMillis.get(i));
                writer.writeToSequence(new IIOImage(frame, null, metadata), params);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private static void configure(IIOMetadata metadata, int delayMillis) throws IOException {
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
        IIOMetadataNode gce = child(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", Integer.toString(Math.max(2, Math.round(delayMillis / 10f))));
        gce.setAttribute("transparentColorIndex", "0");
        IIOMetadataNode extensions = child(root, "ApplicationExtensions");
        IIOMetadataNode application = new IIOMetadataNode("ApplicationExtension");
        application.setAttribute("applicationID", "NETSCAPE");
        application.setAttribute("authenticationCode", "2.0");
        application.setUserObject(new byte[] {0x1, 0x0, 0x0});
        extensions.appendChild(application);
        metadata.setFromTree(format, root);
    }

    private static IIOMetadataNode child(IIOMetadataNode parent, String name) {
        for (int i = 0; i < parent.getLength(); i++) {
            if (parent.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) parent.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        parent.appendChild(node);
        return node;
    }
}
