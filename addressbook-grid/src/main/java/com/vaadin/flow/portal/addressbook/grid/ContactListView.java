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

import javax.portlet.WindowState;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.lifecycle.PortletEvent;

import static com.vaadin.flow.portal.addressbook.backend.PortletEventConstants.EVENT_CONTACT_SELECTED;
import static com.vaadin.flow.portal.addressbook.backend.PortletEventConstants.EVENT_CONTACT_UPDATED;
import static com.vaadin.flow.portal.addressbook.backend.PortletEventConstants.KEY_CONTACT_ID;

/**
 * @author Vaadin Ltd
 */
public class ContactListView extends VerticalLayout implements PortletView {

    private DataProvider<Contact, Void> dataProvider;

    private Grid<Contact> grid = new Grid<>(Contact.class);
    private Button windowStateButton;

    private PortletViewContext portletViewContext;
    private ContactService service;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        portletViewContext = context;
        context.addEventChangeListener(EVENT_CONTACT_UPDATED,
                this::onContactUpdated);
        context.addWindowStateChangeListener(
                event -> handleWindowStateChanged(event.getWindowState()));
        init();
    }

    private void onContactUpdated(PortletEvent event) {
        int contactId = Integer
                .parseInt(event.getParameters().get(KEY_CONTACT_ID)[0]);
        Optional<Contact> contact = getService().findById(contactId);
        contact.ifPresent(value -> dataProvider.refreshItem(value));
    }

    private void handleWindowStateChanged(WindowState windowState) {
        if (WindowState.MAXIMIZED.equals(windowState)) {
            grid.setColumns("firstName", "lastName", "phoneNumber", "email",
                    "birthDate");
            grid.setMinWidth("700px");
            this.windowStateButton.setText("Normalize");
        } else if (WindowState.NORMAL.equals(windowState)) {
            grid.setColumns("firstName", "lastName", "phoneNumber");
            grid.setMinWidth("450px");
            this.windowStateButton.setText("Maximize");
        }
    }

    private void fireSelectionEvent(
            ItemClickEvent<Contact> contactItemClickEvent) {
        Integer contactId = contactItemClickEvent.getItem().getId();

        Map<String, String> param = Collections.singletonMap(KEY_CONTACT_ID,
                contactId.toString());

        portletViewContext.fireEvent(EVENT_CONTACT_SELECTED, param);
    }

    private void init() {
        setWidthFull();

        dataProvider = new CallbackDataProvider<Contact, Void>(
                getService()::getContacts, getService()::getContactsCount,
                Contact::getId);

        grid.setDataProvider(dataProvider);
        grid.removeColumnByKey("id");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(this::fireSelectionEvent);

        windowStateButton = new Button();
        windowStateButton.addClickListener(event -> switchWindowState());

        handleWindowStateChanged(getWindowState());

        add(windowStateButton, grid);
        setHorizontalComponentAlignment(Alignment.END, windowStateButton);
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
