/*
 * Copyright 2018 chengww
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chengww.qingstor_sdk_android.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengww on 2018/12/28.
 */
public class TableEntity {

    public String tableName;
    private List<ColumnEntity> list;

    public TableEntity(String tableName) {
        this.tableName = tableName;
        list = new ArrayList<>();
    }

    public TableEntity addColumn(ColumnEntity columnEntity) {
        list.add(columnEntity);
        return this;
    }

    public String buildTableString() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName).append('(');
        for (ColumnEntity entity : list) {
            if (entity.compositePrimaryKey != null) {
                sb.append("PRIMARY KEY (");
                for (String primaryKey : entity.compositePrimaryKey) {
                    sb.append(primaryKey).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(")");
            } else {
                sb.append(entity.columnName).append(" ").append(entity.columnType);
                if (entity.isNotNull) {
                    sb.append(" NOT NULL");
                }
                if (entity.isPrimary) {
                    sb.append(" PRIMARY KEY");
                }
                if (entity.isAutoincrement) {
                    sb.append(" AUTOINCREMENT");
                }
                sb.append(",");
            }
        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(')');
        return sb.toString();
    }

    public String getColumnName(int columnIndex) {
        return list.get(columnIndex).columnName;
    }

    public int getColumnCount() {
        return list.size();
    }

    public int getColumnIndex(String columnName) {
        int columnCount = getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            if (list.get(i).columnName.equals(columnName)) return i;
        }
        return -1;
    }
}