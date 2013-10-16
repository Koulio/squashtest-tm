/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.requirement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class RequirementVersionAttachmentBridge extends SessionFieldBridge{

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions){
		
		RequirementVersion requirement =  (RequirementVersion) value;
		requirement = (RequirementVersion) session.createCriteria(RequirementVersion.class).add(Restrictions.eq("id", requirement.getId())).uniqueResult(); //NOSONAR session is never null
		
		Field field = new Field(name, String.valueOf(requirement.getAttachmentList().size()), luceneOptions.getStore(),
		   luceneOptions.getIndex(), luceneOptions.getTermVector() );
		   field.setBoost( luceneOptions.getBoost());
		
		document.add(field);
	}
}
