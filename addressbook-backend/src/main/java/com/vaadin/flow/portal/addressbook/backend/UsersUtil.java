package com.vaadin.flow.portal.addressbook.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import elemental.json.Json;
import elemental.json.JsonObject;

/**
 * Utility class for getting random user data.
 */
public final class UsersUtil {

    private UsersUtil() {
    }

    /**
     * Get user data from randomuser.me to use as dummy data in application.
     * Return result will look like:
     *
     * "results": [{
     *   "gender",
     *   "name": {
     *       "title",
     *       "first",
     *       "last"
     *   },
     *   "email"
     *   "dob": {
     *       "date",
     *       "age"
     *   },
     *   "registered": {
     *       "date",
     *       "age"
     *   }
     *   "phone",
     *   "cell",
     *   "id": {
     *       "name",
     *       "value"
     *   }
     *   "picture": {
     *       "large",
     *       "medium",
     *       "thumbnail"
     *   },
     *   "nat"
     * }]
     *
     * @param num
     *         number of users to get
     * @param seed
     *         seed string to use
     * @return optional JsonObject containing user data if successful
     */
    public static Optional<JsonObject> getRandomUsers(int num, String seed) {
        String url = "https://randomuser.me/api/?results=" + num
                + "&exc=login,location&noinfo&seed=" + seed;
        HttpURLConnection con = null;
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                return Optional.empty();
            }

            StringBuffer response = new StringBuffer();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return Optional.of(Json.parse(response.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
