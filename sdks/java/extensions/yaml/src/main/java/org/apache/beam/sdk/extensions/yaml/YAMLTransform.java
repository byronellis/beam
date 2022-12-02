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
package org.apache.beam.sdk.extensions.yaml;

import com.google.auto.value.AutoValue;
import java.io.InputStream;
import java.io.Reader;
import javax.annotation.Nullable;
import org.apache.beam.sdk.extensions.yaml.descriptors.Expansion;
import org.apache.beam.sdk.extensions.yaml.snakeyaml.DescriptorConstructor;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PInput;
import org.apache.beam.sdk.values.Row;
import org.yaml.snakeyaml.Yaml;

@AutoValue
@AutoValue.CopyAnnotations
public abstract class YAMLTransform extends PTransform<PInput, PCollection<Row>> {

  abstract @Nullable Iterable<Object> description();

  @Override
  public PCollection<Row> expand(PInput input) {

    // Pipeline p = input.getPipeline();
    PInput current = input;
    System.out.println("YAMLTransform Expand Start");
    for (Object o : description()) {
      if (o instanceof Expansion) {
        current = ((Expansion) o).expand(this, current);
      }
    }
    System.out.println(current.getPipeline());
    System.out.println("YAMLTransform Expand Stop");
    return (PCollection<Row>) current;
  }

  public static Yaml parser() {
    Yaml y = new Yaml(new DescriptorConstructor());
    return y;
  }

  public static YAMLTransform yaml(String yaml) {
    return builder().setDescription(parser().loadAll(yaml)).build();
  }

  public static YAMLTransform yaml(InputStream in) {
    return builder().setDescription(parser().loadAll(in)).build();
  }

  public static YAMLTransform yaml(Reader reader) {
    return builder().setDescription(parser().loadAll(reader)).build();
  }

  abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_YAMLTransform.Builder();
  }

  @AutoValue.Builder
  @AutoValue.CopyAnnotations
  public abstract static class Builder {

    public abstract Builder setDescription(Iterable<Object> description);

    public abstract YAMLTransform build();
  }
}
