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
import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.portal.addressbook.backend.Contact;
import com.vaadin.flow.portal.addressbook.backend.ContactService;
import com.vaadin.flow.portal.handler.PortletView;
import com.vaadin.flow.portal.handler.PortletViewContext;

/**
 * @author Vaadin Ltd
 */
public class GridView extends VerticalLayout implements PortletView {

    public static final String SELECTION = "selection";
    private ListDataProvider<Contact> dataProvider;

    private Grid<Contact> grid = new Grid<>(Contact.class);
    private Button windowStateButton;

    private PortletViewContext portletViewContext;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        portletViewContext = context;
        context.addWindowStateChangeListener(
                event -> handleWindowStateChanged(event.getWindowState()));
        init();
    }

    private void handleWindowStateChanged(WindowState windowState) {
        if (WindowState.MAXIMIZED.equals(windowState)) {
            this.windowStateButton.setText("Normalize");
            grid.setColumns("firstName", "lastName", "phoneNumber", "email",
                    "birthDate");
            grid.setMinWidth("700px");
        } else if (WindowState.NORMAL.equals(windowState)) {
            this.windowStateButton.setText("Maximize");
            grid.setColumns("firstName", "lastName", "phoneNumber");
            grid.setMinWidth("450px");
        }
    }

    private void init() {
        setWidthFull();

        dataProvider = new ListDataProvider<>(
                ContactService.getInstance().getContacts());

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

    private void fireSelectionEvent(
            ItemClickEvent<Contact> contactItemClickEvent) {
        Integer contactId = contactItemClickEvent.getItem().getId();

        Map<String, String> param = new HashMap<>();
        param.put("contactId", Integer.toString(contactId));

        portletViewContext.fireEvent("contact-selected", param);
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
