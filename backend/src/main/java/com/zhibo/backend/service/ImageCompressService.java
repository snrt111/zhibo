package com.zhibo.backend.service;

import com.zhibo.backend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageCompressService {

    private static final Logger logger = LoggerFactory.getLogger(ImageCompressService.class);

    private static final int DEFAULT_MAX_WIDTH = 1920;
    private static final int DEFAULT_MAX_HEIGHT = 1920;
    private static final int AVATAR_MAX_WIDTH = 512;
    private static final int AVATAR_MAX_HEIGHT = 512;
    private static final float DEFAULT_QUALITY = 0.8f;
    private static final float AVATAR_QUALITY = 0.7f;

    public byte[] compressImage(MultipartFile file) {
        return compressImage(file, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }

    public byte[] compressAvatar(MultipartFile file) {
        return compressImage(file, AVATAR_MAX_WIDTH, AVATAR_MAX_HEIGHT, AVATAR_QUALITY);
    }

    public byte[] compressImage(MultipartFile file, int maxWidth, int maxHeight, float quality) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                logger.warn("Cannot read image, return original file");
                return file.getBytes();
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                logger.debug("Image size {}x{} is within limits, no compression needed", originalWidth, originalHeight);
                return file.getBytes();
            }

            Dimension newSize = calculateNewDimension(originalWidth, originalHeight, maxWidth, maxHeight);
            logger.info("Compressing image from {}x{} to {}x{}", originalWidth, originalHeight, newSize.width, newSize.height);

            BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(originalImage, 0, 0, newSize.width, newSize.height, null);
            g.dispose();

            String formatName = getFormatName(file.getOriginalFilename());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if ("jpg".equalsIgnoreCase(formatName) || "jpeg".equalsIgnoreCase(formatName)) {
                javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
                writer.setOutput(ios);
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
                writer.dispose();
                ios.close();
            } else {
                ImageIO.write(resizedImage, formatName, outputStream);
            }

            byte[] compressedBytes = outputStream.toByteArray();
            logger.info("Image compressed from {} bytes to {} bytes", file.getSize(), compressedBytes.length);
            return compressedBytes;

        } catch (IOException e) {
            logger.error("Failed to compress image", e);
            throw new BusinessException("图片压缩失败");
        }
    }

    private Dimension calculateNewDimension(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return new Dimension(newWidth, newHeight);
    }

    private String getFormatName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return "jpg";
        }
        String ext = filename.substring(lastDot + 1).toLowerCase();
        return "webp".equals(ext) ? "png" : ext;
    }
}
