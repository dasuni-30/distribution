/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sample.proto

package io.siddhi.extension.io.grpc.proto;

public interface RequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Request)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string stringValue = 1;</code>
   */
  String getStringValue();
  /**
   * <code>string stringValue = 1;</code>
   */
  com.google.protobuf.ByteString
      getStringValueBytes();

  /**
   * <code>int32 intValue = 2;</code>
   */
  int getIntValue();

  /**
   * <code>int64 longValue = 3;</code>
   */
  long getLongValue();

  /**
   * <code>bool booleanValue = 4;</code>
   */
  boolean getBooleanValue();

  /**
   * <code>float floatValue = 5;</code>
   */
  float getFloatValue();

  /**
   * <code>double doubleValue = 6;</code>
   */
  double getDoubleValue();
}
