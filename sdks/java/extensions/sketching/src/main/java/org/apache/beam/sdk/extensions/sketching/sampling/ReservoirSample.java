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

import com.google.auto.value.AutoValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.CustomCoder;
import org.apache.beam.sdk.extensions.sketching.impl.CoderArraySerDe;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.values.PCollection;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.sampling.ReservoirItemsSketch;
import org.apache.datasketches.sampling.ReservoirItemsUnion;

public final class ReservoirSample {

  public static <InputT> GlobalSample<InputT> globally() {
    return GlobalSample.<InputT>builder().build();
  }

  @AutoValue
  public abstract static class GlobalSample<InputT>
      extends PTransform<PCollection<InputT>, PCollection<Iterable<InputT>>> {
    abstract int size();

    abstract Builder<InputT> toBuilder();

    public static <InputT> Builder<InputT> builder() {
      return new AutoValue_ReservoirSample_GlobalSample.Builder<InputT>().setSize(1);
    }

    @AutoValue.Builder
    abstract static class Builder<InputT> {

      public abstract Builder<InputT> setSize(int value);

      abstract GlobalSample<InputT> build();
    }

    public GlobalSample<InputT> withSize(int size) {
      return toBuilder().setSize(size).build();
    }

    @Override
    public PCollection<Iterable<InputT>> expand(PCollection<InputT> input) {
      return input.apply(
          "Compute Reservoir Sample", Combine.globally(ReservoirSampleFn.create(size())));
    }
  }

  public static class ReservoirSampleFn<InputT>
      extends Combine.CombineFn<InputT, ReservoirItemsSketch<InputT>, Iterable<InputT>> {

    final int size;

    public ReservoirSampleFn(int size) {
      this.size = size;
    }

    public static <InputT> ReservoirSampleFn<InputT> create(int size) {
      return new ReservoirSampleFn<>(size);
    }

    @Override
    public ReservoirItemsSketch<InputT> createAccumulator() {
      return ReservoirItemsSketch.newInstance(size);
    }

    @Override
    public ReservoirItemsSketch<InputT> addInput(
        ReservoirItemsSketch<InputT> mutableAccumulator, InputT input) {
      mutableAccumulator.update(input);
      return mutableAccumulator;
    }

    @Override
    public ReservoirItemsSketch<InputT> mergeAccumulators(
        Iterable<ReservoirItemsSketch<InputT>> accumulators) {
      ReservoirItemsUnion<InputT> union = ReservoirItemsUnion.newInstance(size);
      for (ReservoirItemsSketch<InputT> sketch : accumulators) {
        union.update(sketch);
      }
      return union.getResult();
    }

    @Override
    public Iterable<InputT> extractOutput(ReservoirItemsSketch<InputT> accumulator) {
      return Arrays.asList(accumulator.getSamples());
    }

    @Override
    public void populateDisplayData(DisplayData.Builder builder) {
      super.populateDisplayData(builder);
      builder.add(DisplayData.item("size", size).withLabel("Size of the reservoir sample"));
    }
  }

  public static class ReservoirSketchCoder<InputT>
      extends CustomCoder<ReservoirItemsSketch<InputT>> {

    final Coder<InputT> coder;

    ReservoirSketchCoder(Coder<InputT> coder) {
      this.coder = coder;
    }

    public static <T> ReservoirSketchCoder<T> of(Coder<T> coder) {
      return new ReservoirSketchCoder<>(coder);
    }

    @Override
    public void encode(ReservoirItemsSketch<InputT> value, OutputStream outStream)
        throws CoderException, IOException {
      outStream.write(value.toByteArray(CoderArraySerDe.of(coder)));
    }

    @Override
    public ReservoirItemsSketch<InputT> decode(InputStream inStream)
        throws CoderException, IOException {
      byte[] bytes = new byte[inStream.available()];
      inStream.read(bytes);
      return ReservoirItemsSketch.heapify(Memory.wrap(bytes), CoderArraySerDe.of(coder));
    }
  }
}
