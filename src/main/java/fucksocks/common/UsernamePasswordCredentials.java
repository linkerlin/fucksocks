/*
 * Copyright 2015-2025 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package fucksocks.common;

import java.security.Principal;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * The class <code>UsernamePasswordCredentials</code> represents an USERNAME/PASSWORD credentials.
 * Only SOCKS5 protocol supports this credentials.
 *
 * @author Youchao Feng
 * @date May 14, 2015 2:36:52 PM
 * @version 1.0
 *
 */
public class UsernamePasswordCredentials implements Credentials {

  private Socks5UserPrincipal principal;

  private String password;

  public UsernamePasswordCredentials(String username, String password) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(username),
        "Username may not be null or empty");
    this.principal = new Socks5UserPrincipal(username);
    this.password = password;
  }

  @Override
  public Principal getUserPrincipal() {
    return this.principal;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof UsernamePasswordCredentials) {
      final UsernamePasswordCredentials that = (UsernamePasswordCredentials) obj;
      if (this.principal.equals(that.principal)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.principal.hashCode();
  }

  @Override
  public String toString() {
    return this.principal.toString();
  }

}
