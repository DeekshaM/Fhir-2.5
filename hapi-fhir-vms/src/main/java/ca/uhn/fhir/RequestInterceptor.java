package ca.uhn.fhir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import ca.uhn.fhir.config.CommonConfig;
import ca.uhn.fhir.jpa.provider.BaseJpaProvider;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.security.AiravataSecurityException;
import ca.uhn.fhir.security.DefaultSecurityManager;

public class RequestInterceptor extends InterceptorAdapter implements WebRequestInterceptor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestInterceptor.class);
	

	@Override
	public void afterCompletion(WebRequest theArg0, Exception theArg1) throws Exception {
		org.slf4j.MDC.remove(BaseJpaProvider.REMOTE_ADDR);
		org.slf4j.MDC.remove(BaseJpaProvider.REMOTE_UA);
	}

	@Override
	public void postHandle(WebRequest theArg0, ModelMap theArg1) throws Exception {
		// nothing
	}

	@Override
	public void preHandle(WebRequest theRequest) throws Exception {
		System.out.println("preHandle() .........................");
		String[] forwardedFors = theRequest.getHeaderValues("x-forwarded-for");
		StringBuilder b = new StringBuilder();
		if (forwardedFors != null) {
			for (String enums : forwardedFors) {
				if (b.length() > 0) {
					b.append(" / ");
				}
				b.append(enums);
			}
		}

		String forwardedFor = b.toString();
		org.slf4j.MDC.put(BaseJpaProvider.REMOTE_ADDR, forwardedFor);

		String userAgent = StringUtils.defaultString(theRequest.getHeader("user-agent"));
		org.slf4j.MDC.put(BaseJpaProvider.REMOTE_UA, userAgent);

		logger.trace("User agent is: {}", userAgent);

	}
	
	/*@Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
		System.out.println("incomingRequestPreProcessed() HttpServletRequest........................."+theRequest.toString());
		System.out.println("incomingRequestPreProcessed() HttpServletResponse........................."+theResponse.toString());
        String authorization = theRequest.getHeader("Authorization");
        logger.info("incomingRequestPreProcessed Authorization header:"+authorization);

        if (authorization == null) {
            theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            theResponse.setHeader("AuthServer", CommonConfig.prop.getProperty("AUTH.Server"));
            logger.info("No Authorization header.  ");
            return false;
        }

        String authToken = authorization.replace("Bearer","").trim();

        if (authToken.contains("REJECT")) {
            theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } else if (!checkAuthentication(authToken)) {
            logger.info("Not accepting auth token... ");
            theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } else {
            return true;
        }
        
        https://fhirauth.azurewebsites.net/connect/accesstokenvalidation?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJjbGllbnRfaWQiOiJpbXBsaWNpdGNsaWVudCIsInNjb3BlIjoid3JpdGUiLCJzdWIiOiI4MTg3MjciLCJhbXIiOiJwYXNzd29yZCIsImF1dGhfdGltZSI6MTQ0NjI0Mjg3NiwiaWRwIjoiaWRzcnYiLCJpc3MiOiJodHRwczovL2ZoaXJhdXRoLmF6dXJld2Vic2l0ZXMubmV0IiwiYXVkIjoiaHR0cHM6Ly9maGlyYXV0aC5henVyZXdlYnNpdGVzLm5ldC9yZXNvdXJjZXMiLCJleHAiOjE0NDYyNDY0NzksIm5iZiI6MTQ0NjI0Mjg3OX0.WJ-_ARmjz2QTOYZR728QzQkaI1hPUHeEujTwov-S0-_kz2Cl0NLcwUOVo1hWePDsTRm72OmWBnixd4wBILHHOBRJGS53gAeXOzYm0ck9u-Sc9ggvaLzUOIgENu1SRR0DmjF2gS5HJOCugiIzdx-ncaqOh1C5zGugca7_LY82DOexwjCuhwKlMHqeoE26xuGU6VHf3umCMMAzaftO1XYZuPteu9jDK1qYbsGNox71pLQcextbsMKBqbNt6tbejIgIcu6Q15BLJT06Lscb8TCMyhbkjIt96bqd2shmA6uUuOCBSVAzSzfsxVm3Ocrog-QoDru9pM4T2pyB3h4yHukmQg
        --->>>
             {
                "client_id": "implicitclient",
                "scope": "write",
                "sub": "818727",
                "amr": "password",
                "auth_time": "1446242876",
                "idp": "idsrv",
                "iss": "https://fhirauth.azurewebsites.net",
                "aud": "https://fhirauth.azurewebsites.net/resources",
                "exp": "1446246479",
                "nbf": "1446242879"
            }
         
		
    }*/
	
	/*
     * This method is called for each request and decode the JWT token which is
       in the header part of the request
     * and validate the access token using wso2 identity server.
     * @param jwt
     * @return
     * @author Satish
     * */

    @Override
    public boolean incomingRequestPreProcessed(
          HttpServletRequest theRequest, HttpServletResponse theResponse) {
           Claims claims = null;
           String jwt = theRequest.getHeader("jwt");
           if (jwt == null) {
                  theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  logger.info("No Authorization header.  ");
                  return false;
           }
           try {
                  claims = Jwts.parser()
                          .setSigningKey("secret".getBytes("UTF-8"))
                          .parseClaimsJws(jwt)
                          .getBody();
           } catch (ExpiredJwtException e) {
                  e.printStackTrace();
                  return false;
           } catch (UnsupportedJwtException e) {
                  e.printStackTrace();
                  return false;
           } catch (MalformedJwtException e) {
                  e.printStackTrace();
                  return false;
           } catch (SignatureException e) {
                  e.printStackTrace();
                  return false;
           } catch (IllegalArgumentException e) {
                  e.printStackTrace();
                  return false;
           } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
                  return false;
           }
           logger.info("AccessToken:       " + claims.getSubject());
           String accessToken = claims.getSubject().replace("Bearer", "").trim();
           if (accessToken.contains("REJECT")) {
                  theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  return false;
           } else if (!checkAuthentication(accessToken, CommonConfig.prop.getProperty("AUTH.User"), CommonConfig.prop.getProperty("AUTH.Password"))) {
                  logger.info("Validated Access Token: UnAutorized ");
                  theResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  return false;
           } else {
                  logger.info("Validated Access Token: Autorized");
                  return true;
           }
    }

    boolean checkAuthentication(
                     String accessToken, String username, String password) {
           DefaultSecurityManager securityManager = new DefaultSecurityManager();
           boolean isAuthz = false;
           try {
      isAuthz = securityManager.isUserAuthenticatedAndAuthorized(accessToken, null, username, password);
           } catch (AiravataSecurityException e) {
                  logger.info("An exception occurred while validating the token with identity server.");
           }
           return isAuthz;
    }


    @Override
    public void incomingRequestPreHandled(RestOperationTypeEnum theOperation, IServerInterceptor.ActionRequestDetails theRequestDetails) {
    	System.out.println("incomingRequestPreHandled().............................."+theOperation.toString());
    	System.out.println("incomingRequestPreHandled().............................."+theRequestDetails.toString());
        logger.info("incomingRequestPreHandled code:"+theOperation.getCode());
        logger.info("incomingRequestPreHandled resource type:"+theRequestDetails.getResourceType());

        // TODO: Convert any incoming data here to the correct details "de-anonymize it"
    }

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails,
                                    Bundle theResponseObject,
                                    HttpServletRequest theServletRequest,
                                    HttpServletResponse theServletResponse) {
        logger.info("outgoingResponse called");
        System.out.println("outgoingResponse() RequestDetails.............................."+theRequestDetails.toString());
        System.out.println("outgoingResponse() Bundle.............................."+theResponseObject.toString());
        System.out.println("outgoingResponse() HttpServletRequest.............................."+theServletRequest.toString());
        System.out.println("outgoingResponse() HttpServletResponse.............................."+theServletResponse.toString());       
        return true;

    }

    // Todo: The below is a hack... it was late at night... This should be using resttemplate or something nicer...
    // It also shouldn't accept any certificate. That's awful actually!
    boolean checkAuthentication(String token) {
    	System.out.println("checkAuthentication().............."+token);
        try {
            try {

                final String url = CommonConfig.prop.getProperty("AUTH.Url")+token;
                boolean success = false;

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname,
                                          SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                HttpsURLConnection c = (HttpsURLConnection) new URL(url)
                        .openConnection();
                c.setUseCaches(false);
                c.connect();

                success = readStream(c.getInputStream());
                c.disconnect();
                return success;

            } catch (Exception e) {
                logger.info("Exception:" + e);
                return false;
            }


        } catch (Exception e) {
            logger.error("checkAuthentication:" + e);
            return false;
        }
    }

    private boolean readStream(InputStream in) {
        BufferedReader reader = null;
        boolean success = false;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                logger.info(">"+line);
                if (line.contains("scope")) {
                    logger.info("Accepting it");
                    success = true;
                }
            }
        } catch (IOException e) {
            logger.debug("Exception:" + e);
            success = false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.debug("Exception:" + e);
                    success = false;
                }
            }
        }
        return success;
    }

    /*
    boolean checkAuthentication(String token) {

        try {

            final String uri = "https://fhirauth.azurewebsites.net/connect/accesstokenvalidation";
            Map<String, String> params = new HashMap<String, String>();
            params.put("token", token);

            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class, params);

            logger.info("checkAuthentication"+result);

            return true;

        } catch(Exception e) {
            logger.error("checkAuthentication:"+e);
            return false;
        }
    }
    */

    /*
    boolean checkAuthentication(String token) {

        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://fhirauth.azurewebsites.net/connect/accesstokenvalidation?token="+token);
            HttpResponse response = client.execute(request);
            logger.info("Checking token response:" + response.getStatusLine().getStatusCode());
            logger.info("checkAuthentication"+response.getProtocolVersion());
            logger.info("checkAuthentication"+response.getStatusLine().getStatusCode());
            logger.info("checkAuthentication"+response.getStatusLine().getReasonPhrase());
            logger.info("checkAuthentication"+response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                if (len != -1 && len < 2048) {
                    logger.info("checkAuthentication"+EntityUtils.toString(entity));
                } else {
                    // Stream content out
                }
            }

            return true;

        } catch(Exception e) {
            logger.error("checkAuthentication:"+e);
            return false;
        }
        */	

}
