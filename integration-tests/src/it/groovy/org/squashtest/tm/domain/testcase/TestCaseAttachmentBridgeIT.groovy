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
package org.squashtest.tm.domain.testcase

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateDaoSpecification
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
@Transactional
class TestCaseAttachmentBridgeIT extends DbunitDaoSpecification {
	TestCaseAttachmentBridge bridge = new TestCaseAttachmentBridge()
	
	@Inject SessionFactory sessionFactory
	
	LuceneOptions lucene = Mock()
	Document doc = new Document()
	
	@DataSet("TestCaseBridgeIT.dataset.xml")
	def "should index the test case's attachemnt count"() {
		given:
		Session session = sessionFactory.currentSession
		TestCase tc = session.load(TestCase, -10L)
		
		and:
		lucene.getStore() >> Mock(Store)
		lucene.getIndex() >> Mock(Index)
		lucene.getTermVector() >> Mock(TermVector)
		
		when:
		bridge.writeFieldToDocument("foo", session, tc, doc, lucene)
		
		then:
		doc.fields.size() == 1
		doc.fields[0].name == "foo"
		doc.fields[0].fieldsData == "0000002"
	}
}
