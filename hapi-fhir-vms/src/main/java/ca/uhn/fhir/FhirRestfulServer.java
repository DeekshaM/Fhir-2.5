package ca.uhn.fhir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;

import ca.uhn.fhir.config.CommonConfig;
import ca.uhn.fhir.config.Dstu1Config;
import ca.uhn.fhir.config.Dstu2Config;
import ca.uhn.fhir.config.Dstu3Config;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.config.WebsocketDstu2Config;
import ca.uhn.fhir.jpa.config.dstu3.WebsocketDstu3Config;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.JpaConformanceProviderDstu1;
import ca.uhn.fhir.jpa.provider.JpaConformanceProviderDstu2;
import ca.uhn.fhir.jpa.provider.JpaSystemProviderDstu1;
import ca.uhn.fhir.jpa.provider.JpaSystemProviderDstu2;
import ca.uhn.fhir.jpa.provider.dstu3.JpaConformanceProviderDstu3;
import ca.uhn.fhir.jpa.provider.dstu3.JpaSystemProviderDstu3;
import ca.uhn.fhir.jpa.provider.dstu3.TerminologyUploaderProviderDstu3;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.model.dstu2.composite.MetaDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.BanUnsupportedHttpMethodsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

public class FhirRestfulServer extends RestfulServer {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirRestfulServer.class);

	private AnnotationConfigWebApplicationContext myAppCtx;
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() throws ServletException {
		super.initialize();
		
		String base = "http://localhost:" + 8081 + "/baseDstu2";		
		// Get the spring context from the web container (it's declared in web.xml)
		WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		
		// These two parmeters are also declared in web.xml
		String implDesc = getInitParameter("ImplementationDescription");
		String fhirVersionParam = getInitParameter("FhirVersion");
		if (StringUtils.isBlank(fhirVersionParam)) {
			fhirVersionParam = "DSTU1";
		}

		// Depending on the version this server is supporing, we will
		// retrieve all the appropriate resource providers and the
		// conformance provider
		List<IResourceProvider> beans;
		@SuppressWarnings("rawtypes")
		IFhirSystemDao systemDao;
		ETagSupportEnum etagSupport;
		String baseUrlProperty;
		List<Object> plainProviders = new ArrayList<Object>();
		
		switch (fhirVersionParam.trim().toUpperCase()) {
		case "DSTU1": {
			myAppCtx = new AnnotationConfigWebApplicationContext();
			myAppCtx.setServletConfig(getServletConfig());
			myAppCtx.setParent(parentAppCtx);
			myAppCtx.register(Dstu1Config.class);
			myAppCtx.refresh();
			setFhirContext(FhirContext.forDstu1());
			beans = myAppCtx.getBean("myResourceProvidersDstu1", List.class);
			plainProviders.add(myAppCtx.getBean("mySystemProviderDstu1", JpaSystemProviderDstu1.class));
			systemDao = myAppCtx.getBean("mySystemDaoDstu1", IFhirSystemDao.class);
			etagSupport = ETagSupportEnum.DISABLED;
			JpaConformanceProviderDstu1 confProvider = new JpaConformanceProviderDstu1(this, systemDao);
			confProvider.setImplementationDescription(implDesc);
			setServerConformanceProvider(confProvider);
			baseUrlProperty = base.replace("Dstu2", "Dstu1");
			System.out.println("baseURL Property DSTU1 : "+baseUrlProperty);
			break;
		}		
		case "DSTU2": {
			myAppCtx = new AnnotationConfigWebApplicationContext();
			myAppCtx.setServletConfig(getServletConfig());
			myAppCtx.setParent(parentAppCtx);			
			myAppCtx.register(Dstu2Config.class, WebsocketDstu2Config.class);
			baseUrlProperty = base;			
			myAppCtx.refresh();
			setFhirContext(FhirContext.forDstu2());
			beans = myAppCtx.getBean("myResourceProvidersDstu2", List.class);
			plainProviders.add(myAppCtx.getBean("mySystemProviderDstu2", JpaSystemProviderDstu2.class));
			systemDao = myAppCtx.getBean("mySystemDaoDstu2", IFhirSystemDao.class);
			etagSupport = ETagSupportEnum.ENABLED;			
			JpaConformanceProviderDstu2 confProvider = new JpaConformanceProviderDstu2(this, systemDao, myAppCtx.getBean(DaoConfig.class));
			confProvider.setImplementationDescription(implDesc);
			setServerConformanceProvider(confProvider);
			System.out.println("baseURL Property DSTU2 : "+baseUrlProperty);
			break;
		}
		case "DSTU3": {
			myAppCtx = new AnnotationConfigWebApplicationContext();
			myAppCtx.setServletConfig(getServletConfig());
			myAppCtx.setParent(parentAppCtx);
			myAppCtx.register(Dstu3Config.class, WebsocketDstu3Config.class);
			baseUrlProperty = base.replace("Dstu2", "Dstu3");			
			myAppCtx.refresh();
			setFhirContext(FhirContext.forDstu3());
			beans = myAppCtx.getBean("myResourceProvidersDstu3", List.class);
			plainProviders.add(myAppCtx.getBean("mySystemProviderDstu3", JpaSystemProviderDstu3.class));
			systemDao = myAppCtx.getBean("mySystemDaoDstu3", IFhirSystemDao.class);
			etagSupport = ETagSupportEnum.ENABLED;
			JpaConformanceProviderDstu3 confProvider = new JpaConformanceProviderDstu3(this, systemDao, myAppCtx.getBean(DaoConfig.class));
			confProvider.setImplementationDescription(implDesc);
			setServerConformanceProvider(confProvider);
			plainProviders.add(myAppCtx.getBean(TerminologyUploaderProviderDstu3.class));
			System.out.println("baseURL Property DSTU3 : "+baseUrlProperty);
			break;
		}
		default:
			throw new ServletException("Unknown FHIR version specified in init-param[FhirVersion]: " + fhirVersionParam);
		}
		
						
		/*
		 * On the DSTU2 endpoint, we want to enable ETag support 
		 */
		setETagSupport(etagSupport);

		/*
		 * This server tries to dynamically generate narratives
		 */
		FhirContext ctx = getFhirContext();
		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		
		/*
		 * The resource and system providers (which actually implement the various FHIR 
		 * operations in this server) are all retrieved from the spring context above
		 * and are provided to the server here.
		 */
		for (IResourceProvider nextResourceProvider : beans) {
			ourLog.info(" * Have resource provider for: {}", nextResourceProvider.getResourceType().getSimpleName());
		}
		setResourceProviders(beans);

		setPlainProviders(plainProviders);

		/*
		 * Enable CORS
		 */
		CorsConfiguration config = new CorsConfiguration();
		CorsInterceptor corsInterceptor = new CorsInterceptor(config);
		config.addAllowedHeader("Origin");
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("X-Requested-With");
		config.addAllowedHeader("Content-Type");
		config.addAllowedHeader("Access-Control-Request-Method");
		config.addAllowedHeader("Access-Control-Request-Headers");
		config.addAllowedOrigin("*");
		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
		registerInterceptor(corsInterceptor);

		/*
		 * We want to format the response using nice HTML if it's a browser, since this
		 * makes things a little easier for testers.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());
		System.out.println("RegisterInterceptor for ResponseHighlighterInterceptor.........");
		registerInterceptor(new BanUnsupportedHttpMethodsInterceptor());
		System.out.println("RegisterInterceptor for BanunsupportedHttpMethodsInterceptor.........");
		
		/*
		 * Default to JSON with pretty printing
		 */
		setDefaultPrettyPrint(true);
		setDefaultResponseEncoding(EncodingEnum.JSON);		
		System.out.println("MyHardcodedServerAddress Strategy  : "+baseUrlProperty);
		setServerAddressStrategy(new MyHardcodedServerAddressStrategy(baseUrlProperty));
		
		/*
		 * Spool results to the database 
		 */
		setPagingProvider(myAppCtx.getBean(DatabaseBackedPagingProvider.class));
		
		/*
		 * Load interceptors for the server from Spring
		 */
		if(CommonConfig.prop.getProperty("INTCR.Enable").equals("true")) {
			System.out.println("Loading the Interceptors for the server.....");
			Collection<IServerInterceptor> interceptorBeans = myAppCtx.getBeansOfType(IServerInterceptor.class).values();
			for (IServerInterceptor interceptor : interceptorBeans) {
				this.registerInterceptor(interceptor);
				System.out.println("Register Interceptor for InterceptorBean :"+interceptor);
			}
			System.out.println("Loading the Intercepotors for the server completed");
			
			RequestInterceptor interceptor = new RequestInterceptor();
			registerInterceptor(interceptor);
			System.out.println("Registering the RequestInterceptor....."+interceptor.toString());
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		ourLog.info("Server is shutting down");
		myAppCtx.destroy();
	}

	/**
	 * The public server is deployed to http://fhirtest.uhn.ca and the JEE webserver
	 * where this FHIR server is deployed is actually fronted by an Apache HTTPd instance,
	 * so we use an address strategy to let the server know how it should address itself.
	 */
	private static class MyHardcodedServerAddressStrategy extends HardcodedServerAddressStrategy {

		public MyHardcodedServerAddressStrategy(String theBaseUrl) {
			super(theBaseUrl);
		}

		@Override
		public String determineServerBase(ServletContext theServletContext, HttpServletRequest theRequest) {
			/*
			 * This is a bit of a hack, but we want to support both HTTP and HTTPS seamlessly
			 * so we have the outer httpd proxy relay requests to the Java container on 
			 * port 28080 for http and 28081 for https.
			 */
			String retVal = super.determineServerBase(theServletContext, theRequest);
			if (theRequest.getRequestURL().indexOf("28081") != -1) {
				retVal = retVal.replace("http://", "https://");
			}
			return retVal;
		}
		
	}
	
	
	
}
