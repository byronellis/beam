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
package org.apache.beam.sdk.extensions.sketching.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.Coder;
import org.apache.datasketches.ArrayOfItemsSerDe;
import org.apache.datasketches.memory.Memory;

/** Class for adapting Beam coders to Datasketches SerDe model. */
@Experimental
@SuppressWarnings({"unsafe"}) // This needs to do an unsafe cast to work
public class CoderArraySerDe<T> extends ArrayOfItemsSerDe<T> {

  final Coder<T> coder;

  public CoderArraySerDe(Coder<T> coder) {
    this.coder = coder;
  }

  public static <T> CoderArraySerDe<T> of(Coder<T> coder) {
    return new CoderArraySerDe<>(coder);
  }

  @Override
  public byte[] serializeToByteArray(T[] items) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      for (T item : items) {
        coder.encode(item, bytes);
      }
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to encode item: " + e.getMessage(), e);
    }
  }

  @Override
  public T[] deserializeFromMemory(Memory mem, int numItems) {
    // Wrap the Memory object in an input stream for reading.
    InputStream memoryStream =
        new InputStream() {

          long offset = 0;

          @Override
          public int read() throws IOException {
            if (offset >= mem.getCapacity()) {
              return -1;
            }
            return mem.getByte(offset++);
          }

          @Override
          public int read(byte[] bytes, int off, int len) throws IOException {
            int bytesRead =
                offset + len > mem.getCapacity() ? (int) (mem.getCapacity() - offset) : len;
            for (int i = 0; i < bytesRead; i++) {
              bytes[off + i] = mem.getByte(offset++);
            }
            return bytesRead;
          }
        };
    try {
      T[] items = (T[]) Array.newInstance(coder.getEncodedTypeDescriptor().getRawType(), numItems);
      for (int i = 0; i < numItems; i++) {
        items[i] = coder.decode(memoryStream);
      }
      return items;
    } catch (IOException e) {
      throw new IllegalStateException("Unable to decode item: " + e.getMessage(), e);
    }
  }
}
