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
package com.vaadin.flow.portal.addressbook.bundle;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Static Assets Provider Portlet
 * 
 * This portlet is included so that we can hide it in Liferay :),
 * and to make it easily removable if someone manages to put it 
 * on a page somehow.
 * 
 * @author Vaadin Ltd
 */
public class VaadinStaticBundlePortlet extends GenericPortlet {
    private static final String BUNDLE_PORTLET_ERROR_MESSAGE = "This portlet provides common Vaadin client side dependencies and should not be placed on portal pages.";

	@Override
    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.getWriter().println(BUNDLE_PORTLET_ERROR_MESSAGE);
    }
}
