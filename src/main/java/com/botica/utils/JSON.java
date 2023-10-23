package com.botica.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;

public class JSON {

    private JSON() {
    }

    public static String readFileAsString(String file) throws JSONException {
        try{
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (Exception e) {
            throw new JSONException("Error reading file: " + file);
        }
    }
}
