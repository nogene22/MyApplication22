
package com.example.myapplication22.utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


// get request
public class NetworkUtils {
    public static String makeHTTPRequest(URL url) throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();


        try{
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput)
                return scanner.next();
            else return null;
            }
            finally{
                connection.disconnect();
        }
    }
}
