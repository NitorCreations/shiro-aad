package com.nitorcreations.willow.shiro.aad;

import java.util.List;

/**
 * Deserialized AAD list of IDs response.
 * 
 * @author Mikko Tommila
 */
public class IdsResponse {

  private List<String> value;

  /**
   * Get the IDs (of groups and/or roles).
   * 
   * @return The list of identifiers.
   */
  public List<String> getIds() {
    return this.value;
  }
}
