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

package com.vaadin.flow.portal.addressbook.backend;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.helger.commons.hashcode.HashCodeGenerator;

public class Contact implements Serializable {

    private final Integer id;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private LocalDate birthDate;
    private String image = "";

    public Contact(Integer id) {
        this.id = id;
    }

    public Contact(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        firstName = resultSet.getString("firstName");
        lastName = resultSet.getString("lastName");
        phoneNumber = resultSet.getString("phoneNumber");
        email = resultSet.getString("email");
        birthDate = LocalDate.parse(resultSet.getString("birthDate"));
        image = resultSet.getString("imageUrl");
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contact) {
            Contact other = (Contact) obj;
            return other.id == id && other.firstName.equals(firstName)
                    && other.lastName.equals(lastName) && other.phoneNumber
                    .equals(phoneNumber) && other.email.equals(email)
                    && other.birthDate.equals(birthDate);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return new HashCodeGenerator(Contact.class).append(id).append(firstName)
                .append(lastName).append(phoneNumber).append(email)
                .append(birthDate).getHashCode();
    }
}
