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
package org.squashtest.tm.domain.chart;

public enum Operation {
	// @formatter:off
	NONE,
	GREATER,
	GREATER_EQUAL,
	LOWER,
	LOWER_EQUAL,
	BETWEEN,
	EQUALS,
	IN,
	LIKE,
	MIN,
	MAX,
	AVG,
	SUM,
	COUNT,
	NOT_NULL,
	NOT_EQUALS,
	BY_DAY,
	BY_WEEK,
	BY_MONTH,
	BY_YEAR;
	// @formatter:on
}
