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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chengww.qingstor_sdk_android.QingstorHelper;

/**
 * Created by chengww on 2018/12/28.
 */
class DBHelper extends SQLiteOpenHelper {

    private static final String DB_CACHE_NAME = "qingstor_upload_download_tasks.db";
    private static final int DB_CACHE_VERSION = 1;
    static final String TABLE_DOWNLOAD = "downloader";
    static final String TABLE_UPLOAD = "uploader";

    private TableEntity downloadTableEntity = new TableEntity(TABLE_DOWNLOAD);
    private TableEntity uploadTableEntity = new TableEntity(TABLE_UPLOAD);

    DBHelper() {
        this(QingstorHelper.getInstance().getContext());
    }

    DBHelper(Context context) {
        super(context, DB_CACHE_NAME, null, DB_CACHE_VERSION);

        downloadTableEntity.addColumn(new ColumnEntity(Progress.TAG, "VARCHAR", true, true))
                .addColumn(new ColumnEntity(Progress.BUCKET, "BLOB"))
                .addColumn(new ColumnEntity(Progress.OBJECT_KEY, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FOLDER, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FILE_PATH, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FILE_NAME, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FRACTION, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.TOTAL_SIZE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.CURRENT_SIZE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.STATUS, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.DATE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.EXTRA1, "BLOB"))
                .addColumn(new ColumnEntity(Progress.EXTRA2, "BLOB"))
                .addColumn(new ColumnEntity(Progress.EXTRA3, "BLOB"))
                .addColumn(new ColumnEntity(Progress.RECORDER_BEAN, "BLOB"));

        uploadTableEntity.addColumn(new ColumnEntity(Progress.TAG, "VARCHAR", true, true))
                .addColumn(new ColumnEntity(Progress.BUCKET, "BLOB"))
                .addColumn(new ColumnEntity(Progress.OBJECT_KEY, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FOLDER, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FILE_PATH, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FILE_NAME, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.FRACTION, "VARCHAR"))
                .addColumn(new ColumnEntity(Progress.TOTAL_SIZE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.CURRENT_SIZE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.STATUS, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.DATE, "INTEGER"))
                .addColumn(new ColumnEntity(Progress.EXTRA1, "BLOB"))
                .addColumn(new ColumnEntity(Progress.EXTRA2, "BLOB"))
                .addColumn(new ColumnEntity(Progress.EXTRA3, "BLOB"))
                .addColumn(new ColumnEntity(Progress.RECORDER_BEAN, "BLOB"));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(downloadTableEntity.buildTableString());
        db.execSQL(uploadTableEntity.buildTableString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DBUtils.isNeedUpgradeTable(db, downloadTableEntity))
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD);
        if (DBUtils.isNeedUpgradeTable(db, uploadTableEntity))
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
