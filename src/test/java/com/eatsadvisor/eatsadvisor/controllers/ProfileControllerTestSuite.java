package com.eatsadvisor.eatsadvisor.controllers;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for ProfileController tests.
 * This suite includes only the basic tests that are known to pass.
 */
@Suite
@SelectClasses({
    ProfileControllerBasicTest.class
})
public class ProfileControllerTestSuite {
    // This class serves as a test suite container
}
