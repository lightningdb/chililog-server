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

package com.chililog.server.data;

import java.io.Serializable;
import java.util.ArrayList;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.security.CryptoUtils;
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

    private String _username;
    private String _password;
    private ArrayList<String> _roles = new ArrayList<String>();
    private Status _status = Status.Enabled;
    private String _displayName;

    static final String USERNAME_FIELD_NAME = "username";
    static final String PASSWORD_FIELD_NAME = "password";
    static final String ROLES_FIELD_NAME = "roles";
    static final String STATUS_FIELD_NAME = "status";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";

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
        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject)
    {
        MongoUtils.setString(dbObject, USERNAME_FIELD_NAME, _username);
        MongoUtils.setString(dbObject, PASSWORD_FIELD_NAME, _password);
        MongoUtils.setStringArrayList(dbObject, ROLES_FIELD_NAME, _roles);
        MongoUtils.setString(dbObject, STATUS_FIELD_NAME, _status.toString());
        MongoUtils.setString(dbObject, DISPLAY_NAME_FIELD_NAME, _displayName);
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
            _password = CryptoUtils.createHash(password, null);
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
     * 
     * @param role
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
        Enabled,

        /**
         * User cannot login
         */
        Disabled,

        /**
         * User cannot login due to too many failed login attempts
         */
        Locked
    }
}
