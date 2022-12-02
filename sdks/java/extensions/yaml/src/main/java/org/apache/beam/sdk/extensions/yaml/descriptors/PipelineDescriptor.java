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
package org.apache.beam.sdk.extensions.yaml.descriptors;

import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.extensions.yaml.YAMLTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PInput;
import org.apache.beam.sdk.values.Row;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

public class PipelineDescriptor implements Expansion {

  public List<FieldDescriptor> options;
  public List<Expansion> apply = new ArrayList<>();

  public static void addTypeDescription(Constructor constructor) {
    TypeDescription me = new TypeDescription(PipelineDescriptor.class, new Tag("!pipeline"));
    constructor.addTypeDescription(me);
  }

  @Override
  public PCollection<Row> expand(YAMLTransform transform, PInput input) {
    PInput out = input;

    // Apply all the expansions that we need
    for (Expansion e : apply) out = e.expand(transform, out);

    return (PCollection<Row>) out;
  }
}
