/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.portal.addressbook.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class ContactService {
    //    private static Map<Integer, Contact> contacts = new HashMap<>();
    //    private static final ContactService INSTANCE = new ContactService();
    private String dbFile;

    public ContactService() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //        synchronized (dbFile) {
        if (dbFile == null) {
            createNewDatabase();

            int contacts = 0;
            try (Connection conn = connect(); Statement stmt = conn
                    .createStatement()) {
                ResultSet resultSet = stmt
                        .executeQuery("SELECT COUNT(*) AS total FROM contacts");
                contacts = resultSet.getInt("total");
            } catch (SQLException e) {
                LoggerFactory.getLogger(getClass())
                        .error("Failed to get contacts", e);
            }

            if (contacts == 0) {
                getRandomUsers(20, "contacts").ifPresent(result -> {
                    JsonArray results = result.getArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        Contact contact = new Contact(i);
                        JsonObject json = results.getObject(i);
                        contact.setFirstName(
                                json.getObject("name").getString("first"));
                        contact.setLastName(
                                json.getObject("name").getString("last"));
                        contact.setBirthDate(LocalDateTime.ofInstant(
                                Instant.parse(json.getObject("dob")
                                        .getString("date")),
                                ZoneId.of(ZoneOffset.UTC.getId()))
                                .toLocalDate());
                        contact.setEmail(json.getString("email"));
                        contact.setPhoneNumber(json.getString("phone"));
                        contact.setImage("https://randomuser.me/"+
                                json.getObject("picture").getString("medium"));
                        save(contact);
                    }

                });
            }
        }
        //        }
    }

    private Connection connect() {

        String url = "jdbc:sqlite:" + dbFile;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                    .error("Failed to connect to DB '{}'", e.getMessage());
        }
        return conn;
    }

    //    public static ContactService getInstance() {
    //        return INSTANCE;
    //    }

    public Collection<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn
                .createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM contacts;");
            while (resultSet.next()) {
                contacts.add(new Contact(resultSet));
            }
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                    .error("Failed to get contacts", e);
        }
        return contacts;
    }

    public Optional<Contact> findById(int contactId) {
        Contact contact = null;
        String sql = "SELECT * FROM contacts WHERE id='" + contactId + "';";
        try (Connection conn = connect(); Statement stmt = conn
                .createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            contact = new Contact(resultSet);
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                    .error("Failed to get contacts", e);
        }
        return Optional.ofNullable(contact);
    }

    private Optional<JsonObject> getRandomUsers(int num, String seed) {
        String url = "https://randomuser.me/api/?results=" + num
                + "&exc=login,location&nat=us&noinfo&seed=" + seed;
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

    public void save(Contact contact) {
        String insert = "INSERT INTO contacts(id,firstName,lastName,phoneNumber,email,birthDate,imageUrl) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn
                .prepareStatement(insert)) {
            pstmt.setInt(1, contact.getId());
            pstmt.setString(2, contact.getFirstName());
            pstmt.setString(3, contact.getLastName());
            pstmt.setString(4, contact.getPhoneNumber());
            pstmt.setString(5, contact.getEmail());
            pstmt.setString(6, contact.getBirthDate().toString());
            pstmt.setString(7, contact.getImage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                    .error("Failed to insert contact due to '{}'",
                            e.getMessage(), e);
        }
    }

    private void createNewDatabase() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File database = new File(tempDir, "vaadin-portal.db");
        database.deleteOnExit();
        tempDir.deleteOnExit();
        if (database.exists()) {
            dbFile = database.toString();
        } else {
            try {
                dbFile = Files.createFile(database.toPath()).toString();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to create a database file.", e);
            }
        }

        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager
                .getConnection(url); Statement stmt = conn.createStatement()) {
            StringBuilder initScript = new StringBuilder();
            initScript.append("CREATE TABLE IF NOT EXISTS ")
                    .append("contacts (");
            initScript.append("id integer PRIMARY KEY,");
            initScript.append("firstName text,");
            initScript.append("lastName text,");
            initScript.append("phoneNumber text,");
            initScript.append("email text,");
            initScript.append("birthDate text,");
            initScript.append("imageUrl text");
            initScript.append(");");
            stmt.execute(initScript.toString());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to init database.", e);
        }
    }
}
