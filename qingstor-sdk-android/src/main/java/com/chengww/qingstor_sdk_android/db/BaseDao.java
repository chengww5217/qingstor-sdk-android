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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseDao<T> {

    protected static String TAG;
    private static final Lock lock = new ReentrantLock();
    protected SQLiteOpenHelper helper;
    protected SQLiteDatabase database;

    public BaseDao(SQLiteOpenHelper helper) {
        TAG = getClass().getSimpleName();
        this.helper = helper;
        this.database = openWriter();
    }

    public SQLiteDatabase openReader() {
        return helper.getReadableDatabase();
    }

    public SQLiteDatabase openWriter() {
        return helper.getWritableDatabase();
    }

    protected final void closeDatabase(SQLiteDatabase database, Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) cursor.close();
        if (database != null && database.isOpen()) database.close();
    }

    public boolean insert(T t) {
        if (t == null) return false;
        lock.lock();
        try {
            database.beginTransaction();
            database.insert(getTableName(), null, getContentValues(t));
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long insert(SQLiteDatabase database, T t) {
        return database.insert(getTableName(), null, getContentValues(t));
    }

    public boolean insert(List<T> ts) {
        if (ts == null) return false;
        lock.lock();
        try {
            database.beginTransaction();
            for (T t : ts) {
                database.insert(getTableName(), null, getContentValues(t));
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public boolean insert(SQLiteDatabase database, List<T> ts) {
        try {
            for (T t : ts) {
                database.insert(getTableName(), null, getContentValues(t));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAll() {
        return delete(null, null);
    }

    public long deleteAll(SQLiteDatabase database) {
        return delete(database, null, null);
    }

    public boolean delete(String whereClause, String[] whereArgs) {
        long start = System.currentTimeMillis();
        lock.lock();
        try {
            database.beginTransaction();
            database.delete(getTableName(), whereClause, whereArgs);
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long delete(SQLiteDatabase database, String whereClause, String[] whereArgs) {
        return database.delete(getTableName(), whereClause, whereArgs);
    }

    public boolean deleteList(List<Pair<String, String[]>> where) {
        lock.lock();
        try {
            database.beginTransaction();
            for (Pair<String, String[]> pair : where) {
                database.delete(getTableName(), pair.first, pair.second);
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public boolean replace(T t) {
        if (t == null) return false;
        lock.lock();
        try {
            database.beginTransaction();
            database.replace(getTableName(), null, getContentValues(t));
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long replace(SQLiteDatabase database, T t) {
        return database.replace(getTableName(), null, getContentValues(t));
    }

    public boolean replace(ContentValues contentValues) {
        lock.lock();
        try {
            database.beginTransaction();
            database.replace(getTableName(), null, contentValues);
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long replace(SQLiteDatabase database, ContentValues contentValues) {
        return database.replace(getTableName(), null, contentValues);
    }

    public boolean replace(List<T> ts) {
        if (ts == null) return false;
        lock.lock();
        try {
            database.beginTransaction();
            for (T t : ts) {
                database.replace(getTableName(), null, getContentValues(t));
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public boolean replace(SQLiteDatabase database, List<T> ts) {
        try {
            for (T t : ts) {
                database.replace(getTableName(), null, getContentValues(t));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(T t, String whereClause, String[] whereArgs) {
        if (t == null) return false;
        lock.lock();
        try {
            database.beginTransaction();
            database.update(getTableName(), getContentValues(t), whereClause, whereArgs);
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long update(SQLiteDatabase database, T t, String whereClause, String[] whereArgs) {
        return database.update(getTableName(), getContentValues(t), whereClause, whereArgs);
    }

    public boolean update(ContentValues contentValues, String whereClause, String[] whereArgs) {
        long start = System.currentTimeMillis();
        lock.lock();
        try {
            database.beginTransaction();
            database.update(getTableName(), contentValues, whereClause, whereArgs);
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
        return false;
    }

    public long update(SQLiteDatabase database, ContentValues contentValues, String whereClause, String[] whereArgs) {
        return database.update(getTableName(), contentValues, whereClause, whereArgs);
    }

    public List<T> queryAll(SQLiteDatabase database) {
        return query(database, null, null);
    }

    public List<T> query(SQLiteDatabase database, String selection, String[] selectionArgs) {
        return query(database, null, selection, selectionArgs, null, null, null, null);
    }

    public T queryOne(SQLiteDatabase database, String selection, String[] selectionArgs) {
        List<T> query = query(database, null, selection, selectionArgs, null, null, null, "1");
        if (query.size() > 0) return query.get(0);
        return null;
    }

    public List<T> query(SQLiteDatabase database, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        List<T> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            while (!cursor.isClosed() && cursor.moveToNext()) {
                list.add(parseCursorToBean(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase(null, cursor);
        }
        return list;
    }

    public List<T> queryAll() {
        return query(null, null);
    }

    public List<T> query(String selection, String[] selectionArgs) {
        return query(null, selection, selectionArgs, null, null, null, null);
    }

    public T queryOne(String selection, String[] selectionArgs) {
        List<T> query = query(null, selection, selectionArgs, null, null, null, "1");
        return query.size() > 0 ? query.get(0) : null;
    }

    public List<T> query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        lock.lock();
        List<T> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            database.beginTransaction();
            cursor = database.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            while (!cursor.isClosed() && cursor.moveToNext()) {
                list.add(parseCursorToBean(cursor));
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase(null, cursor);
            database.endTransaction();
            lock.unlock();
        }
        return list;
    }

    public interface Action {
        void call(SQLiteDatabase database);
    }

    public void startTransaction(Action action) {
        lock.lock();
        try {
            database.beginTransaction();
            action.call(database);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            lock.unlock();
        }
    }


    public abstract String getTableName();

    public abstract void unInit();

    public abstract T parseCursorToBean(Cursor cursor);

    public abstract ContentValues getContentValues(T t);
}
