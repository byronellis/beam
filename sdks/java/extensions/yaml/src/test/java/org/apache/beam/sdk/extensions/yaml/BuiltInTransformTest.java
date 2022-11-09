package org.apache.beam.sdk.extensions.yaml;

import org.apache.beam.sdk.annotations.Internal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BuiltInTransformTest {

    @Test
    public void testFlatten() {
        YAMLTransform.yaml("main\n- flatten");
    }

}
