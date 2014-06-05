package sdf_manager;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

/**
 * General utility methods class.
 *
 * @author Jaanus
 */
public class Util {

    /**
     * Connect to the URL and check if it exists at the remote end. Local identifiers are removed (the part after the '#') before
     * connecting. The method returns true (i.e. URL is considered as "broken") if the given URL is malformed, or its
     * connection throws a {@link UnknownHostException} or sends a HTTP code that is 501 or 505 or anything in the range of 400
     * to 499. The latter range, however, is ignored if the given boolean input is true (meaning a client error is OK).
     *
     * @param urlStr the URL to check.
     * @param clientErrorOk If true, then a response code in the range of 400 to 499 is considered OK.
     * @return True/false as described above.
     */
    public static boolean isUrlBroken(String urlStr, boolean clientErrorOk) {

        int responseCode = -1;
        IOException ioe = null;
        URLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlStr, "#"));
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
        } catch (IOException e) {
            ioe = e;
        } finally {
            Util.disconnect(urlConnection);
        }

        System.out.println("Response code: " + responseCode);

        return ioe instanceof MalformedURLException || ioe instanceof UnknownHostException || ioe instanceof ConnectException
                || (!clientErrorOk && isClientError(responseCode)) || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                || responseCode == HttpURLConnection.HTTP_VERSION;
    }

    /**
     * Disconnect from given URL connection.
     *
     * @param urlConnection
     */
    public static void disconnect(URLConnection urlConnection) {

        if (urlConnection != null && urlConnection instanceof HttpURLConnection) {
            try {
                ((HttpURLConnection) urlConnection).disconnect();
            } catch (Exception e) {
                // Ignore deliberately.
            }
        }
    }

    /**
     * Check if given HTTP response code is a "client error".
     *
     * @param httpResponseCode The given HTTP response code.
     * @return True, if the given HTTP response code is a "client error", otherwise false.
     */
    private static boolean isClientError(int httpResponseCode) {
        return httpResponseCode >= 400 && httpResponseCode <= 499;
    }
}
