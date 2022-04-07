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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class Print {
  static private final Instant START = Instant.now();
  static private final AtomicInteger ERROR_COUNT = new AtomicInteger();

  static void printShutdownMessage() {
    Print.ln("SHUTDOWN", "--------------------------------------");
    Print.ln("SHUTDOWN", Duration.between(START, Instant.now()));
    Print.ln("SHUTDOWN", "ERROR_COUNT = " + ERROR_COUNT);
  }

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(Print::printShutdownMessage));
  }


  static synchronized void ln(Object name, Object message) {
    System.out.println(format(name, message));
  }

  static synchronized void error(Object name, Object message) {
    System.err.println("ERROR " + format(name, message));
  }

  static synchronized void error(Object name, Object message, Throwable e) {
    ERROR_COUNT.getAndIncrement();
    error(name, message);
    e.printStackTrace();
  }

  static String format(Object name, Object message) {
    if (message instanceof Duration) {
      message = duration2String((Duration) message);
    }
    return String.format("%s: %s", name, message);
  }

  static String duration2String(Duration duration) {
    final long millis = duration.toMillis();
    return millis < 1000? millis + "ms": millis + "ms (" + duration.toString().substring(2) + ")";
  }

  static List<String> parseCommaSeparatedString(String commaSeparated) {
    final List<String> strings = new ArrayList<>();
    for(StringTokenizer t = new StringTokenizer(commaSeparated, ",;"); t.hasMoreTokens(); ) {
      strings.add(t.nextToken().trim());
    }
    return strings;
  }
}