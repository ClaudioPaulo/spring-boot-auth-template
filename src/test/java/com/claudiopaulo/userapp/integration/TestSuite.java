package com.claudiopaulo.userapp.integration;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("User Management Test Suite")
@SelectPackages({
    "com.example.usermanagement.unit",
    "com.example.usermanagement.integration"
})
public class TestSuite {
}