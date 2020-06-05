package org.fuin.kcawrapper;

import static org.fuin.units4j.JandexAssert.assertThat;

import java.io.File;
import java.util.List;

import org.fuin.units4j.Units4JUtils;
import org.fuin.units4j.assertionrules.RuleMethodHasNullabilityInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class GeneralTests {

    @Test
    public void testNullabilityInfoOnAllMethods() {

        // Collect all class files
        File dir = new File("target/classes");
        List<File> classFiles = Units4JUtils.findAllClasses(dir);
        Index index = Units4JUtils.indexAllClasses(classFiles);

        // Verify that all methods make a statement if null is allowed or not
        assertThat(index).hasNullabilityInfoOnAllMethods(new RuleMethodHasNullabilityInfo());

    }

}
