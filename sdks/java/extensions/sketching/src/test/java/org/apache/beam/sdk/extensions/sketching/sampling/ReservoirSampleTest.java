/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.extensions.sketching.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReservoirSampleTest {
  @Rule public final transient TestPipeline tp = TestPipeline.create();

  @Test
  public void simpleTypeSample() {
    final int samples = 1000;
    final int size = 100;
    List<String> stream = new ArrayList<>();
    for (int i = 0; i < samples; i++) {
      stream.add("word " + i);
    }
    Collections.shuffle(stream);

    PCollection<Iterable<String>> p =
        tp.apply(Create.of(stream)).apply(ReservoirSample.<String>globally().withSize(size));
    PAssert.that(p);
    tp.run();
  }
}
