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
package com.vaadin.flow.portal.addressbook.form;

import javax.portlet.PortletMode;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletView;
import com.vaadin.flow.portal.handler.PortletViewContext;

/**
 * @author Vaadin Ltd
 */
public class ContactFormView extends VerticalLayout implements PortletView {

    private static final String ACTION_EDIT = "Edit";
    private static final String ACTION_SAVE = "Save";

    private Binder<Contact> binder;
    private Image image;
    private Contact contact;
    private Button action;
    private Button cancel;

    private PortletViewContext portletViewContext;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.portletViewContext = context;
        context.addEventChangeListener("contact-selected",
                this::onContactSelected);
        context.addPortletModeChangeListener(this::handlePortletModeChange);
        init();
    }

    private void init() {
        FormLayout formLayout = populateFormLayout();
        setupButtons();

        HorizontalLayout actionButtons = new HorizontalLayout(action, cancel);
        add(formLayout, actionButtons);
        setHorizontalComponentAlignment(Alignment.END, actionButtons);
    }

    private PortletMode getPortletMode() {
        return portletViewContext.getPortletMode();
    }

    private FormLayout populateFormLayout() {
        FormLayout formLayout = new FormLayout();
        TextField firstName = new TextField();
        formLayout.addFormItem(firstName, "First name");

        TextField lastName = new TextField();
        formLayout.addFormItem(lastName, "Last name");

        TextField phone = new TextField();
        formLayout.addFormItem(phone, "Phone number");

        EmailField email = new EmailField();
        formLayout.addFormItem(email, "Email");

        binder = new Binder<>(Contact.class);
        binder.bind(firstName, "firstName");
        binder.bind(lastName, "lastName");
        binder.bind(email, "email");
        binder.bind(phone, "phoneNumber");
        // Set the state of form depending on portlet mode.
        binder.setReadOnly(PortletMode.VIEW.equals(getPortletMode()));

        image = new Image();
        image.setMaxHeight("72px");
        image.setMaxWidth("72px");
        formLayout.add(image);
        return formLayout;
    }

    private void setupButtons() {
        action = new Button(
                PortletMode.EDIT.equals(getPortletMode()) ? ACTION_SAVE
                        : ACTION_EDIT,
                event -> {
                    if (PortletMode.EDIT.equals(getPortletMode())) {
                        save();
                    } else if (contact != null) {
                        portletViewContext.setPortletMode(PortletMode.EDIT);
                    }
                });

        cancel = new Button("Cancel", event -> cancel());
    }

    private void onContactSelected(PortletEvent event) {
        int contactId = Integer
                .parseInt(event.getParameters().get("contactId")[0]);
        Optional<Contact> contact = ContactService.getInstance()
                .findById(contactId);
        if (contact.isPresent()) {
            this.contact = contact.get();
            binder.readBean(this.contact);
            image.setSrc(this.contact.getImage().toString());
        } else {
            clear();
        }
    }

    private void clear() {
        contact = null;
        cancel();
    }

    private void cancel() {
        binder.readBean(contact);
        image.setSrc(contact != null ? contact.getImage().toString() : "");
        portletViewContext.setPortletMode(PortletMode.VIEW);
    }

    private void save() {
        if (contact != null) {
            binder.writeBeanIfValid(contact);
            ContactService.getInstance().save(contact);
            fireUpdateEvent(contact);
        }

        portletViewContext.setPortletMode(PortletMode.VIEW);
    }

    private void fireUpdateEvent(Contact contact) {
        Map<String, String> param = Collections.singletonMap("contactId",
                contact.getId().toString());

        portletViewContext.fireEvent("contact-updated", param);
    }

    private void handlePortletModeChange(PortletModeEvent event) {
        binder.setReadOnly(PortletMode.VIEW.equals(event.getPortletMode()));
        if (event.isEditMode()) {
            action.setText(ACTION_SAVE);
        } else {
            action.setText(ACTION_EDIT);
        }
    }
}
