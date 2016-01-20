/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.testautomation.spi;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class TestAutomationException extends ActionException{

	
	private static final String TEST_AUTOMATION_EXCEPTION_KEY = "testautomation.exceptions.generic";
	private static final long serialVersionUID = 6313915492631424474L;

	public TestAutomationException() {
		super();
	}

	public TestAutomationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TestAutomationException(String message) {
		super(message);
	}

	public TestAutomationException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getI18nKey() {
		return TEST_AUTOMATION_EXCEPTION_KEY;
	}
	
}
