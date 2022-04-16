package com.gomezrondon.exchangevef.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageService {

    /***
     * Download an Image from a webpage and save it to a file
     * @param url where the image is hosted
     */
    public static void downloadImage(String url) {
        File theDir = new File("./images");
        if (!theDir.exists()){
            theDir.mkdirs();
        }

        BufferedImage image = null;
        try {
            URL imageUrl = new URL(url);
            image = ImageIO.read(imageUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        saveBufferdImage("./images/image.jpg", image);

    }

    /***
     * returns the value in specific location of a webpage.
     * @param url web page url
     * @param xpath location of the value
     * @return value
     */
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


    /***
     * Scale an image
     * @param imageName image name
     * @param newName name of the copy scaled.
     */
    public static void scaleImage(String imageName, String newName) {
        BufferedImage img = getBufferedImage(imageName);
        int width = (int) (img.getWidth() * 1.5);
        int height = (int) (img.getHeight() * 1.5);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaledImage.getGraphics().drawImage(img, 0, 0, width, height, null);
        saveBufferdImage(newName, scaledImage);
    }

    /***
     * convert an image to grayscale
     * @param imageName image name
     * @param newName name of the copy gray.
     */
    public static void toGrayScale(String imageName, String newName) {
        BufferedImage img = getBufferedImage(imageName);
        BufferedImage grayScaleImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayScaleImage.getGraphics().drawImage(img, 0, 0, null);
        saveBufferdImage(newName, grayScaleImage);
    }


    /***
     * convert a image to BufferedImage
     * @param imageName file name
     * @return BufferedImage of the file
     */
    private static BufferedImage getBufferedImage(String imageName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }


    /***
     * save a BufferedImage to file image
     * @param newName file name
     * @param img is BufferedImage to be saved to file as a jpg
     */
    private static void saveBufferdImage(String newName, BufferedImage img) {
        try {
            ImageIO.write(img, "jpg", new File(newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
