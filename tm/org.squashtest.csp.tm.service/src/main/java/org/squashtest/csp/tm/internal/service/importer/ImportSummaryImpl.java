/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.service.importer;

import org.squashtest.csp.tm.service.importer.ImportSummary;

public class ImportSummaryImpl implements ImportSummary {

	private int total=0;
	private int renamed=0;
	private int modified=0;
	private int failures=0;
	
	public ImportSummaryImpl(){
		
	}
	
	public void incrTotal(){
		total++;
	}
	
	public void incrRenamed(){
		renamed++;
	}
	
	public void incrFailures(){
		failures++;
	}
	
	public void incrModified(){
		modified++;
	}

	@Override
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public int getSuccess() {
		return total - failures;
	}


	@Override
	public int getRenamed() {
		return renamed;
	}

	public void setRenammed(int renammed) {
		this.renamed = renammed;
	}

	@Override
	public int getModified() {
		return modified;
	}

	public void setModified(int modified) {
		this.modified = modified;
	}

	@Override
	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}

	@Override
	public void add(ImportSummary summary) {
		this.total+=summary.getTotal();
		this.failures+=summary.getFailures();
		this.modified+=summary.getModified();
		this.renamed+=summary.getRenamed();
		
	}

	
	

}
