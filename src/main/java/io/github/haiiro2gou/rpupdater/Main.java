package io.github.haiiro2gou.rpupdater;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Main
{
    public static void main(String[] args)
    {
        String propPath = "server.properties";

        try {
            String rpUrl = getPropertyValue(propPath, "resource-pack");
            String rpHash = calculateFileHash(downloadFile(new URL(rpUrl), "resources.zip"), "SHA-1");
        
            updatePropertyValue(propPath, "resource-pack-sha1", rpHash);
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getPropertyValue(String path, String key) throws IOException
    {
        Properties properties = new Properties();

        try(FileInputStream input = new FileInputStream(path))
        {
            properties.load(input);
            return properties.getProperty(key);
        }
    }

    private static void downloadFile(URL url, String path) throws IOException
    {
        try(InputStream input = url.openStream();
            ReadableByteChannel rbc = Channels.newChannel(input);
            FileOutputStream fos = new FileOutputStream(path))
        { fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE); }
    }

    private static String calculateFileHash(String path, String algorithm) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest hashAlgorithm = MessageDigest.getInstance(algorithm);

        try(DigestInputStream digestInput = new DigestInputStream(Files.newInputStream(path), hashAlgorithm))
        { while(digestInput.read() != -1) {} }

        byte[] hashBytes = hashAlgorithm.digest();
        StringBuilder hexBuilder = new StringBuilder();
        for(auto b : hashBytes) {
            hexBuilder.append(String.format("%02x", b));
        }

        return hexBuilder.toString();
    }

    private static void updatePropertyValue(String path, String key, String value) throws IOException
    {
        Properties properties = new Properties();

        try(FileInputStream input = new FileInputStream(path))
        {
            properties.load(input);
            properties.setProperty(key, value);
        }

        Files.writeString(Path.of(path), "");
        properties.store(Files.newOutputStream(Path.of(path), StandardOpenOption.CREATE), null);
    }
}