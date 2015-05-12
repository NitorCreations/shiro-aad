package com.nitorcreations.willow.shiro.aad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.gson.Gson;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

/**
 * Realm for authenticating and authorizing against Azure Active Directory (AAD).
 * 
 * @author Mikko Tommila
 */
public class AADRealm extends AuthorizingRealm {

  private static final String API_VERSION = "1.5";

  private AADRealmConfig config;

  private AuthenticationContext authenticationContext;
  private ClientCredential credential;

  private Gson gson;

  private String bearerToken;
  private long bearerTokenExpiration;

  /**
   * Default constructor.
   */
  public AADRealm() {
    // Nothing here
  }

  /**
   * Constructor with configuration.
   * 
   * @param config The configuration.
   */
  public AADRealm(AADRealmConfig config) {
    setConfig(config);
  }

  /**
   * Set the configuration.
   * 
   * @param config The configuration.
   */
  public void setConfig(AADRealmConfig config) {
    this.config = config;
  }

  @Override
  protected void onInit() {
    if (this.config == null) {
      throw new IllegalStateException("Configuration not set");
    }
    try {
      this.authenticationContext = new AuthenticationContext(this.config.getAuthority() + this.config.getTenant(), false, SynchronousExecutorService.INSTANCE);
    } catch (MalformedURLException mue) {
      throw new ShiroException("Error initializing realm", mue);
    }
    this.credential = new ClientCredential(this.config.getAuthorizationClientId(), this.config.getAuthorizationClientSecret());
    this.gson = new Gson();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
    for (Object principal : principals.fromRealm(getName())) {
      String username = principal.toString();
      List<String> ids;
      try {
        ids = getDirectoryObjectIds(username);
      } catch (IOException | InterruptedException | ExecutionException e) {
        throw new ShiroException("Error accessing authorization service", e);
      }
      for (String id : ids) {
        authorizationInfo.addRole(id);
      }
    }
    return authorizationInfo;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    if (!(token instanceof UsernamePasswordToken)) {
      // Token type is not supported by this realm
      return null;
    }
    UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
    String username = usernamePasswordToken.getUsername();
    char[] password = usernamePasswordToken.getPassword();
    try {
      this.authenticationContext.acquireToken(this.config.getGraphResource(), this.config.getAuthenticationClientId(), username + '@' + this.config.getTenant(), escape(new String(password)), null).get();
    } catch (InterruptedException | ExecutionException e) {
      if (e.getCause() instanceof com.microsoft.aad.adal4j.AuthenticationException) {
        // Invalid username or password
        return null;
      }
      throw new AuthenticationException("Error accessing authentication service", e);
    }
    SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(username, password, getName());
    return authenticationInfo;
  }

  private synchronized String getBearerToken() throws InterruptedException, ExecutionException {
    if (System.currentTimeMillis() >= this.bearerTokenExpiration - 60000) {
      AuthenticationResult result = this.authenticationContext.acquireToken(this.config.getGraphResource(), this.credential, null).get();
      this.bearerToken = result.getAccessToken();
      this.bearerTokenExpiration = System.currentTimeMillis() + result.getExpiresOn() * 1000;
    }
    return this.bearerToken;
  }

  private List<String> getDirectoryObjectIds(String username) throws IOException, InterruptedException, ExecutionException {
    URL url = new URL(this.config.getGraphResource() + this.config.getTenant() + "/users/" + username + '@' + this.config.getTenant() + '/' + this.config.getMemberFunction()  + "?api-version=" + API_VERSION);
    String requestBody = "{\"securityEnabledOnly\":" + this.config.isSecurityEnabledOnly() + '}';
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Authorization", "Bearer " + getBearerToken());
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length()));
    connection.setDoInput(true);
    connection.setDoOutput(true);
    OutputStream out = connection.getOutputStream();
    out.write(requestBody.getBytes());
    out.flush();
    InputStream in = connection.getInputStream();
    StringWriter buffer = new StringWriter();
    int b;
    while ((b = in.read()) >= 0) {
      buffer.write(b);
    }
    String responseBody = buffer.toString();
    if (connection.getResponseCode() != 200) {
      throw new IOException("Received " + connection.getResponseCode() + ' ' + connection.getResponseMessage() + ": " + responseBody);
    }
    IdsResponse idsResponse = this.gson.fromJson(responseBody, IdsResponse.class);
    return idsResponse.getIds();
  }

  private String escape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
