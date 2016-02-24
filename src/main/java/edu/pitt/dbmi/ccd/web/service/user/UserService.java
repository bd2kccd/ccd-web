/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.service.user;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.entity.UserLoginAttempt;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import edu.pitt.dbmi.ccd.web.util.UrlUtility;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.mail.MessagingException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Feb 23, 2016 5:13:11 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final String LOCAL_FOLDER = "local";

    private final CcdProperties ccdProperties;

    private final DefaultPasswordService passwordService;

    private final MailService mailService;

    private final UserAccountService userAccountService;

    @Autowired
    public UserService(
            CcdProperties ccdProperties,
            DefaultPasswordService passwordService,
            MailService mailService,
            UserAccountService userAccountService) {
        this.ccdProperties = ccdProperties;
        this.passwordService = passwordService;
        this.mailService = mailService;
        this.userAccountService = userAccountService;
    }

    public boolean registerNewUser(
            final UserRegistration userRegistration,
            final String userIPAddress) {
        boolean success = false;

        String account = UUID.randomUUID().toString();
        Path userDir = Paths.get(ccdProperties.getWorkspaceDir(), LOCAL_FOLDER, account.replace("-", "_"));

        String username = userRegistration.getUsername();
        String password = userRegistration.getPassword();
        String email = userRegistration.getUsername();
        String workspString = userDir.toAbsolutePath().toString();

        try {
            Files.createDirectories(userDir);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
            return false;
        }

        Person person = new Person();
        person.setFirstName("");
        person.setLastName("");
        person.setEmail(email);
        person.setWorkspace(workspString);

        UserAccount userAccount = new UserAccount();
        userAccount.setAccount(account);
        userAccount.setActive(false);
        userAccount.setDisabled(false);
        userAccount.setPassword(passwordService.encryptPassword(password));
        userAccount.setPerson(person);
        userAccount.setRegistrationDate(new Date(System.currentTimeMillis()));
        try {
            userAccount.setRegistrationLocation(UrlUtility.InetNTOA(userIPAddress));
        } catch (UnknownHostException exception) {
            LOGGER.error(exception.getLocalizedMessage());
        }
        userAccount.setUserLogin(new UserLogin());
        userAccount.setUserLoginAttempt(new UserLoginAttempt());
        userAccount.setUsername(username);

        try {
            success = userAccountService.saveUserAccount(userAccount) != null;
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        if (success) {
            Thread t = new Thread(() -> {
                try {
                    String activationLink = UriComponentsBuilder.fromHttpUrl(ccdProperties.getServerURL()).pathSegment("user", "registration", "activate")
                            .queryParam("account", Base64.getUrlEncoder().encodeToString(account.getBytes()))
                            .build().toString();
                    mailService.sendUserActivationLink(email, activationLink);
                    mailService.sendNewUserAlert(email, userAccount.getRegistrationDate(), userIPAddress);
                } catch (MessagingException exception) {
                    LOGGER.warn(String.format("Unable to send registration email for user '%s'.", username), exception);
                }
            });
            t.start();
        }

        return success;
    }

}