package com.jaha.server.emaul.util;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

/**
 * Created by doring on 15. 3. 11..
 */
public class Thumbnails {
    public static File create(File src) {
        return create(src, 250, 250);
    }

    public static File create(File src, int thumbWidth, int thumbHeight) {
        String name = src.getName();
        if (name.length() < 4)
            return null;
        FileImageOutputStream out = null;
        try {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter) iter.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0f);

            BufferedImage bi = ImageIO.read(src);

            final int srcWidth = bi.getWidth();
            final int srcHeight = bi.getHeight();

            int newWidth = srcWidth;
            int newHeight = srcHeight;
            int leftGab = 0;
            int topGab = 0;

            boolean isWidthLonger = true;
            if (srcHeight * (thumbWidth / (float) thumbHeight) < srcWidth) {
                newWidth = (int) (srcHeight * (thumbWidth / (float) thumbHeight));
                newHeight = srcHeight;
                isWidthLonger = true;
            } else {
                newWidth = srcWidth;
                newHeight = (int) (srcWidth * (thumbHeight / (float) thumbWidth));
                isWidthLonger = false;
            }

            if (isWidthLonger)
                leftGab = (srcWidth - newWidth) / 2;
            else topGab = (srcHeight - newHeight) / 2;

            BufferedImage imgRet = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            imgRet.getGraphics().drawImage(bi, 0, 0, thumbWidth, thumbHeight, leftGab, topGab, newWidth + leftGab, newHeight + topGab, null);


            String newFilePath = FilenameUtils.removeExtension(src.getCanonicalPath()) + "-thumb.jpg";
            File outFile = new File(newFilePath);
            out = new FileImageOutputStream(outFile);
            writer.setOutput(out);

            IIOImage img = new IIOImage(imgRet, null, null);
            writer.write(null, img, param);
            writer.dispose();

            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }

        return null;
    }
}
