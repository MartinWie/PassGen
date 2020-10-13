package de.mw.passgen

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class PassgenApplicationTest {

	@Test
	fun contextLoads() {
	}

	@Ignore
	@Test
	fun testIfTestsCanFail(){
		Assert.assertEquals(2,3)
	}

	// add first handler (part which handles the http logic) + service
	// choose http client for testing handlers + normal tests for services
}
