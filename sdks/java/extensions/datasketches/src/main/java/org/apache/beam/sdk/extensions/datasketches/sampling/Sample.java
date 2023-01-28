package org.apache.beam.sdk.extensions.datasketches.sampling;

import java.util.List;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.ImmutableList;

/** {@code PTransforms} for obtaining a sample of n elements in a {@code PCollection}.
 *
 */
@Experimental
public final class Sample {
  private static final List<TypeDescriptor<?>> SAMPLE_IMPLEMENTED_TYPES = ImmutableList.of(
      TypeDescriptors.longs(),
      TypeDescriptors.integers(),
      new TypeDescriptor<byte[]>() { }
  );




}