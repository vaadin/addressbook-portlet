package com.vaadin.flow.portal.addressbook.backend;

import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ContactServiceTest {
    ContactService service;

    @Before
    public void init() {
        service = new ContactService();
    }

    @Test
    public void getServiceInstance_populatesContacts() {
        Collection<Contact> contacts = service.getContacts();

        assertEquals(20, contacts.size());
    }

    @Test
    public void getContactById_contactIsReturned() {
        Optional<Contact> byId = service.findById(1);
        assertTrue(byId.isPresent());
    }
}