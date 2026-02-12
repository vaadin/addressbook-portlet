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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

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
                        StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);

                ArrayNode contacts;
                if (root.isArray()) {
                    contacts = (ArrayNode) root;
                } else {
                    contacts = mapper.createArrayNode();
                    contacts.add(root);
                }

                int count = 0;
                for (JsonNode obj : contacts) {
                    Contact c = new Contact(getService().getNextId());
                    c.setFirstName(obj.get("firstName").asText());
                    c.setLastName(obj.get("lastName").asText());
                    c.setPhoneNumber(obj.get("phoneNumber").asText());
                    c.setEmail(obj.get("email").asText());
                    c.setBirthDate(LocalDate.parse(obj.get("birthDate").asText()));
                    if (obj.has("image")) {
                        c.setImage(obj.get("image").asText());
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
