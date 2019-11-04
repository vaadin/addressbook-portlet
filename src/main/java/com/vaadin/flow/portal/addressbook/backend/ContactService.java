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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class ContactService {
    private static Map<Integer, Contact> contacts = new HashMap<>();
    private static ContactService service;

    private ContactService() {
        if (contacts.isEmpty()) {
            getRandomUsers(20, "contacts").ifPresent(result -> {
                JsonArray results = result.getArray("results");
                for (int i = 0; i < results.length(); i++) {
                    Contact contact = new Contact(i);
                    JsonObject json = results.getObject(i);
                    contact.setFirstName(
                            json.getObject("name").getString("first"));
                    contact.setLastName(
                            json.getObject("name").getString("last"));
                    contact.setBirthDate(LocalDateTime.ofInstant(Instant.parse(
                            json.getObject("dob").getString("date")), ZoneId
                            .of(ZoneOffset.UTC.getId())).toLocalDate());
                    contact.setEmail(json.getString("email"));
                    contact.setPhoneNumber(json.getString("phone"));
                    try {
                        contact.setImage(new URL(json.getObject("picture")
                                .getString("medium")));
                    } catch (MalformedURLException e) {
                    }
                    contacts.put(i, contact);
                }
            });
        }
    }

    public static ContactService getInstance() {
        if(service == null) {
            service = new ContactService();
        }
        return service;
    }

    public Collection<Contact> getContacts() {
        return contacts.values();
    }

    public Optional<Contact> findById(int contactId) {
        return Optional.ofNullable(contacts.get(contactId));
    }

    private Optional<JsonObject> getRandomUsers(int num, String seed) {
        String url = "https://randomuser.me/api/?results=" + num + "&exc=login,location&nat=us&noinfo&seed=" + seed;
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
        contacts.put(contact.getId(), contact);
    }
}
