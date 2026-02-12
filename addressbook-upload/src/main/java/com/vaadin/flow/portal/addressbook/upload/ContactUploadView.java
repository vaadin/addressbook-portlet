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
package com.vaadin.flow.portal.addressbook.upload;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.addressbook.backend.PortletEventConstants;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class ContactUploadView extends VerticalLayout implements PortletView {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactUploadView.class);

    private PortletViewContext portletViewContext;
    private ContactService service;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.portletViewContext = context;
        Upload upload = createJsonUpload();
        add(upload);
    }

    private ContactService getService() {
        if (service == null) {
            service = new ContactService();
        }
        return service;
    }

    private void fireListChangedEvent() {
        portletViewContext.fireEvent(PortletEventConstants.EVENT_CONTACT_LIST_CHANGED,
            Collections.emptyMap());
    }

    private Upload createJsonUpload() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.addSucceededListener(event -> {
            try {
                String json = new String(
                        buffer.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8).trim();

                // Json.parse() returns JsonObject and throws
                // ClassCastException on top-level arrays, so
                // detect arrays and wrap them for parsing.
                JsonArray contacts;
                if (json.startsWith("[")) {
                    contacts = Json.parse("{\"items\":" + json + "}")
                            .getArray("items");
                } else {
                    contacts = Json.createArray();
                    contacts.set(0, Json.parse(json));
                }

                int count = 0;
                for (int i = 0; i < contacts.length(); i++) {
                    JsonObject obj = contacts.getObject(i);
                    Contact c = new Contact(getService().getNextId());
                    c.setFirstName(obj.getString("firstName"));
                    c.setLastName(obj.getString("lastName"));
                    c.setPhoneNumber(obj.getString("phoneNumber"));
                    c.setEmail(obj.getString("email"));
                    c.setBirthDate(LocalDate.parse(obj.getString("birthDate")));
                    if (obj.hasKey("image")) {
                        c.setImage(obj.getString("image"));
                    }
                    getService().create(c);
                    count++;
                }

                Notification.show("Imported " + count + " contact(s)");
                upload.clearFileList();
                fireListChangedEvent();
            } catch (Exception ex) {
                LOG.error("Failed to import contacts", ex);
                Notification.show(
                        "Failed to import contacts: " + ex.getMessage());
            }
        });
        upload.setAcceptedFileTypes(".json", "application/json");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Upload JSON contacts"));
        return upload;
    }
}
