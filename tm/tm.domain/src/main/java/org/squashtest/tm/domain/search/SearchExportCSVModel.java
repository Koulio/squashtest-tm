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

import java.util.Iterator;
import java.util.List;
/**
 * 
 * Used to Export the result of an advanced Search . This model id not supposed to be importable.
 *
 */
public interface SearchExportCSVModel {

	char getSeparator();

	void setSeparator(char separator);

	Row getHeader();

	Iterator<Row> dataIterator();


	// ********** interfaces *************


	public interface Row{
		/**
		 * return a separator-separated list of cell values
		 */
		String toString();

		List<Cell> getCells();
	}



	public interface Cell{
		String getValue();
	}
}