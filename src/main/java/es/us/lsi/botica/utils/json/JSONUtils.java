package es.us.lsi.botica.utils.json;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;

public class JSONUtils {

    private JSONUtils() {
    }

    /**
     * Reads the content of a JSON file and returns it as a string.
     *
     * @param file The path to the JSON file to read.
     * @return The content of the JSON file as a string.
     * @throws JSONException If there's an error reading the file.
     */
    public static String readFileAsString(String file) throws JSONException {
        try{
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (Exception e) {
            throw new JSONException("Error reading file: " + file);
        }
    }
}
