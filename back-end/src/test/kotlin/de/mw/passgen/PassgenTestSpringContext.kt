package de.mw.passgen

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

// Testing in spring context
@SpringBootTest(classes = [PassgenApplication::class]) // Show Spring where spring configuration is
@RunWith(SpringRunner::class)
@Transactional
abstract class PassgenTestSpringContext
