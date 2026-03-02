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
package com.vaadin.flow.portal.addressbook.grid;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import jakarta.portlet.WindowState;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.server.Version;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.addressbook.backend.PortletEventConstants;
import com.vaadin.flow.portal.lifecycle.PortletEvent;

/**
 * @author Vaadin Ltd
 */
public class ContactListView extends VerticalLayout implements PortletView {

    private GridLazyDataView<Contact> dataView;

    private Grid<Contact> grid = new Grid<>(Contact.class, false);
    private Column<Contact> emailColumn;
    private Column<Contact> birthDateColumn;
    private Button windowStateButton;

    private PortletViewContext portletViewContext;
    private ContactService service;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        portletViewContext = context;
        context.addEventChangeListener(PortletEventConstants.EVENT_CONTACT_UPDATED,
                this::onContactUpdated);
        context.addEventChangeListener(PortletEventConstants.EVENT_CONTACT_LIST_CHANGED,
                this::onContactsChanged);
        context.addWindowStateChangeListener(
                event -> handleWindowStateChanged(event.getWindowState()));
        init();
    }

    private void onContactUpdated(PortletEvent event) {
        int contactId = Integer
                .parseInt(event.getParameters().get(PortletEventConstants.KEY_CONTACT_ID)[0]);
        Optional<Contact> contact = getService().findById(contactId);
        contact.ifPresent(value -> dataView.refreshItem(value));
    }

    private void onContactsChanged(PortletEvent event) {
        dataView.refreshAll();
    }

    private void handleWindowStateChanged(WindowState windowState) {
        if (WindowState.MAXIMIZED.equals(windowState)) {
            emailColumn.setVisible(true);
            birthDateColumn.setVisible(true);
            grid.setMinWidth("700px");
            this.windowStateButton.setText("Normalize");
        } else if (WindowState.NORMAL.equals(windowState)) {
            emailColumn.setVisible(false);
            birthDateColumn.setVisible(false);
            grid.setMinWidth("450px");
            this.windowStateButton.setText("Maximize");
        }
    }

    private void fireSelectionEvent(
            ItemClickEvent<Contact> contactItemClickEvent) {
        Integer contactId = contactItemClickEvent.getItem().getId();

        Map<String, String> param = Collections.singletonMap(PortletEventConstants.KEY_CONTACT_ID,
                contactId.toString());

        portletViewContext.fireEvent(PortletEventConstants.EVENT_CONTACT_SELECTED, param);
    }

    private void init() {
        setWidthFull();

        grid.addColumn(Contact::getFirstName).setHeader("First name");
        grid.addColumn(Contact::getLastName).setHeader("Last name");
        grid.addColumn(Contact::getPhoneNumber).setHeader("Phone number");
        emailColumn = grid.addColumn(Contact::getEmail).setHeader("Email");
        birthDateColumn = grid.addColumn(Contact::getBirthDate).setHeader("Birth date");

        dataView = grid.setItems(
                query -> getService().getContacts(query),
                query -> getService().getContactsCount(query));

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(this::fireSelectionEvent);

        windowStateButton = new Button();
        windowStateButton.addClickListener(event -> switchWindowState());

        Button versionButton = new Button("Version", event ->
                Notification.show("Vaadin Flow " + Version.getFullVersion()));

        handleWindowStateChanged(getWindowState());

        HorizontalLayout toolbar = new HorizontalLayout(windowStateButton, versionButton);
        add(toolbar, grid);
        setHorizontalComponentAlignment(Alignment.END, toolbar);
    }

    private ContactService getService() {
        if (service == null) {
            service = new ContactService();
        }
        return service;
    }

    private void switchWindowState() {
        if (WindowState.NORMAL.equals(getWindowState())) {
            portletViewContext.setWindowState(WindowState.MAXIMIZED);
        } else if (WindowState.MAXIMIZED.equals(getWindowState())) {
            portletViewContext.setWindowState(WindowState.NORMAL);
        }
    }

    private WindowState getWindowState() {
        return portletViewContext.getWindowState();
    }
}
