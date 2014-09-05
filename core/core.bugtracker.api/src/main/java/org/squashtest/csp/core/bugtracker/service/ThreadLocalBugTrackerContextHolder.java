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
package org.squashtest.csp.core.bugtracker.service;

import org.apache.commons.lang.NullArgumentException;

/**
 * Thread-local based implementation of {@link BugTrackerContext}
 *
 * @author Gregory Fouquet
 *
 */
public class ThreadLocalBugTrackerContextHolder implements BugTrackerContextHolder {
	private final ThreadLocal<BugTrackerContext> contextHolder = new ThreadLocal<BugTrackerContext>();

	@Override
	public BugTrackerContext getContext() {
		BugTrackerContext context = contextHolder.get();

		if (context == null) {
			context = new BugTrackerContext();
			contextHolder.set(context);
		}

		return context;
	}

	@Override
	public void setContext(BugTrackerContext context) {
		if (context == null) {
			throw new NullArgumentException("context");
		}
		this.contextHolder.set(context);
	}

	@Override
	public void clearContext() {
		contextHolder.remove();
	}

}
