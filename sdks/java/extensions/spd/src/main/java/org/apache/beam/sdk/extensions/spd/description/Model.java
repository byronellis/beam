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
package org.apache.beam.sdk.extensions.spd.description;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;

@SuppressFBWarnings
public class Model {
  @Nullable
  @JsonSetter(nulls = Nulls.FAIL)
  public String name;

  public String getName() {
    return name == null ? "" : name;
  }

  @Nullable public String description;

  public String getDescription() {
    return description == null ? "" : description;
  }

  @JsonSetter(nulls = Nulls.AS_EMPTY)
  public List<Column> columns = Arrays.asList();

  @Nullable
  @JsonSetter(nulls = Nulls.SKIP)
  public String type = "file";

  public String getType() {
    return type == null ? "file" : type;
  }

  @Nullable
  @JsonSetter(nulls = Nulls.SKIP)
  public String input = "";

  public String getInput() {
    return input == null ? "" : input;
  }

  @Nullable
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  public ObjectNode config;

  public @Nullable ObjectNode getConfig() {
    return config;
  }
}