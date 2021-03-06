package ca.uhn.fhir.security;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import javax.net.ssl.*;
import java.rmi.RemoteException;
import java.util.UUID;

public class OAuthAppRegisteringClient {
    private OAuthAdminServiceStub stub;
    private final static Logger logger = LoggerFactory.getLogger(OAuthTokenValidationClient.class);

    public OAuthAppRegisteringClient(String auhorizationServerURL, String username, String password,
                                     ConfigurationContext configCtx) throws Exception {
        String serviceURL = auhorizationServerURL + "OAuthAdminService";
        try {
            stub = new OAuthAdminServiceStub(configCtx, serviceURL);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true, stub._getServiceClient());
        } catch (AxisFault e) {
            logger.error("Error initializing OAuth2 Client");
            throw new Exception("Error initializing OAuth Client", e);
        }
        //TODO:enable proper SSL handshake
        try {
            // Get SSL context
            SSLContext sc = SSLContext.getInstance("SSL");

            // Create empty HostnameVerifier
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                               String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                               String authType) {
                }
            }};

            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sc);
        } catch (Exception e) {
            e.printStackTrace();
            //ignore
        }

    }

    public OAuthConsumerAppDTO registerApplication(String appName) throws AiravataSecurityException {

        try {
            OAuthConsumerAppDTO consumerAppDTO = new OAuthConsumerAppDTO();
            consumerAppDTO.setApplicationName(appName);
            // consumer key and secret is set by the application.
            //consumerAppDTO.setOauthConsumerKey(userName);
            //consumerAppDTO.setOauthConsumerSecret(UUID.randomUUID().toString());
            //consumerAppDTO.setCallbackUrl("");
            //consumerAppDTO.setUsername(userName);
            stub.registerOAuthApplicationData(consumerAppDTO);
            // After registration application is retrieve
            return stub.getOAuthApplicationDataByAppName(appName);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            throw new AiravataSecurityException("Error in registering the OAuth application.");
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new AiravataSecurityException("Error in registering the OAuth application.");
        } catch (OAuthAdminServiceException e) {
            e.printStackTrace();
            throw new AiravataSecurityException("Error in registering the OAuth application.");
        }
    }
}

