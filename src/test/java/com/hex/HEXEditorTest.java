package com.hex;

import junit.framework.TestCase;

public class HEXEditorTest extends TestCase {

    /**
     * Tests if HEXEditor fields init properly
     */
    public void testFieldsInitOnCreateHexEditor() {
        HEXEditor hexEditor = new HEXEditor();
        assertNotNull(hexEditor.getMainJFrame());
        assertNull(hexEditor.getRaf());
        assertNull(hexEditor.getMainTable());
    }


}