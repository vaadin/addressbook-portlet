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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.WindowState;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.portal.handler.EventHandler;
import com.vaadin.flow.portal.handler.PortletEvent;

/**
 * @author Vaadin Ltd
 */
public class FormPortlet extends VaadinPortlet<FormView> {

    public static final String TAG = "form-portlet";
    private Component portletView;

    public static FormPortlet getCurrent() {
        return (FormPortlet) VaadinPortlet.getCurrent();
    }

    public void setPortletView(Component portletView) {
        this.portletView = portletView;
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {
        if (request.getActionParameters().getNames()
                .contains("selection")) {
            if (portletView != null && portletView instanceof EventHandler) {
                ((EventHandler) portletView).handleEvent(new PortletEvent("selection", request.getParameterMap()));
            }
            if (request.getActionParameters().getValue("windowState") != null) {
                response.setWindowState(new WindowState(
                        request.getRenderParameters().getValue("windowState")));
            }
        }
    }
}
