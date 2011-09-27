/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.helper;

import org.apache.commons.lang.StringUtils;

import spock.lang.Specification;


class JsonHelperTest extends Specification {
	def "should serialize a Dummy"() {
		when:
		def res = JsonHelper.serialize(new Dummy(foo: "foofoo", bar: "barbar"))
		
		then:
		StringUtils.remove(res, " ") == '{"foo":"foofoo","bar":"barbar"}'
	}
	def "should serialize a list of Dummy"() {
		given:
		def val = [new Dummy(foo: "f", bar: "b"), new Dummy(foo: "ff", bar: "bb")]
		
		when:
		def res = JsonHelper.serialize(val)
		
		then:
		StringUtils.remove(res, " ") == '[{"foo":"f","bar":"b"},{"foo":"ff","bar":"bb"}]'
	}
	def "should serialize a map"() {
		given:
		def val = ["foo" : "bar"]
		
		when:
		def res = JsonHelper.serialize(val)
		
		then:
		StringUtils.remove(res, " ") == '{"foo":"bar"}'
	}
}

class Dummy {
	String foo;
	String bar;
}
