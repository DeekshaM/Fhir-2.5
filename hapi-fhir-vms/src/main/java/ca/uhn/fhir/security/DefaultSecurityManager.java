/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package ca.uhn.fhir.security;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import ca.uhn.fhir.config.CommonConfig;

import java.util.Map;

public class DefaultSecurityManager implements SecurityManager {
    private final static Logger logger = LoggerFactory.getLogger(DefaultSecurityManager.class);
    @Override
    public boolean isUserAuthenticatedAndAuthorized(String accessToken, Map<String, String> claimMap, String username, String password)
            throws AiravataSecurityException {
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            OAuthTokenValidationClient oauthClient = new OAuthTokenValidationClient(CommonConfig.prop.getProperty("AUTH.Url"),
                    username, password, configContext);
            OAuth2TokenValidationResponseDTO validationResponse = oauthClient.validateAccessToken(accessToken);
            return validationResponse.getValid();
        } catch (AxisFault axisFault) {
            throw new AiravataSecurityException(axisFault.getMessage());
        } catch (Exception exception) {
            logger.error(exception.getCause().toString());
            throw new AiravataSecurityException(exception.getMessage());
        }
    }
	@Override
	public boolean isUserAuthenticatedAndAuthorized(String accessToken, Map<String, String> claimMap)
			throws AiravataSecurityException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
