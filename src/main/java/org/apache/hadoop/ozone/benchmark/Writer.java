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
package org.apache.hadoop.ozone.benchmark;

import org.apache.hadoop.hdds.client.ReplicationConfig;
import org.apache.hadoop.hdds.client.ReplicationFactor;
import org.apache.hadoop.hdds.client.ReplicationType;
import org.apache.hadoop.ozone.client.OzoneBucket;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

abstract class Writer {
  static class KeyDescriptor {
    private final int index;
    private final String path;
    private final CompletableFuture<Boolean> writeFuture;
    private final CompletableFuture<Boolean> verifyFuture = new CompletableFuture<>();

    KeyDescriptor(String path, int index, CompletableFuture<Boolean> writeFuture) {
      this.index = index;
      this.path = path;
      this.writeFuture = writeFuture;
    }

    int getIndex() {
      return index;
    }

    String getPath() {
      return path;
    }

    boolean joinWriteFuture() {
      return writeFuture.join();
    }

    boolean joinVerifyFuture() {
      return verifyFuture.join();
    }

    void completeVerifyFuture(boolean b) {
      Print.ln(Benchmark.Op.VERIFY,  "Is " + this + " correctly written? " + b);
      verifyFuture.complete(b);
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "-" + index;
    }
  }

  static final ReplicationConfig REPLICATION_CONFIG = ReplicationConfig.fromTypeAndFactor(
      ReplicationType.RATIS, ReplicationFactor.THREE);

  private final List<String> paths;

  Writer(List<String> paths) {
    this.paths = paths;
  }

  List<String> getPaths() {
    return paths;
  }

  String getPath(int i) {
    return paths.get(i);
  }

  abstract Writer init(long fileSize, OzoneBucket bucket) throws IOException;

  abstract List<KeyDescriptor> write(long fileSize, int chunkSize, ExecutorService executor);

  CompletableFuture<Boolean> writeAsync(Object name, Supplier<Boolean> writeMethod, ExecutorService executor) {
    final Instant start = Instant.now();
    Print.ln(this, "Start writing to " + name);
    return CompletableFuture.supplyAsync(writeMethod, executor)
        .whenComplete((b, e) -> {
          if (Optional.ofNullable(b).orElse(Boolean.FALSE)) {
            Print.elapsed(this + ": Successfully wrote to " + name, start);
          }
        });
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
