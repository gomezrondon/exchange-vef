package com.gomezrondon.exchangevef.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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


    public static void writeToFile(String fileName, String data) {
        Path path = Paths.get(fileName);
        byte[] strToBytes = data.getBytes();

        try {
            System.out.println("Saving data to file: "+path.getFileName());
            Files.write(path, strToBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String fileName) {
        Path path = Paths.get(fileName);
        String data = "";
        if (path.toFile().exists()) {
            try {
                data= Files.readString(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return data.trim();
    }


}
