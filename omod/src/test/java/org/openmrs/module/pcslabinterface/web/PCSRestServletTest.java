package org.openmrs.module.pcslabinterface.web;

import org.junit.Test;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

public class PCSRestServletTest extends BaseModuleWebContextSensitiveTest{

    @Test
    public void doGet_shouldDoTheRightThing() {
        HttpServletRequest request = new MockHttpServletRequest();

    }
}
