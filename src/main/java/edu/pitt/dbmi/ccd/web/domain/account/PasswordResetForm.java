/*
 * Copyright (C) 2017 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.domain.account;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Apr 20, 2017 6:03:58 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordResetForm {

    @NotBlank
    private String activationKey;

    @Length(min = 4, max = 10, message = "Please enter a password (4-10 chars).")
    private String password;

    @Length(min = 4, max = 10, message = "Please reenter the password.")
    private String confirmPassword;

    public PasswordResetForm() {
    }

    @Override
    public String toString() {
        return "PasswordResetForm{"
                + "activationKey=" + activationKey
                + ", password=" + password
                + ", confirmPassword=" + confirmPassword + '}';
    }

    public PasswordResetForm(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        if (!(this.password == null || this.password.equals(this.confirmPassword))) {
            this.confirmPassword = "";
        }
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        if (!(this.confirmPassword == null || this.confirmPassword.equals(this.password))) {
            this.password = "";
        }
    }

}