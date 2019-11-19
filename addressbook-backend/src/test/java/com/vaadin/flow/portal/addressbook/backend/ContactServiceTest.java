package com.vaadin.flow.portal.addressbook.backend;

import java.time.LocalDate;
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
        assertTrue("DB should init with users", byId.isPresent());
    }

    @Test
    public void saveContact_contactDetailsStored() {
        Optional<Contact> optionalUser = service.findById(4);
        assertTrue("User should exist in database", optionalUser.isPresent());

        Contact user = optionalUser.get();
        String firstName = user.getFirstName();
        user.setFirstName("my pal");

        service.save(user);

        assertEquals("my pal", service.findById(4).get().getFirstName());
    }

    @Test
    public void contactsCount_returnsCorrectCount() {
        assertEquals(20, service.getContactsCount());
    }

    @Test
    public void addRemoveMethods_addsAndRemovesContact() {
        int nextId = service.getNextId();

        assertEquals("DataBase should contain 20 items making the next id 21",
                21, nextId);

        Contact newContact = new Contact(nextId);
        newContact.setFirstName("Miriam");
        newContact.setLastName("Mirador");
        newContact.setBirthDate(LocalDate.of(1985, 1, 1));
        newContact.setEmail("me@boogle.bong");
        newContact.setPhoneNumber("112 332 15141");
        service.create(newContact);

        assertEquals("After addition 21 contacts should exist", 21,
                service.getContactsCount());
        Contact contact = service.findById(21).get();
        assertEquals("Read contact should match input", newContact, contact);
        assertEquals(
                "DataBase should contain 21 items making the next id 22", 22,
                service.getNextId());


        service.remove(contact);
        assertEquals("After removal 20 contacts should exist", 20,
                service.getContactsCount());
        assertEquals(
                "As highest contact was removed next id should again be 21", 21,
                service.getNextId());

    }
}
