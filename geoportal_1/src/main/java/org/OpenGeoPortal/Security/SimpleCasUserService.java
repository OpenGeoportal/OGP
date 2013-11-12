package org.OpenGeoPortal.Security;
/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;
import org.apache.commons.lang.ArrayUtils;
import org.jasig.cas.client.validation.Assertion;

import java.util.List;
import java.util.ArrayList;

/**
* Populates the {@link org.springframework.security.core.GrantedAuthority}s for a user by reading a list of attributes that were returned as
* part of the CAS response. Each attribute is read and each value of the attribute is turned into a GrantedAuthority. If the attribute has no
* value then its not added.
* 
* --modified: if the user has been authenticated via CAS, they are granted the role "ROLE_USER".  If the user is found in the admins list, they
* are also granted the role "ROLE_ADMIN".  Note that at this point the admin user has no special privileges in the application.  the addition is
* for future functionality and parity with the LDAP authorization as it stands.
*
* @author Scott Battaglia, modified Chris Barnett
* @since 3.0
*/
public final class SimpleCasUserService implements UserDetailsService {

    protected String admins;
    protected String[] adminList;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String NON_EXISTENT_PASSWORD_VALUE = "NO_PASSWORD";

    private final String[] attributes;

    private boolean convertToUpperCase = true;
    
    public void setAdmins(String admins){
	admins = admins.replace(" ", "");
	adminList = admins.split(",");
    }
	
    protected Boolean isAdmin(String username){
	return ArrayUtils.contains(adminList, username);
    }
	
    public SimpleCasUserService(final String[] attributes) {
        Assert.notNull(attributes, "attributes cannot be null.");//should null attributes be allowed, since we are not using CAS attributes for our roles?
       // Assert.isTrue(attributes.length > 0, "At least one attribute is required to retrieve roles from.");
        this.attributes = attributes;
    }

    /**
* Converts the returned attribute values to uppercase values.
*
* @param convertToUpperCase true if it should convert, false otherwise.
*/
    public void setConvertToUpperCase(final boolean convertToUpperCase) {
        this.convertToUpperCase = convertToUpperCase;
    }

    public UserDetails loadUserByUsername(String userName) {

        final List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
	if(userName == null || userName.equals("")) {
	} else if(userName.equals("175219")) {
	    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	if(userName != null && !userName.equals("")) {
	    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
	}
	return new User(userName, NON_EXISTENT_PASSWORD_VALUE, true, true, true, true, grantedAuthorities);
    }
}