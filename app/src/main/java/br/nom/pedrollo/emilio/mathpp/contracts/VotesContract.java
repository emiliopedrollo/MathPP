package br.nom.pedrollo.emilio.mathpp.contracts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class VotesContract {
    private VotesContract(){}

    public static class VoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "votes";
        public static final String COLUMN_NAME_ANSWER_ID = "answer_id";
        public static final String COLUMN_NAME_VALUE = "value";
    }

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + VoteEntry.TABLE_NAME + " (" +
                    VoteEntry._ID + " INTEGER PRIMARY KEY," +
                    VoteEntry.COLUMN_NAME_ANSWER_ID + INTEGER_TYPE + COMMA_SEP +
                    VoteEntry.COLUMN_NAME_VALUE + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + VoteEntry.TABLE_NAME;

    public static class VotesDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Votes.db";

        public VotesDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //db.execSQL(SQL_DELETE_ENTRIES);
            //onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
