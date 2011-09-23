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

package org.chililog.server.workbench.workers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.AppProperties;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.CryptoUtils;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.data.UserBO;
import org.chililog.server.workbench.Strings;
import org.chililog.server.workbench.workers.AuthenticationAO.ExpiryType;

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
public class AuthenticationTokenAO {

    private String _id;

    private String _userID;

    private ExpiryType _expiryType;

    private int _expirySeconds;

    private Date _expiresOn;

    /**
     * Basic constructor
     */
    public AuthenticationTokenAO() {
        return;
    }

    /**
     * Constructor using the details of a validated user
     * 
     * @param userBO
     *            Authenticated user
     * @param authenticationApiObject
     *            authentication object with the user's details
     */
    public AuthenticationTokenAO(UserBO userBO, AuthenticationAO authenticationApiObject) {
        _id = UUID.randomUUID().toString();
        _userID = userBO.getDocumentID().toString();
        _expiryType = authenticationApiObject.getExpiryType();
        _expirySeconds = authenticationApiObject.getExpirySeconds();
        updateExpiresOn();

        return;
    }

    /**
     * Updates the expiry date of the token to the current time + number of expiry seconds.
     */
    public void updateExpiresOn() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.SECOND, _expirySeconds);
        _expiresOn = cal.getTime();
    }

    /**
     * Unique id for this session
     */
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    /**
     * The user's mongoDB document id
     */
    public String getUserID() {
        return _userID;
    }

    public void setUserID(String userID) {
        _userID = userID;
    }

    /**
     * Method used to determine when the issued authentication token expires. See {@link ExpiryType}
     */
    public ExpiryType getExpiryType() {
        return _expiryType;
    }

    public void setExpiryType(ExpiryType expiryType) {
        _expiryType = expiryType;
    }

    /**
     * Number of seconds before the issued authentication token expires
     */
    public int getExpirySeconds() {
        return _expirySeconds;
    }

    public void setExpirySeconds(int expirySeconds) {
        _expirySeconds = expirySeconds;
    }

    /**
     * The date and time when this token expires
     */
    public Date getExpiresOn() {
        return _expiresOn;
    }

    public void setExpiresOn(Date expiresOn) {
        _expiresOn = expiresOn;
    }

    /**
     * <p>
     * Convert the token to a string.
     * </p>
     * <p>
     * Format is <code>json(AuthenticationTokenAO) + ~~~ + hash(json(AuthenticationTokenAO))</code>
     * </p>
     */
    @Override
    public String toString() {
        try {
            AppProperties appProperties = AppProperties.getInstance();
            String json = JsonTranslator.getInstance().toJson(this).replace("\n", "");
            StringBuilder sb = new StringBuilder();
            sb.append(json);
            sb.append("~~~");
            sb.append(CryptoUtils.createSHA512Hash(json, appProperties.getWorkbenchApiAuthenticationHashSalt(), false));

            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error creating authentication token.", ex);
        }
    }

    /**
     * <p>
     * Loads from an encrypted token
     * </p>
     * <p>
     * Format is <code>json(AuthenticationTokenAO) + ~~~ + hash(json(AuthenticationTokenAO))</code>
     * </p>
     * 
     * @param tokenString
     *            String representation of a token as returned by <code>toString()</code>.
     * @throws ChiliLogException
     *             If token is invalid, errorCode set to Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR. If token is
     *             expired, errorCode set to Strings.AUTHENTICAITON_TOKEN_EXPIRED_ERROR.
     */
    public static AuthenticationTokenAO fromString(String tokenString) throws ChiliLogException {
        if (StringUtils.isBlank(tokenString)) {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }

        AppProperties appProperties = AppProperties.getInstance();

        int separatorIndex = tokenString.indexOf("~~~");
        if (separatorIndex < 0) {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }
        String json = tokenString.substring(0, separatorIndex);
        String hash = tokenString.substring(separatorIndex + 3);

        if (!CryptoUtils.verifyHash(json, appProperties.getWorkbenchApiAuthenticationHashSalt(), hash)) {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_INVALID_ERROR);
        }

        AuthenticationTokenAO token = JsonTranslator.getInstance().fromJson(json, AuthenticationTokenAO.class);
        if (token.getExpiresOn().getTime() < new Date().getTime()) {
            throw new ChiliLogException(Strings.AUTHENTICAITON_TOKEN_EXPIRED_ERROR);
        }

        return token;
    }
}
