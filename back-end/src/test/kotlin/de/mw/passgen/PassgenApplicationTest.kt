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

	// currently learning about spring structure, so not much to commit here
	// Handlers are the names for controller classes + Service classes for logic
	// http client for testing handlers + normal tests for services
}
