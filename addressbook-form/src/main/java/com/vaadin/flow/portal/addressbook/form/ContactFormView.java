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

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
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
    private static final String ACTION_CREATE = "Create new";
    private static final String ACTION_SAVE = "Save";

    private PortletViewContext portletViewContext;

    private Binder<Contact> binder;
    private Contact contact;

    private Button action;
    private Button cancel;
    private Button remove;
    private Image image;

    private ContactService service;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.portletViewContext = context;
        context.addEventChangeListener("contact-selected",
                this::onContactSelected);
        context.addPortletModeChangeListener(this::handlePortletModeChange);
        init();
    }

    private void onContactSelected(PortletEvent event) {
        int contactId = Integer
                .parseInt(event.getParameters().get("contactId")[0]);
        Optional<Contact> contact = getService().findById(contactId);
        if (contact.isPresent()) {
            this.contact = contact.get();
            updateActionText();
            binder.readBean(this.contact);
            if (this.contact.getImage() != null) {
                image.setSrc(this.contact.getImage());
                image.setVisible(true);
            }
            remove.setVisible(true);
        } else {
            clear();
        }
    }

    private void handlePortletModeChange(PortletModeEvent event) {
        binder.setReadOnly(event.isViewMode());
        if (event.isViewMode()) {
            action.setText(ACTION_EDIT);
        } else {
            action.setText(ACTION_SAVE);
        }
    }

    private void fireUpdateEvent(Contact contact) {
        Map<String, String> param = Collections
                .singletonMap("contactId", contact.getId().toString());

        portletViewContext.fireEvent("contact-updated", param);
    }

    private PortletMode getPortletMode() {
        return portletViewContext.getPortletMode();
    }

    private void init() {
        FormLayout formLayout = populateFormLayout();
        setupButtons();

        HorizontalLayout actionButtons = new HorizontalLayout(action, cancel,
                remove);
        add(formLayout, actionButtons);
        setHorizontalComponentAlignment(Alignment.END, actionButtons);
    }

    private ContactService getService() {
        if (service == null) {
            service = new ContactService();
        }
        return service;
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

        DatePicker birthDate = new DatePicker();
        formLayout.addFormItem(birthDate, "Birth date");

        binder = new Binder<>(Contact.class);
        binder.bind(firstName, "firstName");
        binder.bind(lastName, "lastName");
        binder.bind(email, "email");
        binder.bind(phone, "phoneNumber");
        binder.bind(birthDate, "birthDate");

        // Set the state of form depending on portlet mode.
        binder.setReadOnly(PortletMode.VIEW.equals(getPortletMode()));

        image = new Image();
        image.setMaxHeight("72px");
        image.setMaxWidth("72px");
        image.setVisible(false);
        formLayout.add(image);
        return formLayout;
    }

    private void setupButtons() {
        action = new Button("action", event -> {
            if (PortletMode.EDIT.equals(getPortletMode())) {
                save();
            } else {
                portletViewContext.setPortletMode(PortletMode.EDIT);
            }
        });

        cancel = new Button("Cancel", event -> cancel());
        remove = new Button("Remove", event -> remove());
        remove.setVisible(false);
        updateActionText();
    }

    private void updateActionText() {
        action.setText(PortletMode.EDIT.equals(getPortletMode()) ?
                ACTION_SAVE :
                contact == null ? ACTION_CREATE : ACTION_EDIT);
    }

    private void clear() {
        contact = null;
        cancel();
    }

    private void cancel() {
        if (contact != null) {
            image.setSrc(contact.getImage());
            image.setVisible(true);
        }
        if (PortletMode.EDIT.equals(getPortletMode())) {
            binder.readBean(contact);
            portletViewContext.setPortletMode(PortletMode.VIEW);
        } else {
            contact = null;
            binder.removeBean();
            binder.getFields().forEach(HasValue::clear);
            image.setVisible(false);
            remove.setVisible(false);
        }
        updateActionText();
    }

    private void remove() {
        if (contact != null) {
            getService().remove(contact);
            contact = null;
            cancel();
            portletViewContext.setPortletMode(PortletMode.VIEW);
        }
        updateActionText();
    }

    private void save() {
        if (contact != null) {
            binder.writeBeanIfValid(contact);
            getService().save(contact);
        } else {
            contact = new Contact(getService().getNextId());
            binder.writeBeanIfValid(contact);
            getService().create(contact);
        }
        fireUpdateEvent(contact);
        updateActionText();

        portletViewContext.setPortletMode(PortletMode.VIEW);
    }

}
