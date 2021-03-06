package com.test.data.processors.converter;/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData.Record;
import org.apache.commons.lang.LocaleUtils;
import com.test.data.processors.converter.AvroRecordConverterCustom;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.controller.ControllerServiceLookup;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.Relationship;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestAvroRecordConverterCustom {
    final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    final static ProcessContext context = new ProcessContext() {
        @Override
        public PropertyValue getProperty(String s) {
            return null;
        }

        @Override
        public PropertyValue newPropertyValue(String s) {
            return null;
        }

        @Override
        public void yield() {

        }

        @Override
        public int getMaxConcurrentTasks() {
            return 0;
        }

        @Override
        public String getAnnotationData() {
            return null;
        }

        @Override
        public Map<PropertyDescriptor, String> getProperties() {
            return null;
        }

        @Override
        public String encrypt(String s) {
            return null;
        }

        @Override
        public String decrypt(String s) {
            return null;
        }

        @Override
        public ControllerServiceLookup getControllerServiceLookup() {
            return null;
        }

        @Override
        public Set<Relationship> getAvailableRelationships() {
            return null;
        }

        @Override
        public boolean hasIncomingConnection() {
            return false;
        }

        @Override
        public boolean hasNonLoopConnection() {
            return false;
        }

        @Override
        public boolean hasConnection(Relationship relationship) {
            return false;
        }

        @Override
        public boolean isExpressionLanguagePresent(PropertyDescriptor propertyDescriptor) {
            return false;
        }

        @Override
        public StateManager getStateManager() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public PropertyValue getProperty(PropertyDescriptor propertyDescriptor) {
            return null;
        }

        @Override
        public Map<String, String> getAllProperties() {
            return null;
        }
    };
    final static Map<String, String> EMPTY_MAPPING = ImmutableMap.of();
    final static String NESTED_RECORD_SCHEMA_STRING = "{\n"
            + "    \"type\": \"record\",\n"
            + "    \"name\": \"NestedInput\",\n"
            + "    \"namespace\": \"org.apache.example\",\n"
            + "    \"fields\": [\n" + "        {\n"
            + "            \"name\": \"l1\",\n"
            + "            \"type\": \"long\"\n"
            + "        },\n"
            + "        {\n" + "            \"name\": \"s1\",\n"
            + "            \"type\": \"string\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"name\": \"parent\",\n"
            + "            \"type\": [\"null\", {\n"
            + "              \"type\": \"record\",\n"
            + "              \"name\": \"parent\",\n"
            + "              \"fields\": [\n"
            + "                { \"name\": \"id\", \"type\": \"long\" },\n"
            + "                { \"name\": \"name\", \"type\": \"string\" }\n"
            + "              ]"
            + "            } ]"
            + "        }"
            + "   ] }";
    final static Schema NESTED_RECORD_SCHEMA = new Schema.Parser()
            .parse(NESTED_RECORD_SCHEMA_STRING);
    final static Schema NESTED_PARENT_SCHEMA = AvroRecordConverterCustom
            .getNonNullSchema(NESTED_RECORD_SCHEMA.getField("parent").schema());
    final static Schema UNNESTED_OUTPUT_SCHEMA = SchemaBuilder.record("Output")
            .namespace("org.apache.example").fields().requiredLong("l1")
            .requiredLong("s1").optionalLong("parentId").endRecord();

    /**
     * Tests the case where we don't use a mapping file and just map records by
     * name.
     */
    @Test
    public void testDefaultConversion() throws Exception {
        // We will convert s1 from string to long (or leave it null), ignore s2,
        // convert s3 to from string to double, convert l1 from long to string,
        // and leave l2 the same.
        Schema input = SchemaBuilder.record("Input")
                .namespace("com.cloudera.edh").fields()
                .nullableString("s1", "").requiredString("s2")
                .requiredString("s3").optionalLong("l1").requiredLong("l2")
                .endRecord();
        Schema output = SchemaBuilder.record("Output")
                .namespace("com.cloudera.edh").fields().optionalLong("s1")
                .optionalString("l1").requiredLong("l2").requiredDouble("s3")
                .endRecord();

        AvroRecordConverterCustom converter = new AvroRecordConverterCustom(input, output,
                EMPTY_MAPPING, LocaleUtils.toLocale("en_US"),context.getProperties());

        Record inputRecord = new Record(input);
        inputRecord.put("s1", null);
        inputRecord.put("s2", "blah");
        inputRecord.put("s3", "5.5");
        inputRecord.put("l1", null);
        inputRecord.put("l2", 5L);
        Record outputRecord = converter.convert(inputRecord);
        assertNull(outputRecord.get("s1"));
        assertNull(outputRecord.get("l1"));
        assertEquals(5L, outputRecord.get("l2"));
        assertEquals(5.5, outputRecord.get("s3"));

        inputRecord.put("s1", "500");
        inputRecord.put("s2", "blah");
        inputRecord.put("s3", "5.5e-5");
        inputRecord.put("l1", 100L);
        inputRecord.put("l2", 2L);
        outputRecord = converter.convert(inputRecord);
        assertEquals(500L, outputRecord.get("s1"));
        assertEquals("100", outputRecord.get("l1"));
        assertEquals(2L, outputRecord.get("l2"));
        assertEquals(5.5e-5, outputRecord.get("s3"));
    }

    /**
     * Tests the case where we want to default map one field and explicitly map
     * another.
     */
    @Test
    public void testExplicitMapping() throws Exception {
        // We will convert s1 from string to long (or leave it null), ignore s2,
        // convert l1 from long to string, and leave l2 the same.
        Schema input = NESTED_RECORD_SCHEMA;
        Schema parent = NESTED_PARENT_SCHEMA;
        Schema output = UNNESTED_OUTPUT_SCHEMA;
        Map<String, String> mapping = ImmutableMap.of("parent.id", "parentId");

        AvroRecordConverterCustom converter = new AvroRecordConverterCustom(input, output,
                mapping,context.getProperties());

        Record inputRecord = new Record(input);
        inputRecord.put("l1", 5L);
        inputRecord.put("s1", "1000");
        Record parentRecord = new Record(parent);
        parentRecord.put("id", 200L);
        parentRecord.put("name", "parent");
        inputRecord.put("parent", parentRecord);
        Record outputRecord = converter.convert(inputRecord);
        assertEquals(5L, outputRecord.get("l1"));
        assertEquals(1000L, outputRecord.get("s1"));
        assertEquals(200L, outputRecord.get("parentId"));
    }

    /**
     * Tests the case where we try to convert a string to a long incorrectly.
     */
    @Test(expected = AvroRecordConverterCustom.AvroConversionException.class)
    public void testIllegalConversion() throws Exception {
        // We will convert s1 from string to long (or leave it null), ignore s2,
        // convert l1 from long to string, and leave l2 the same.
        Schema input = SchemaBuilder.record("Input")
                .namespace("com.cloudera.edh").fields()
                .nullableString("s1", "").requiredString("s2")
                .optionalLong("l1").requiredLong("l2").endRecord();
        Schema output = SchemaBuilder.record("Output")
                .namespace("com.cloudera.edh").fields().optionalLong("s1")
                .optionalString("l1").requiredLong("l2").endRecord();

        AvroRecordConverterCustom converter = new AvroRecordConverterCustom(input, output,
                EMPTY_MAPPING,context.getProperties());

        Record inputRecord = new Record(input);
        inputRecord.put("s1", "blah");
        inputRecord.put("s2", "blah");
        inputRecord.put("l1", null);
        inputRecord.put("l2", 5L);
        converter.convert(inputRecord);
    }

    @Test
    public void testGetUnmappedFields() throws Exception {
        Schema input = SchemaBuilder.record("Input")
                .namespace("com.cloudera.edh").fields()
                .nullableString("s1", "").requiredString("s2")
                .optionalLong("l1").requiredLong("l2").endRecord();
        Schema output = SchemaBuilder.record("Output")
                .namespace("com.cloudera.edh").fields().optionalLong("field")
                .endRecord();

        // Test the case where the field isn't mapped at all.
        AvroRecordConverterCustom converter = new AvroRecordConverterCustom(input, output,
                EMPTY_MAPPING,context.getProperties());
        assertEquals(ImmutableList.of("field"), converter.getUnmappedFields());

        // Test the case where we tried to map from a non-existent field.
        converter = new AvroRecordConverterCustom(input, output, ImmutableMap.of(
                "nonExistentField", "field"),context.getProperties());
        assertEquals(ImmutableList.of("field"), converter.getUnmappedFields());

        // Test the case where we tried to map from a non-existent record.
        converter = new AvroRecordConverterCustom(input, output, ImmutableMap.of(
                "parent.nonExistentField", "field"),context.getProperties());
        assertEquals(ImmutableList.of("field"), converter.getUnmappedFields());

        // Test a valid case
        converter = new AvroRecordConverterCustom(input, output, ImmutableMap.of(
                "l2", "field"),context.getProperties());
        assertEquals(Collections.EMPTY_LIST, converter.getUnmappedFields());
    }
}
