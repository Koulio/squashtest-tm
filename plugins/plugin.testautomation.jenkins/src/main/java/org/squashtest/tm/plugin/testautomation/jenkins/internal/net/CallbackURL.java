/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Ugly trick. Booh.
 * 
 * @author bsiri
 *
 */

@Component
public final class CallbackURL {
	private static CallbackURL instance;
	private static final String PROPERTY_NAME = "tm.test.automation.server.callbackurl";

	private String strURL = null;

	private CallbackURL() {
		super();
		instance = this;
	}

	public String getValue() {
		return strURL;
	}

	String getConfPropertyName() {
		return PROPERTY_NAME;
	}

	@Value("${" + PROPERTY_NAME + "}")
	void setURL(String url) {
		strURL = url;
	}

	/**
	 * @return the instance
	 */
	public static CallbackURL getInstance() {
		return instance;
	}

}
