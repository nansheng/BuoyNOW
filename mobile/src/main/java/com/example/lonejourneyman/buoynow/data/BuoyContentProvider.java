package com.example.lonejourneyman.buoynow.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.lonejourneyman.buoynow.data.BuoyContract.BuoyEntry.TABLE_NAME;

public class BuoyContentProvider extends ContentProvider {
    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BuoyDBHelper mBuoyDBHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(BuoyContract.AUTHORITY, BuoyContract.PATH_TASKS, TASKS);
        uriMatcher.addURI(BuoyContract.AUTHORITY, BuoyContract.PATH_TASKS + "/#", TASK_WITH_ID);

        return uriMatcher;
    }

    public BuoyContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mBuoyDBHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int taskDeleted;

        switch (match) {
            case TASK_WITH_ID:
                String id = uri.getPathSegments().get(1);
                taskDeleted = db.delete(TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (taskDeleted != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return taskDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Implement this to handle requests for the MIME type of the data at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mBuoyDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case TASKS:
                long id = db.insert(TABLE_NAME,
                        null,
                        values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(BuoyContract.BuoyEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public boolean onCreate() {

        Context context = getContext();
        mBuoyDBHelper = new BuoyDBHelper(context);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mBuoyDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case TASKS:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mBuoyDBHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int taskUpdated;

        switch (match) {
            case TASK_WITH_ID:
                String id = uri.getPathSegments().get(1);
                taskUpdated = db.update(TABLE_NAME,
                        values,
                        "_id=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return taskUpdated;
    }
}
