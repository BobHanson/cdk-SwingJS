/* $Revision: 7635 $ $Author: egonw $ $Date: 2007-01-04 18:32:54 +0100 (Thu, 04 Jan 2007) $
 * 
 * Copyright (C) 2004-2007  Egon Willighagen <egonw@users.sf.net>
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA. 
 */
package org.openscience.cdk.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TestSuite that uses tests wether all public methods in the standard
 * module are tested. Unlike Emma, it does not test that all code is
 * tested, just all methods.
 *
 * @cdk.module test-standard
 */
public class StandardCoverageTest extends CoverageTest {

    private final static String CLASS_LIST = "standard.javafiles";
    
    public StandardCoverageTest(String name) {
        super(name);
    }

    protected void setUp() {
        super.setUp();
        try {
            super.loadClassList(CLASS_LIST);
        } catch (Exception exception) {
        	exception.printStackTrace();
            fail("Could not load classes to test: " + exception.getMessage());
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(StandardCoverageTest.class);
        return suite;
    }

    public void testCoverage() {
        assertTrue(super.runCoverageTest());
    }

}
