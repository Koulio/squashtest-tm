/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.report.criteria;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;

/**
 * @author Gregory
 * 
 */
public final class EmptyCriteria extends CriteriaBase implements Criteria {
	public static Criteria createEmptyCriteria(String name, InputType sourceInput) {
		return new EmptyCriteria(name, sourceInput);
	}

	/**
	 * @param name
	 * @param sourceInput
	 */
	protected EmptyCriteria(String name, InputType sourceInput) {
		super(name, sourceInput);
	}

	/**
	 * @see org.squashtest.tm.api.report.criteria.Criteria#getValue()
	 */
	@Override
	public Object getValue() {
		return Criteria.NO_VALUE;
	}

	/**
	 * @see org.squashtest.tm.api.report.criteria.Criteria#hasValue()
	 */
	@Override
	public boolean hasValue() {
		return false;
	}

}
