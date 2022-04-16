package com.gomezrondon.exchangevef.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageService {

    public static void downloadImage(String url) {
        File theDir = new File("./images");
        if (!theDir.exists()){
            theDir.mkdirs();
        }

        try {
            URL imageUrl = new URL(url);
            BufferedImage image = ImageIO.read(imageUrl);
            ImageIO.write(image, "jpg", new File("./images/image.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getImageFromXPath(String url, String xpath) {
        String result = "";
        try {
            URL url1 = new URL(url);
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(url1, 10000);
            result = doc.selectXpath(xpath).text();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void scaleImage(String imageName, String newName) {
        BufferedImage img = getBufferedImage(imageName);
        int width = (int) (img.getWidth() * 1.5);
        int height = (int) (img.getHeight() * 1.5);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.getGraphics().drawImage(img, 0, 0, width, height, null);
        saveBufferdImage(newName, scaledImage);
    }

    //create a method that convert image to gray scale
    public static void toGrayScale(String imageName, String newName) {
        BufferedImage img = getBufferedImage(imageName);
        BufferedImage grayScaleImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayScaleImage.getGraphics().drawImage(img, 0, 0, null);
        saveBufferdImage(newName, grayScaleImage);
    }


    private static BufferedImage getBufferedImage(String imageName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    private static void saveBufferdImage(String newName, BufferedImage scaledImage) {
        try {
            ImageIO.write(scaledImage, "jpg", new File(newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
