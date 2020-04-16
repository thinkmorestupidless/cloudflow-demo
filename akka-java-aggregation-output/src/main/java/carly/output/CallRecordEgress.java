/*
 * Copyright (C) 2016-2020 Lightbend Inc. <https://www.lightbend.com>
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

package carly.output;

import akka.NotUsed;
import akka.kafka.ConsumerMessage.CommittableOffset;
import akka.stream.javadsl.FlowWithContext;
import akka.stream.javadsl.RunnableGraph;
import carly.data.CallRecord;
import cloudflow.akkastream.AkkaStreamlet;
import cloudflow.akkastream.AkkaStreamletLogic;
import cloudflow.akkastream.javadsl.FlowWithOffsetContext;
import cloudflow.akkastream.javadsl.RunnableGraphStreamletLogic;
import cloudflow.streamlets.StreamletShape;
import cloudflow.streamlets.avro.AvroInlet;

public class CallRecordEgress extends AkkaStreamlet {
  public AvroInlet<CallRecord> in = AvroInlet.create("in", CallRecord.class);

  private Object doPrint(final CallRecord record) {
    System.out.println(record);
    return record;
  }

  @Override public StreamletShape shape() {
    return StreamletShape.createWithInlets(in);
  }

  @Override
  public AkkaStreamletLogic createLogic() {
    return new RunnableGraphStreamletLogic(getContext()) {
      @Override
      public RunnableGraph<?> createRunnableGraph() {
        return getSourceWithOffsetContext(in)
          .via(flowWithContext())
          .to(getSinkWithOffsetContext());
      }
    };
  }

  private FlowWithContext<CallRecord,CommittableOffset,Object,CommittableOffset,NotUsed> flowWithContext() {
    return FlowWithOffsetContext.<CallRecord>create().map(metric -> doPrint(metric));
  }
}
