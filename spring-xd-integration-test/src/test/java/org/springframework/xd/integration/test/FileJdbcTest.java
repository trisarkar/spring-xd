/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.integration.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.xd.test.fixtures.FileJdbcJob;
import org.springframework.xd.test.fixtures.JdbcSink;


/**
 * Verifies that this job will read the specified file and place the results into the database.
 *
 * @author Glenn Renfro
 */
public class FileJdbcTest extends AbstractIntegrationTest {

	private final static String DEFAULT_FILE_NAME = "filejdbctest";

	private JdbcSink jdbcSink;

	private String tableName;

	/**
	 * Removes the table created from a previous test.
	 */
	@Before
	public void initialize() {
		jdbcSink = sinks.jdbc();
		tableName = "filejdbctest";
		jdbcSink.tableName(tableName);
		cleanup();
	}

	/**
	 * Verifies that fileJdbcJob has written the test data from a file to the table.
	 *
	 */
	@Test
	public void testFileJdbcJob() {
		String data = UUID.randomUUID().toString();
		jdbcSink.getJdbcTemplate().getDataSource();
		FileJdbcJob job = jobs.fileJdbcJob();
		//Create a stream that writes to a file.  This file will be used by the job.
		stream("dataSender", "trigger --payload='" + data + "'" + XD_DELIMETER
				+ sinks.file(FileJdbcJob.DEFAULT_DIRECTORY, DEFAULT_FILE_NAME).toDSL("REPLACE", "true"), WAIT_TIME);
		job(job.toDSL());
		jobLaunch();
		String query = String.format("SELECT data FROM %s", tableName);
		assertEquals(
				data,
				jdbcSink.getJdbcTemplate().queryForObject(query, String.class));
	}

	/**
	 * Being a good steward of the database remove the result table from the database.
	 */
	@After
	public void cleanup() {
		if (jdbcSink != null) {
			jdbcSink.dropTable(tableName);
		}
	}
}
