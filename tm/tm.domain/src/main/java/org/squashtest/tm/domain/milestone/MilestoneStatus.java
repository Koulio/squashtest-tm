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
package org.squashtest.tm.domain.milestone;

import org.squashtest.tm.domain.Level;

public enum MilestoneStatus implements Level {
	
	PLANNED(1), IN_PROGRESS(2), FINISHED(3), LOCKED(4);
	
	
	private static final String I18N_KEY_ROOT = "milestone.status.";

	private final int level;

	private MilestoneStatus(int level) {
		this.level = level;
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	@Override
	public int getLevel() {
		return level;
	}

}
