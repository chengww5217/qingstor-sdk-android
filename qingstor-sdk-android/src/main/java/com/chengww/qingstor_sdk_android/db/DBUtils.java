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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by chengww on 2018/12/28.
 */
public class DBUtils {

    public static boolean isNeedUpgradeTable(SQLiteDatabase db, TableEntity table) {
        if (!isTableExists(db, table.tableName)) return true;

        Cursor cursor = db.rawQuery("select * from " + table.tableName, null);
        if (cursor == null) return false;
        try {
            int columnCount = table.getColumnCount();
            if (columnCount == cursor.getColumnCount()) {
                for (int i = 0; i < columnCount; i++) {
                    if (table.getColumnIndex(cursor.getColumnName(i)) == -1) {
                        return true;
                    }
                }
            } else {
                return true;
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    public static boolean isTableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) return false;

        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
            if (!cursor.moveToFirst()) {
                return false;
            }
            count = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return count > 0;
    }

    public static boolean isFieldExists(SQLiteDatabase db, String tableName, String fieldName) {
        if (tableName == null || db == null || fieldName == null || !db.isOpen()) return false;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
            return cursor != null && cursor.getColumnIndex(fieldName) != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
