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

import java.io.File;
import java.io.IOException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.Query;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Service for getting and storing contacts to a SQL DataBase.
 */
public class ContactService {

    private String dbFile;

    /**
     * Create a service instance. This will init a DataBase if one doesn't
     * exist.
     */
    public ContactService() {
        try {
            // This is for pluto as it seems to not find the driver class if not
            // explicitly loaded
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (dbFile == null) {
            createNewDatabase();

            int contacts = getContactsCount();

            if (contacts == 0) {
                UsersUtil.getRandomUsers(20, "contacts").ifPresent(result -> {
                    JsonArray results = result.getArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        Contact contact = new Contact(i + 1);
                        JsonObject json = results.getObject(i);
                        contact.setFirstName(
                                json.getObject("name").getString("first"));
                        contact.setLastName(
                                json.getObject("name").getString("last"));
                        contact.setBirthDate(LocalDateTime
                                .ofInstant(
                                        Instant.parse(json.getObject("dob")
                                                .getString("date")),
                                        ZoneId.of(ZoneOffset.UTC.getId()))
                                .toLocalDate());
                        contact.setEmail(json.getString("email"));
                        contact.setPhoneNumber(json.getString("phone"));
                        contact.setImage(
                                json.getObject("picture").getString("medium"));
                        create(contact);
                    }

                });
            }
        }
    }

    /**
     * Get the amount of contacts stored in the database.
     *
     * @return number of contacts in database
     */
    public int getContactsCount() {
        int contacts = 0;
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt
                    .executeQuery("SELECT COUNT(*) AS total FROM contacts");
            contacts = resultSet.getInt("total");
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error("Failed to get contacts",
                    e);
        }
        return contacts;
    }

    /**
     * Get the next available id for a new contact. This will be the max id
     * stored +1.
     *
     * @return next available contact id
     */
    public int getNextId() {
        int nextId = 0;
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt
                    .executeQuery("SELECT MAX(id) AS total FROM contacts");
            nextId = resultSet.getInt("total") + 1;
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass())
                    .error("Failed to get contacts max id", e);
        }
        return nextId;
    }

    /**
     * Get all contacts stored into the database.
     *
     * @return collection of contacts
     */
    public Collection<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM contacts;");
            while (resultSet.next()) {
                contacts.add(new Contact(resultSet));
            }
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error("Failed to get contacts",
                    e);
        }
        return contacts;
    }

    /**
     * Get a contact by id from the database.
     *
     * @param contactId
     *            id of contact to fetch
     * @return contact for id or empty if none found
     */
    public Optional<Contact> findById(int contactId) {
        Contact contact = null;
        String sql = "SELECT * FROM contacts WHERE id='" + contactId + "';";
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            contact = new Contact(resultSet);
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error("Failed to get contacts",
                    e);
        }
        return Optional.ofNullable(contact);
    }

    /**
     * Update contents for an existing contact.
     *
     * @param contact
     *            contact to update details for, not <code>null</code>
     */
    public void save(Contact contact) {
        Objects.requireNonNull(contact);
        String insert = "UPDATE contacts SET firstName = ?,lastName = ?,phoneNumber = ?,email = ?,birthDate = ?,imageUrl = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(insert)) {
            pstmt.setString(1, contact.getFirstName());
            pstmt.setString(2, contact.getLastName());
            pstmt.setString(3, contact.getPhoneNumber());
            pstmt.setString(4, contact.getEmail());
            pstmt.setString(5, contact.getBirthDate().toString());
            pstmt.setString(6, contact.getImage());
            pstmt.setInt(7, contact.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error(
                    "Failed to insert contact due to '{}'", e.getMessage(), e);
        }
    }

    /**
     * Create a new contact row into the database. Will throw an exception if
     * 'id' already exists in the database.
     *
     * @param contact
     *            contact to add to database, not <code>null</code>
     */
    public void create(Contact contact) {
        Objects.requireNonNull(contact);
        String insert = "INSERT INTO contacts(id,firstName,lastName,phoneNumber,email,birthDate,imageUrl) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(insert)) {
            pstmt.setInt(1, contact.getId());
            pstmt.setString(2, contact.getFirstName());
            pstmt.setString(3, contact.getLastName());
            pstmt.setString(4, contact.getPhoneNumber());
            pstmt.setString(5, contact.getEmail());
            pstmt.setString(6, contact.getBirthDate().toString());
            pstmt.setString(7, contact.getImage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error(
                    "Failed to insert contact due to '{}'", e.getMessage(), e);
        }
    }

    /**
     * Remove a contact row from the database.
     *
     * @param contact
     *            contact to remove, not <code>null</code>
     */
    public void remove(Contact contact) {
        Objects.requireNonNull(contact);
        String remove = "DELETE FROM contacts WHERE id = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(remove)) {
            pstmt.setInt(1, contact.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error(
                    "Failed to remove contact due to '{}'", e.getMessage(), e);
        }
    }

    /**
     * Create connection to database.
     *
     * @return connection to database
     */
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

    /**
     * Create a new database if no file exists. Add contacts table.
     */
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

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
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

    public Stream<Contact> getContacts(
            com.vaadin.flow.data.provider.Query<Contact, Void> query) {
        List<Contact> contacts = new ArrayList<>();
        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            String sql = String.format(
                    "SELECT * FROM contacts LIMIT %d OFFSET %d;",
                    query.getLimit(), query.getOffset());
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                contacts.add(new Contact(resultSet));
            }
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass()).error("Failed to get contacts",
                    e);
        }
        return contacts.stream();
    }

    public int getContactsCount(Query<Contact, Void> query) {
        return getContactsCount();
    }
}
