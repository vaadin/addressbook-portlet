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
import javax.portlet.WindowState;
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
import com.vaadin.flow.portal.handler.WindowStateEvent;

/**
 * @author Vaadin Ltd
 */
public class FormView extends VerticalLayout implements PortletView {

    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_SAVE = "Save";
    public static final String WINDOW_MAXIMIZE = "Maximize";
    public static final String WINDOW_NORMALIZE = "Normalize";

    private Binder<Contact> binder;
    private Button action;
    private Button cancel;
    private Button windowState;
    private TextField firstName;
    private Image image;

    private PortletViewContext portletViewContext;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.portletViewContext = context;
        context.addEventChangeListener("contact-selected", this::handleEvent);
        context.addPortletModeChangeListener(this::handlePortletModeChange);
        context.addWindowStateChangeListener(this::handleWindowStateChange);
        init();
    }

    private void init() {
        FormLayout formLayout = populateFormLayout();
        setupButtons();

        HorizontalLayout actionButtons = new HorizontalLayout(action, cancel);
        add(windowState, formLayout, actionButtons);
        setHorizontalComponentAlignment(Alignment.END, windowState,
                actionButtons);
    }

    private PortletMode getPortletMode() {
        return portletViewContext.getPortletMode();
    }

    private WindowState getWindowState() {
        return portletViewContext.getWindowState();
    }

    private FormLayout populateFormLayout() {
        FormLayout formLayout = new FormLayout();
        firstName = new TextField();
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
                    } else {
                        portletViewContext.setPortletMode(PortletMode.EDIT);
                    }
                });

        cancel = new Button("Cancel", event -> cancel());

        windowState = new Button(
                WindowState.NORMAL.equals(getWindowState()) ? WINDOW_MAXIMIZE
                        : WINDOW_NORMALIZE,
                event -> switchWindowState());
    }

    private void switchWindowState() {
        if (WindowState.NORMAL.equals(getWindowState())) {
            portletViewContext.setWindowState(WindowState.MAXIMIZED);
        } else if (WindowState.MAXIMIZED.equals(getWindowState())) {
            portletViewContext.setWindowState(WindowState.NORMAL);
        }
    }

    private void handleEvent(PortletEvent event) {
        Integer contactId = Integer
                .parseInt(event.getParameters().get("contactId")[0]);
        Optional<Contact> contact = ContactService.getInstance()
                .findById(contactId);
        if (contact.isPresent()) {
            binder.setBean(contact.get());
            image.setSrc(contact.get().getImage().toString());
        } else {
            cancel();
        }
    }

    private void cancel() {
        if (binder.getBean() != null) {
            portletViewContext.setPortletMode(PortletMode.VIEW);
        }
    }

    private void save() {
        Contact contact = binder.getBean();

        if (contact != null) {
            ContactService.getInstance().save(contact);
        }

        portletViewContext.setPortletMode(PortletMode.VIEW);
    }

    private void handleWindowStateChange(WindowStateEvent event) {
        if (WindowState.MAXIMIZED.equals(event.getWindowState())) {
            windowState.setText(WINDOW_NORMALIZE);
        } else if (WindowState.NORMAL.equals(event.getWindowState())) {
            windowState.setText(WINDOW_MAXIMIZE);
        }
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
