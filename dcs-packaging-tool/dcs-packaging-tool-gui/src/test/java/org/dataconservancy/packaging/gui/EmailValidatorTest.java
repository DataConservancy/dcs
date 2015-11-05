/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.dataconservancy.packaging.gui;

import org.dataconservancy.packaging.gui.util.EmailValidator;
import org.dataconservancy.packaging.gui.util.Validator;
import org.dataconservancy.packaging.gui.util.ValidatorFactory;
import org.dataconservancy.packaging.tool.model.ValidationType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for EmailValidator
 */
public class EmailValidatorTest {


    ValidatorFactory vf = new ValidatorFactory();
    Validator ev = ValidatorFactory.getValidator(ValidationType.EMAIL);

    @Test
    public void testValidEmails() {
        assertTrue(ev.isValid("blah@blah.com"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse(ev.isValid("jack.com"));
        assertFalse(ev.isValid("@jack.com"));
    }

}