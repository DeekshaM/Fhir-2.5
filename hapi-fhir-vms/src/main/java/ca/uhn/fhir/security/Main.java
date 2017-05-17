/*
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
 */

package ca.uhn.fhir.security;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

public class Main {
    private static String accessToken = "7afeabcd-dcc5-388d-a765-6196197f7ac9";

    public static void main(String[] args) throws Exception {
        //register OAuth application
       /* ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

        OAuthAppRegisteringClient authAppRegisteringClient = new OAuthAppRegisteringClient(
                Properties.oauthAuthzServerURL, Properties.userName, Properties.password, configContext);
        OAuthConsumerAppDTO appDTO = authAppRegisteringClient.registerApplication("AiravataGateway2");
        System.out.println("Consumer key: " + appDTO.getOauthConsumerKey());
        System.out.println("Consumer secret: " + appDTO.getOauthConsumerSecret());*/
        //obtain OAuth access token

        //validate access token
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        boolean isAuthz = securityManager.isUserAuthenticatedAndAuthorized(accessToken, null);
        if (isAuthz) {
            System.out.println("Authentication Successful...");
        } else {
            System.out.println("Authentication Failed...");
        }
    }
}
