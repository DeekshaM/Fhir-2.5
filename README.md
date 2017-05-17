hapi-fhir
=========

HAPI FHIR - Java API for HL7 FHIR Clients and Servers

Changes for Intercepter in Fhir :

vms-hapi-fhir
   pom.xml to add follwoing Repository and dependencies

Add following Repository :
==========================
    <repository>
      <id>wso2-nexus</id>
      <name>WSO2 internal Repository</name>
      <url>http://maven.wso2.org/nexus/content/groups/wso2-public/</url>
      <releases>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
        <checksumPolicy>ignore</checksumPolicy>
      </releases>
    </repository>

 
Dependencyies for Interceptor :
===============================
    <dependency>
      <groupId>javax.json</groupId>
      <artifactId>javax.json-api</artifactId>
      <version>1.0</version>
    </dependency>
    <dependency>
      <groupId>org.wso2.carbon</groupId>
      <artifactId>org.wso2.carbon.identity.oauth.stub</artifactId>
      <version>4.2.3</version>
    </dependency>
    <dependency>
      <groupId>org.wso2.carbon</groupId>
      <artifactId>org.wso2.carbon.utils</artifactId>
      <version>4.2.0</version>
    </dependency>
    <dependency>
      <groupId>org.apache.axis2.wso2</groupId>
      <artifactId>axis2</artifactId>
      <version>1.6.1.wso2v4</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt</artifactId>
      <version>0.7.0</version>
    </dependency>

import the following packages in ca.uhn.fhir.RequestInterceptor class

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

Added this piece of code in ca.uhn.fhir.RequestInterceptor class:

And already existing incomingRequestPreProcessed() to commented.

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
             } else if (!checkAuthentication(accessToken, _USERNAME, _PASSWORD)) {
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



Added new package ca.uhn.fhir.security

Updated the config.properties file as below:

AUTH.Url=https://192.168.128.19:9443/services/
AUTH.User=admin
AUTH.Password=admin