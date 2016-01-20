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
package org.squashtest.tm.domain.search;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.bridge.StringBridge;

public class CollectionSizeBridge implements StringBridge {
	
	private static final int EXPECTED_LENGTH = 7;
	
	private String padRawValue(String rawValue){
		return StringUtils.leftPad(rawValue, EXPECTED_LENGTH, '0');
	}
	
	@Override
	public String objectToString(Object value) {
		Collection<?> collection = (Collection<?>) value;
		return padRawValue(Integer.toString(collection.size()));
	}
}

