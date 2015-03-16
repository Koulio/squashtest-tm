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

package org.squashtest.tm.service.internal.batchimport.excel;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Coerces the string content of a cell into a list. Items are supposed to be
 * separated by a pipe by default.
 *
 * @author Gregory Fouquet
 *
 */
public class OptionalStringArrayCellCoercer extends TypeBasedCellValueCoercer<String[]> {
	/**
	 * Default list separator.
	 */
	private static final char DEFAULT_SEPARATOR = '|';
	private static final String[] EMPTY = {};
	/**
	 * Instance of {@link OptionalStringArrayCellCoercer} using default
	 * separator.
	 */
	public static final OptionalStringArrayCellCoercer INSTANCE = new OptionalStringArrayCellCoercer();
	/**
	 * List separator for this object.
	 */
	private final char separator;

	/**
	 * Creates a coercer with default separator.
	 *
	 * Create another constructor if you need another separator
	 */
	protected OptionalStringArrayCellCoercer() {
		super();
		separator = DEFAULT_SEPARATOR;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceBlankCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected String[] coerceBlankCell(Cell cell) {
		return EMPTY;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.TypeBasedCellValueCoercer#coerceStringCell(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	protected String[] coerceStringCell(Cell cell) {
		String val = cell.getStringCellValue();
		if (val != null) {
			// Rem : `String.split("", "|") == [""]` which is not what we want. Hence, `StringUtils`
			return StringUtils.split(val, separator);
		}
		return EMPTY;
	}
}
