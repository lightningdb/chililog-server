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

package com.chililog.server.ui.api;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.security.CryptoUtils;
import com.chililog.server.ui.Strings;
import com.chililog.server.ui.api.AuthenticationAO.ExpiryType;

/**
 * <p>
 * Authentication API Object is used for login
 * </p>
 * 
 * @author vibul
 * 
 */
/**
 * @author vibul
 * 
 */
public class AuthenticationTokenAO
{
    private String _id;

    private String _username;

    private ExpiryType _expiryType;

    private int _expirySeconds;

    private Date _expiresOn;

    /**
     * Basic constructor
     */
    public AuthenticationTokenAO()
    {
        return;
    }

    /**
     * Constructor using the details of a validated user
     * 
     * @param authenticationApiObject
     *            authentication object with the user's details
     */
    public AuthenticationTokenAO(AuthenticationAO authenticationApiObject)
    {
        _id = UUID.randomUUID().toString();
        _username = authenticationApiObject.getUsername();
        _expiryType = authenticationApiObject.getExpiryType();
        _expirySeconds = authenticationApiObject.getExpirySeconds();
        updateExpiresOn();

        return;
    }

    /**
     * Updates the expiry date of the token to the current time + number of expiry seconds.
     */
    public void updateExpiresOn()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.SECOND, _expirySeconds);
        _expiresOn = cal.getTime();
    }

    /**
     * Unique id for this session
     */
    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    /**
     * The user's login name
     */
    public String getUsername()
    {
        return _username;
    }

    public void setUsername(String username)
    {
        _username = username;
    }

    /**
     * Method used to determine when the issued authentication token expires. See {@link ExpiryType}
     */
    public ExpiryType getExpiryType()
    {
        return _expiryType;
    }

    public void setExpiryType(ExpiryType expiryType)
    {
        _expiryType = expiryType;
    }

    /**
     * Number of seconds before the issued authentication token expires
     */
    public int getExpirySeconds()
    {
        return _expirySeconds;
    }

    public void setExpirySeconds(int expirySeconds)
    {
        _expirySeconds = expirySeconds;
    }

    /**
     * The date and time when this token expires
     */
    public Date getExpiresOn()
    {
        return _expiresOn;
    }

    public void setExpiresOn(Date expiresOn)
    {
        _expiresOn = expiresOn;
    }

    /**
     * <p>
     * Convert the token to a string.
     * </p>
     * <p>
     * Format is <code>encrypt(json(AuthenticationTokenAO) + ~~~ + hash(json(AuthenticationTokenAO)))</code>
     * </p>
     */
    @Override
    public String toString()
    {
        try
        {
            AppProperties appProperties = AppProperties.getInstance();
            String json = JsonTranslator.getInstance().toJson(this);
            StringBuilder sb = new StringBuilder();
            sb.append(json);
            sb.append("~~~");
            sb.append(CryptoUtils.createHash(json, appProperties.getWebApiAuthenticationHashSalt(), false));

            String encryptedToken = CryptoUtils.encryptTripleDES(sb.toString(),
                    appProperties.getWebApiAuthenticationEncryptionPassword());

            return encryptedToken;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error creating authentication token.", ex);
        }
    }

    /**
     * <p>
     * Loads from an encrypted token
     * </p>
     * <p>
     * Format is <code>encrypt(json(AuthenticationTokenAO) + ~~~ + hash(json(AuthenticationTokenAO)))</code>
     * </p>
     * 
     * @throws ChiliLogException
     *             If token is invalid, errorCode set to Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR. If token is
     *             expired, errorCode set to Strings.AUTHENTICAITON_TOKEN_EXPIRED_ERROR.
     */
    public static AuthenticationTokenAO fromString(String encryptedToken) throws ChiliLogException
    {
        if (StringUtils.isBlank(encryptedToken))
        {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }

        AppProperties appProperties = AppProperties.getInstance();

        String plainTextToken = null;
        try
        {
            plainTextToken = CryptoUtils.decryptTripleDES(encryptedToken,
                    appProperties.getWebApiAuthenticationEncryptionPassword());
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }

        int separatorIndex = plainTextToken.indexOf("~~~");
        String json = plainTextToken.substring(0, separatorIndex);
        String hash = plainTextToken.substring(separatorIndex + 3);

        if (!CryptoUtils.verifyHash(json, appProperties.getWebApiAuthenticationHashSalt(), hash))
        {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }

        AuthenticationTokenAO token = JsonTranslator.getInstance().fromJson(json, AuthenticationTokenAO.class);
        if (token.getExpiresOn().getTime() < new Date().getTime())
        {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_EXPIRED_ERROR);
        }

        return token;
    }
}
