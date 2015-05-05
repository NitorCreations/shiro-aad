package com.nitorcreations.willow.shiro.aad;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.io.ResourceUtils;

/**
 * Configuration object for {@link AADRealm}.
 * 
 * @author Mikko Tommila
 */
public class AADRealmConfig {

  private static final String PREFIX = "com.nitorcreations.willow.shiro.aad.";
  private static final Properties DEFAULTS;

  static {
    DEFAULTS = new Properties();
    DEFAULTS.setProperty(PREFIX + "memberFunction", "getMemberObjects");
  }

  private String authority;
  private String tenant;
  private String graphResource;
  private String memberFunction;
  private boolean securityEnabledOnly;
  private String authenticationClientId;
  private String authorizationClientId;
  private String authorizationClientSecret;

  /**
   * Default constructor.
   */
  public AADRealmConfig() {
    load(DEFAULTS);
  }

  /**
   * Load the configuration from the given resource path.
   * 
   * @param resourcePath The path to the properties file containing configuration settings.
   * 
   * @see #load(Properties)
   */
  public void load(String resourcePath) {
    try (InputStream in = ResourceUtils.getInputStreamForPath(resourcePath)) {
      Properties properties = new Properties();
      properties.load(in);
      load(properties);
    } catch (IOException ioe) {
      throw new ShiroException("Error loading configuration", ioe);
    }
  }

  /**
   * Load the configuration from the given properties.
   * The property keys start with {@code "com.nitorcreations.willow.shiro.aad."},
   * followed by any of the JavaBean property names of this class.
   * 
   * @param properties The properties.
   * 
   * @see #setAuthority(String)
   * @see #setTenant(String)
   * @see #setGraphResource(String)
   * @see #setMemberFunction(String)
   * @see #setSecurityEnabledOnly(boolean)
   * @see #setAuthenticationClientId(String)
   * @see #setAuthorizationClientId(String)
   * @see #setAuthorizationClientSecret(String)
   */
  public void load(Properties properties) {
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(PREFIX)) {
        String name = key.substring(PREFIX.length());
        String value = properties.getProperty(key);
        try {
          BeanUtils.setProperty(this, name, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new ShiroException("Error setting property", e);
        }
      }
    }
  }

  /**
   * Get the authority.
   * 
   * @return The authority.
   * 
   * @see #setAuthority(String)
   */
  public String getAuthority() {
    return this.authority;
  }

  /**
   * Set the authority. This must be a valid URL with the
   * https protocol and ending with a slash ("/").
   * For example: {@code https://login.windows.net/}
   * 
   * @param authority The authority.
   */
  public void setAuthority(String authority) {
    this.authority = authority;
  }

  /**
   * Get the tenant.
   * 
   * @return The tenant.
   * 
   * @see #setTenant(String)
   */
  public String getTenant() {
    return this.tenant;
  }

  /**
   * Set the tenant. For example: {@code contoso.com}
   * 
   * @param tenant The tenant.
   */
  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  /**
   * Get the graph resource URL.
   * 
   * @return The Graph API URL.
   * 
   * @see #setGraphResource(String)
   */
  public String getGraphResource() {
    return this.graphResource;
  }

  /**
   * Set the graph resource URL. This is the URL of the Graph API.
   * It must be a valid URL with the https protocol, ending with a slash ("/").
   * For example: {@code https://graph.windows.net/}
   * 
   * @param graphResource The Graph API URL.
   */
  public void setGraphResource(String graphResource) {
    this.graphResource = graphResource;
  }

  /**
   * Get the member function.
   * 
   * @return The member function.
   * 
   * @see #setMemberFunction(String)
   */
  public String getMemberFunction() {
    return this.memberFunction;
  }

  /**
   * Set the member function. This determines, what data is returned as the roles of the user,
   * from the authorization provider. There are currently two possibilities for this:
   * <ul>
   *   <li>{@code getMemberObjects}, which uses both the groups and the roles where the user is a member (transitively).
   *       This is the default.</li>
   *   <li>{@code getMemberGroups}, which only uses the groups where the user is a member (transitively).</li>
   * </ul>
   * @param memberFunction
   */
  public void setMemberFunction(String memberFunction) {
    this.memberFunction = memberFunction;
  }

  /**
   * If only security-enabled groups are used.
   * 
   * @return If only security-enabled groups are used.
   * 
   * @see #setSecurityEnabledOnly(boolean)
   */
  public boolean isSecurityEnabledOnly() {
    return this.securityEnabledOnly;
  }

  /**
   * Set if only security-enabled groups are used. This affects the roles of the user, as returned
   * by the authorization provider, which include the groups where the users is a member.<p>
   * 
   * If set to {@code false} (which is the default), then all groups where the user is a member are returned.
   * If set to {@code true} then only security groups are returned.
   * 
   * @param securityEnabledOnly If only security-enabled groups are used.
   */
  public void setSecurityEnabledOnly(boolean securityEnabledOnly) {
    this.securityEnabledOnly = securityEnabledOnly;
  }

  /**
   * Get the client ID used for authentication.
   * 
   * @return The client ID.
   * 
   * @see #setAuthenticationClientId(String)
   */
  public String getAuthenticationClientId() {
    return this.authenticationClientId;
  }

  /**
   * Set the client ID used for authentication.
   * This is configured in the Azure management console for the directory.
   * A "native" type application must be used, this is the client ID of the application.
   * 
   * @param authenticationClientId The client ID.
   */
  public void setAuthenticationClientId(String authenticationClientId) {
    this.authenticationClientId = authenticationClientId;
  }

  /**
   * Get the client ID used for authorization.
   * 
   * @return The client ID.
   * 
   * @see #setAuthorizationClientId(String)
   */
  public String getAuthorizationClientId() {
    return this.authorizationClientId;
  }

  /**
   * Set the client ID used for authorization.
   * This is configured in the Azure management console for the directory.
   * A "web" type application must be used, this is the client ID of the application.
   * 
   * @param authorizationClientId The client ID.
   */
  public void setAuthorizationClientId(String authorizationClientId) {
    this.authorizationClientId = authorizationClientId;
  }

  /**
   * Get the client secret key used for authorization.
   * 
   * @return The client secret.
   * 
   * @see #setAuthorizationClientSecret(String)
   */
  public String getAuthorizationClientSecret() {
    return this.authorizationClientSecret;
  }

  /**
   * Set the client secret key used for authorization.
   * This is configured in the Azure management console for the directory.
   * A "web" type application must be used. You must generate a client secret for the application.
   * Note that client secret keys are valid only for 1 or 2 years, and must thus be re-configured
   * periodically.
   * 
   * @param authorizationClientSecret The client secret.
   */
  public void setAuthorizationClientSecret(String authorizationClientSecret) {
    this.authorizationClientSecret = authorizationClientSecret;
  }
}