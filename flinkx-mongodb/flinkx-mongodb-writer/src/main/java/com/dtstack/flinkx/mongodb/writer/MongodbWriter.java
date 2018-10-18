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

package com.dtstack.flinkx.mongodb.writer;

import com.dtstack.flinkx.config.DataTransferConfig;
import com.dtstack.flinkx.config.WriterConfig;
import com.dtstack.flinkx.mongodb.Column;
import com.dtstack.flinkx.writer.DataWriter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.functions.sink.OutputFormatSinkFunction;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dtstack.flinkx.mongodb.MongodbConfigKeys.*;
import static com.dtstack.flinkx.mongodb.MongodbConfigKeys.KEY_COLLECTION;

/**
 * The writer plugin for mongodb database
 *
 * @Company: www.dtstack.com
 * @author jiangbo
 */
public class MongodbWriter extends DataWriter {

    protected String hostPorts;

    protected String username;

    protected String password;

    protected String database;

    protected String collection;

    protected List<Column> columns = new ArrayList<>();

    protected String replaceKey;

    public MongodbWriter(DataTransferConfig config) {
        super(config);

        WriterConfig writerConfig = config.getJob().getContent().get(0).getWriter();
        hostPorts = writerConfig.getParameter().getStringVal(KEY_HOST_PORTS);
        username = writerConfig.getParameter().getStringVal(KEY_USERNAME);
        password = writerConfig.getParameter().getStringVal(KEY_PASSWORD);
        database = writerConfig.getParameter().getStringVal(KEY_DATABASE);
        collection = writerConfig.getParameter().getStringVal(KEY_COLLECTION);
        mode = writerConfig.getParameter().getStringVal(KEY_MODE);
        replaceKey = writerConfig.getParameter().getStringVal(KEY_REPLACE_KEY);

        for (Object item : writerConfig.getParameter().getColumn()) {
            Map<String,String> colMap = (Map<String,String>)item;
            columns.add(new Column(colMap.get(KEY_NAME),colMap.get(KEY_TYPE),colMap.get(KEY_SPLITTER)));
        }
    }

    @Override
    public DataStreamSink<?> writeData(DataStream<Row> dataSet) {
        MongodbOutputFormatBuilder builder = new MongodbOutputFormatBuilder();

        builder.setHostPorts(hostPorts);
        builder.setUsername(username);
        builder.setPassword(password);
        builder.setDatabase(database);
        builder.setCollection(collection);
        builder.setMode(mode);
        builder.setColumns(columns);
        builder.setReplaceKey(replaceKey);

        builder.setMonitorUrls(monitorUrls);
        builder.setErrors(errors);
        builder.setDirtyPath(dirtyPath);
        builder.setDirtyHadoopConfig(dirtyHadoopConfig);
        builder.setSrcCols(srcCols);

        OutputFormatSinkFunction formatSinkFunction = new OutputFormatSinkFunction(builder.finish());
        DataStreamSink<?> dataStreamSink = dataSet.addSink(formatSinkFunction);
        dataStreamSink.name("mongodbwriter");

        return dataStreamSink;
    }
}
