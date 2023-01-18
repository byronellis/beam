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

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.datasketches.sampling.ReservoirItemsSketch;
import org.apache.datasketches.sampling.ReservoirItemsUnion;

public final class ReservoirSampling {

  private static class RetrieveSample {
    private static <InputT> DoFn<ReservoirItemsSketch,InputT> globally() {
      public void processElement(DoFn.ProcessContext c) {
        ReservoirItemsSketch element = c.element();
        for(InputT item : element.getElements()) {
          c.output(item);
        }
      }

    }
  }

  public static class ReserviorSampleFn<InputT>
      extends Combine.CombineFn<
          InputT, ReservoirItemsSketch<InputT>, ReservoirItemsSketch<InputT>> {

    final int size;

    public ReserviorSampleFn(int size) {
      this.size = size;
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
    public ReservoirItemsSketch<InputT> extractOutput(ReservoirItemsSketch<InputT> accumulator) {
      return accumulator;
    }
  }
}
