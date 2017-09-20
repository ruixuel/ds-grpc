/*
 * Copyright 2017, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.internal.testing;

import io.grpc.ClientStreamTracer;
import io.grpc.Status;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link ClientStreamTracer} suitable for testing.
 */
public class TestClientStreamTracer extends ClientStreamTracer implements TestStreamTracer {
  private final TestBaseStreamTracer delegate = new TestBaseStreamTracer();
  protected final AtomicBoolean outboundHeadersCalled = new AtomicBoolean();
  protected final AtomicBoolean inboundHeadersCalled = new AtomicBoolean();

  @Override
  public void await() throws InterruptedException {
    delegate.await();
  }

  @Override
  public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
    return delegate.await(timeout, timeUnit);
  }

  /**
   * Returns if {@link ClientStreamTracer#inboundHeaders} has been called.
   */
  public boolean getInboundHeaders() {
    return inboundHeadersCalled.get();
  }

  /**
   * Returns if {@link ClientStreamTracer#outboundHeaders} has been called.
   */
  public boolean getOutboundHeaders() {
    return outboundHeadersCalled.get();
  }

  @Override
  public Status getStatus() {
    return delegate.getStatus();
  }

  @Override
  public long getInboundWireSize() {
    return delegate.getInboundWireSize();
  }

  @Override
  public long getInboundUncompressedSize() {
    return delegate.getInboundUncompressedSize();
  }

  @Override
  public long getOutboundWireSize() {
    return delegate.getOutboundWireSize();
  }

  @Override
  public long getOutboundUncompressedSize() {
    return delegate.getOutboundUncompressedSize();
  }

  @Override
  public String nextOutboundEvent() {
    return delegate.nextOutboundEvent();
  }

  @Override
  public String nextInboundEvent() {
    return delegate.nextInboundEvent();
  }

  @Override
  public void outboundWireSize(long bytes) {
    delegate.outboundWireSize(bytes);
  }

  @Override
  public void inboundWireSize(long bytes) {
    delegate.inboundWireSize(bytes);
  }

  @Override
  public void outboundUncompressedSize(long bytes) {
    delegate.outboundUncompressedSize(bytes);
  }

  @Override
  public void inboundUncompressedSize(long bytes) {
    delegate.inboundUncompressedSize(bytes);
  }

  @Override
  public void streamClosed(Status status) {
    delegate.streamClosed(status);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void inboundMessage() {
    delegate.inboundMessage();
  }

  @Override
  public void inboundMessage(int seqNo) {
    delegate.inboundMessage(seqNo);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void outboundMessage() {
    delegate.outboundMessage();
  }

  @Override
  public void outboundMessage(int seqNo) {
    delegate.outboundMessage(seqNo);
  }

  @Override
  public void outboundMessageSent(int seqNo, long optionalWireSize, long optionalUncompressedSize) {
    delegate.outboundMessageSent(seqNo, optionalWireSize, optionalUncompressedSize);
  }

  @Override
  public void inboundMessageRead(int seqNo, long optionalWireSize, long optionalUncompressedSize) {
    delegate.inboundMessageRead(seqNo, optionalWireSize, optionalUncompressedSize);
  }

  @Override
  public void outboundHeaders() {
    if (!outboundHeadersCalled.compareAndSet(false, true)
        && delegate.failDuplicateCallbacks.get()) {
      throw new AssertionError("outboundHeaders called more than once");
    }
  }

  @Override
  public void inboundHeaders() {
    if (!inboundHeadersCalled.compareAndSet(false, true) && delegate.failDuplicateCallbacks.get()) {
      throw new AssertionError("inboundHeaders called more than once");
    }
  }
}
