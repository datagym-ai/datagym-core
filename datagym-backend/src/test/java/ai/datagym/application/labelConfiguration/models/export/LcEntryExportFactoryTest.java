package ai.datagym.application.labelConfiguration.models.export;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntryFreeText;
import ai.datagym.application.labelConfiguration.entity.classification.LcEntrySelect;
import ai.datagym.application.labelConfiguration.entity.geometry.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class LcEntryExportFactoryTest {

    @Test
    public void cast_Point() {
        //Given
        final LcEntryPoint entry = new LcEntryPoint();
        entry.setType(LcEntryType.POINT);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportGeometry);
    }

    @Test
    public void cast_Line() {
        //Given
        final LcEntryLine entry = new LcEntryLine();
        entry.setType(LcEntryType.LINE);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportGeometry);
    }

    @Test
    public void cast_Polygon() {
        //Given
        final LcEntryPolygon entry = new LcEntryPolygon();
        entry.setType(LcEntryType.POLYGON);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportGeometry);
    }

    @Test
    public void cast_Rectangle() {
        //Given
        final LcEntryRectangle entry = new LcEntryRectangle();
        entry.setType(LcEntryType.RECTANGLE);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportGeometry);
    }

    @Test
    public void cast_Segmentation() {
        //Given
        final LcEntryImageSegmentation entry = new LcEntryImageSegmentation();
        entry.setType(LcEntryType.IMAGE_SEGMENTATION);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportGeometry);
    }

    @Test
    public void cast_Geometry() {
        //Given
        final LcEntryImageSegmentation entry = new LcEntryImageSegmentation();
        entry.setType(LcEntryType.IMAGE_SEGMENTATION);
        entry.setColor("color");
        entry.setShortcut("shortcut");

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertTrue(export instanceof LcEntryExportGeometry);
        assertEquals(entry.getColor(), ((LcEntryExportGeometry) export).getColor());
        assertEquals(entry.getShortcut(), ((LcEntryExportGeometry) export).getShortcut());
    }

    @Test
    public void cast_Text() {
        //Given
        final LcEntryFreeText entry = new LcEntryFreeText();
        entry.setType(LcEntryType.FREETEXT);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");
        entry.setMaxLength(42);

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportFreeText);
        assertEquals(entry.getMaxLength(), ((LcEntryExportFreeText) export).getMaxLength());
    }

    @Test
    public void cast_Select() {
        //Given
        final LcEntrySelect entry = new LcEntrySelect();
        entry.setType(LcEntryType.SELECT);
        entry.setEntryValue("entryValue");
        entry.setEntryKey("key");
        entry.setOptions(null);

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(entry);

        //Then
        assertNotNull(export);
        assertEquals(entry.getEntryKey(), export.getEntryKey());
        assertEquals(entry.getEntryValue(), export.getEntryValue());

        assertTrue(export instanceof LcEntryExportOption);
        assertNull(((LcEntryExportOption) export).getOptions());
    }

    @Test
    public void cast_test_children() {
        //Given

        final LcEntryLine child1 = new LcEntryLine();
        child1.setType(LcEntryType.LINE);

        final LcEntrySelect child2 = new LcEntrySelect();
        child2.setType(LcEntryType.SELECT);

        final LcEntryRectangle parent = new LcEntryRectangle();
        parent.setType(LcEntryType.RECTANGLE);
        parent.setChildren(List.of(child1, child2));

        //When
        final LcEntryExport export = LcEntryExportFactory.cast(parent);

        //Then
        assertNotNull(export);
        assertTrue(export instanceof LcEntryExportGeometry);
        assertNotNull(export.getChildren());
        assertEquals(parent.getChildren().size(), export.getChildren().size());

        for (final LcEntryExport exportChild: export.getChildren()) {
            assertTrue(exportChild.getType() == LcEntryType.SELECT || exportChild.getType() == LcEntryType.LINE);
            if (exportChild.getType() == LcEntryType.SELECT) {
                assertTrue(exportChild instanceof LcEntryExportOption);
            }
            if (exportChild.getType() == LcEntryType.LINE) {
                assertTrue(exportChild instanceof LcEntryExportGeometry);
            }
        }
    }
}