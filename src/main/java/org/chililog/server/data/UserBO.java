//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.chililog.server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.CryptoUtils;

import com.mongodb.DBObject;

/**
 * <p>
 * The User Business Object encapsulate user details used for authentication and authorization:
 * <ul>
 * <li>Username</li>
 * <li>Password</li>
 * <li>Roles</li>
 * </ul>
 * </p>
 * 
 * @author vibul
 * 
 */
public class UserBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String SYSTEM_ADMINISTRATOR_ROLE_NAME = "system.administrator";

    public static final String REPOSITORY_ROLE_PREFIX = "repo.";

    public static final String REPOSITORY_ADMINISTRATOR_ROLE_SUFFIX = ".administrator";
    public static final String REPOSITORY_ADMINISTRATOR_ROLE_TEMPLATE = REPOSITORY_ROLE_PREFIX + "%s"
            + REPOSITORY_ADMINISTRATOR_ROLE_SUFFIX;

    public static final String REPOSITORY_WORKBENCH_ROLE_SUFFIX = ".workbench";
    public static final String REPOSITORY_WORKBENCH_ROLE_TEMPLATE = REPOSITORY_ROLE_PREFIX + "%s"
            + REPOSITORY_WORKBENCH_ROLE_SUFFIX;

    public static final String REPOSITORY_PUBLISHER_ROLE_SUFFIX = ".publisher";
    public static final String REPOSITORY_PUBLISHER_ROLE_TEMPLATE = REPOSITORY_ROLE_PREFIX + "%s"
            + REPOSITORY_PUBLISHER_ROLE_SUFFIX;

    public static final String REPOSITORY_SUBSCRIBER_ROLE_SUFFIX = ".subscriber";
    public static final String REPOSITORY_SUBSCRIBER_ROLE_TEMPLATE = REPOSITORY_ROLE_PREFIX + "%s"
            + REPOSITORY_SUBSCRIBER_ROLE_SUFFIX;

    // Pattern thanks to http://www.regular-expressions.info/email.html
    private static Pattern _emailAddressPattern = Pattern
            .compile("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$");

    private String _username;
    private String _password;
    private ArrayList<String> _roles = new ArrayList<String>();
    private Status _status = Status.ENABLED;
    private String _displayName;
    private String _emailAddress;

    static final String USERNAME_FIELD_NAME = "username";
    static final String PASSWORD_FIELD_NAME = "password";
    static final String ROLES_FIELD_NAME = "roles";
    static final String STATUS_FIELD_NAME = "status";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";
    static final String EMAIL_ADDRESS_FIELD_NAME = "email_address";

    /**
     * Basic constructor
     */
    public UserBO()
    {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    public UserBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _username = MongoUtils.getString(dbObject, USERNAME_FIELD_NAME, true);
        _password = MongoUtils.getString(dbObject, PASSWORD_FIELD_NAME, false);
        _roles = MongoUtils.getStringArrayList(dbObject, ROLES_FIELD_NAME, false);
        _status = Status.valueOf(MongoUtils.getString(dbObject, STATUS_FIELD_NAME, true));
        _displayName = MongoUtils.getString(dbObject, DISPLAY_NAME_FIELD_NAME, false);
        _emailAddress = MongoUtils.getString(dbObject, EMAIL_ADDRESS_FIELD_NAME, false);
        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     * @throws ChiliLogException
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        MongoUtils.setString(dbObject, USERNAME_FIELD_NAME, _username, true);
        MongoUtils.setString(dbObject, PASSWORD_FIELD_NAME, _password, true);
        MongoUtils.setStringArrayList(dbObject, ROLES_FIELD_NAME, _roles, false);
        MongoUtils.setString(dbObject, STATUS_FIELD_NAME, _status.toString(), true);
        MongoUtils.setString(dbObject, DISPLAY_NAME_FIELD_NAME, _displayName, false);
        MongoUtils.setString(dbObject, EMAIL_ADDRESS_FIELD_NAME, _emailAddress, false);

        if (!StringUtils.isBlank(_emailAddress) && !_emailAddressPattern.matcher(_emailAddress).matches())
        {
            throw new ChiliLogException(Strings.USER_EMAIL_ADDRESS_FORMAT_ERROR, _emailAddress);
        }
    }

    /**
     * Returns the username (or unique code) for this user
     */
    public String getUsername()
    {
        return _username;
    }

    /**
     * Assigns a unique code to this user
     * 
     * @param username
     */
    public void setUsername(String username)
    {
        _username = username;
    }

    /**
     * Returns a hashed password
     */
    public String getPassword()
    {
        return _password;
    }

    /**
     * Assigns a password to this user. If plain text, then it is hashed before saving
     * 
     * @param password
     *            Password
     * @param isPlainText
     *            indicates if the password is plain text or hashed.
     * @throws ChiliLogException
     *             if there is an error during hashing
     */
    public void setPassword(String password, boolean isPlainText) throws ChiliLogException
    {
        if (isPlainText)
        {
            _password = CryptoUtils.createSHA512Hash(password, null);
        }
        else
        {
            _password = password;
        }
    }

    /**
     * Validates the plain text password against the hash password stored with this user record
     * 
     * @param plainTextPassword
     *            the plain text password to validate
     * @return true if <code>plainTextPassword</code> matches the stored hashed password, false if it does not match.
     * @throws ChiliLogException
     *             if there was an error during password verification
     */
    public boolean validatePassword(String plainTextPassword) throws ChiliLogException
    {
        return CryptoUtils.verifyHash(plainTextPassword, _password);
    }

    /**
     * Add this user to the role
     * 
     * @param role
     *            name of role
     */
    public void addRole(String role)
    {
        if (!hasRole(role))
        {
            _roles.add(role);
        }
    }

    /**
     * Removes the user from the role
     * 
     * @param role
     */
    public void removeRole(String role)
    {
        _roles.remove(role);
    }

    /**
     * Removes the user from all roles
     */
    public void removeAllRoles()
    {
        _roles.clear();
    }

    /**
     * Does the user have this role?
     * 
     * @param role
     *            Name of role to check
     * @return true if the user has the role, false if not
     */
    public boolean hasRole(String role)
    {
        return _roles.contains(role);
    }

    /**
     * Returns an array of roles to which the user belongs. Modifying the array will NOT modify the user's role. Use
     * <code>addRole</code> and <code>removeRole</code> instead.
     */
    public String[] getRoles()
    {
        return _roles.toArray(new String[] {});
    }

    /**
     * Returns the status of this user account
     */
    public Status getStatus()
    {
        return _status;
    }

    /**
     * Sets the status of this user account
     * 
     * @param status
     */
    public void setStatus(Status status)
    {
        _status = status;
    }

    /**
     * Returns the name of the user to display on the UI
     */
    public String getDisplayName()
    {
        return _displayName;
    }

    /**
     * Sets the name of the user to display on the UI
     */
    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

    /**
     * Returns the email address of the user
     */
    public String getEmailAddress()
    {
        return _emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        _emailAddress = emailAddress;
    }

    /**
     * Returns if the user is a system administrator or not
     */
    public boolean isSystemAdministrator()
    {
        return hasRole(SYSTEM_ADMINISTRATOR_ROLE_NAME);
    }

    /**
     * Creates the role name for users who can manage the repository. Administrators have access to the repository in
     * the workbench, can publish to the repository, can subscribe to the repository as well configure the repository.
     * 
     * @param repositoryName
     *            name of the repository
     * @return Role name that grants the user permission to administer the named repository
     */
    public static String createRepositoryAdministratorRoleName(String repositoryName)
    {
        return String.format(REPOSITORY_ADMINISTRATOR_ROLE_TEMPLATE, repositoryName);
    }

    /**
     * Creates the role name for users who can access the named repository from the workbench. Workbench users cannot
     * publish but they can subscribe to log entries (for streaming)
     * 
     * @param repositoryName
     *            name of the repository
     * @return Role name that grants the user permission to access the named repository from the workbench.
     */
    public static String createRepositoryWorkbenchRoleName(String repositoryName)
    {
        return String.format(REPOSITORY_WORKBENCH_ROLE_TEMPLATE, repositoryName);
    }

    /**
     * Creates the role name for users who can publish log entries to this repository.
     * 
     * @param repositoryName
     *            name of the repository
     * @return Role name that grants the user permission to publish log entries to this repository.
     */
    public static String createRepositoryPublisherRoleName(String repositoryName)
    {
        return String.format(REPOSITORY_PUBLISHER_ROLE_TEMPLATE, repositoryName);
    }

    /**
     * Creates the role name for users who can subscribe to log entries from this repository
     * 
     * @param repositoryName
     *            name of the repository
     * @return Role name that grants the user permission subscribe to log entries from this repository
     */
    public static String createRepositorySubscriberRoleName(String repositoryName)
    {
        return String.format(REPOSITORY_SUBSCRIBER_ROLE_TEMPLATE, repositoryName);
    }

    /**
     * <p>
     * Given a role name, return the repository to which the role provides permission. Assumes repository roles are in
     * the format: <code>repo.[repository name].[role type]</code>
     * </p>
     * <p>
     * This provides a quick short cut way to find out the repositories to which a user has access
     * </p>
     * 
     * @param role
     *            Role name. For example repo.xxx.administrator
     * @return The repository name (xxx in the above example). Null if not repository access.
     */
    public static String extractRepositoryNameFromRole(String role)
    {
        if (StringUtils.isBlank(role))
        {
            return null;
        }

        if (!role.startsWith(REPOSITORY_ROLE_PREFIX))
        {
            return null;
        }

        if (role.endsWith(REPOSITORY_ADMINISTRATOR_ROLE_SUFFIX) || role.endsWith(REPOSITORY_WORKBENCH_ROLE_SUFFIX)
                || role.endsWith(REPOSITORY_PUBLISHER_ROLE_SUFFIX) || role.endsWith(REPOSITORY_SUBSCRIBER_ROLE_SUFFIX))
        {
            return role.substring(5, role.lastIndexOf('.'));
        }

        return null;
    }

    /**
     * User status
     * 
     * @author vibul
     * 
     */
    public static enum Status
    {
        /**
         * User can login
         */
        ENABLED,

        /**
         * User cannot login
         */
        DISABLED,

        /**
         * User cannot login due to too many failed login attempts
         */
        LOCKED
    }
}
