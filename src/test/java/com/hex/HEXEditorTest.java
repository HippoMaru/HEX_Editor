package com.hex;

import junit.framework.TestCase;

public class HEXEditorTest extends TestCase {

    public void testCreateHexEditor() {
        HEXEditor hexEditor = new HEXEditor();
        assertNotNull(hexEditor.getMainJFrame());
        assertNull(hexEditor.getRaf());
        assertNull(hexEditor.getMainTable());
    }
}