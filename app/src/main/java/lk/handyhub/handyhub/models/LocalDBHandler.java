package lk.handyhub.handyhub.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class LocalDBHandler extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "handyhub.db";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_USER = "user";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_LINE1 = "line1";
    private static final String COLUMN_LINE2 = "line2";
    private static final String COLUMN_POSTAL = "postal";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";
    private static final String COLUMN_WORK_TITLE = "work_title";
    private static final String COLUMN_PRICING = "pricing";
    private static final String COLUMN_NIC = "nic";
    private static final String COLUMN_EXPERIENCE = "experience";
    private static final String COLUMN_IS_VERIFIED = "is_verified";
    private static final String COLUMN_CATEGORY = "category";

    // SQL query to create the table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FIRST_NAME + " TEXT, " +
                    COLUMN_LAST_NAME + " TEXT, " +
                    COLUMN_MOBILE + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_LINE1 + " TEXT, " +
                    COLUMN_LINE2 + " TEXT, " +
                    COLUMN_POSTAL + " TEXT, " +
                    COLUMN_CITY + " TEXT, " +
                    COLUMN_PROFILE_PICTURE + " TEXT, " +
                    COLUMN_WORK_TITLE + " TEXT, " +
                    COLUMN_PRICING + " TEXT, " +
                    COLUMN_NIC + " TEXT, " +
                    COLUMN_EXPERIENCE + " TEXT, " +
                    COLUMN_IS_VERIFIED + " INTEGER, " +
                    COLUMN_CATEGORY + " TEXT" +
                    ");";


    public LocalDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }


    public long insertUserData(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_LAST_NAME, user.getLastName());
        values.put(COLUMN_MOBILE, user.getMobile());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_LINE1, user.getLine1());
        values.put(COLUMN_LINE2, user.getLine2());
        values.put(COLUMN_POSTAL, user.getPostal());
        values.put(COLUMN_CITY, user.getCity());
        values.put(COLUMN_PROFILE_PICTURE, user.getProfilePicture());
        values.put(COLUMN_WORK_TITLE, user.getWorkTitle());
        values.put(COLUMN_PRICING, user.getPricing());
        values.put(COLUMN_NIC, user.getNic());
        values.put(COLUMN_EXPERIENCE, user.getExperience());
        values.put(COLUMN_IS_VERIFIED, user.isVerified() ? 1 : 0);
        values.put(COLUMN_CATEGORY, user.getCategory());


        return db.insert(TABLE_USER, null, values);
    }


    public User getUserByMobile(String mobile) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                null,
                COLUMN_MOBILE + "=?",
                new String[]{mobile},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }



    public int updateUserData(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_LAST_NAME, user.getLastName());
        values.put(COLUMN_MOBILE, user.getMobile());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_LINE1, user.getLine1());
        values.put(COLUMN_LINE2, user.getLine2());
        values.put(COLUMN_POSTAL, user.getPostal());
        values.put(COLUMN_CITY, user.getCity());
        values.put(COLUMN_PROFILE_PICTURE, user.getProfilePicture());
        values.put(COLUMN_WORK_TITLE, user.getWorkTitle());
        values.put(COLUMN_PRICING, user.getPricing());
        values.put(COLUMN_NIC, user.getNic());
        values.put(COLUMN_EXPERIENCE, user.getExperience());
        values.put(COLUMN_IS_VERIFIED, user.isVerified() ? 1 : 0);
        values.put(COLUMN_CATEGORY, user.getCategory());


        return db.update(TABLE_USER, values, COLUMN_MOBILE + "=?", new String[]{user.getMobile()});
    }


    public void deleteUserByMobile(String mobile) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, COLUMN_MOBILE + "=?", new String[]{mobile});
    }


    private User cursorToUser(Cursor cursor) {
        User user = new User();


        int columnIndex;

        columnIndex = cursor.getColumnIndex(COLUMN_FIRST_NAME);
        if (columnIndex >= 0) user.setFirstName(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_LAST_NAME);
        if (columnIndex >= 0) user.setLastName(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_MOBILE);
        if (columnIndex >= 0) user.setMobile(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
        if (columnIndex >= 0) user.setEmail(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_LINE1);
        if (columnIndex >= 0) user.setLine1(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_LINE2);
        if (columnIndex >= 0) user.setLine2(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_POSTAL);
        if (columnIndex >= 0) user.setPostal(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_CITY);
        if (columnIndex >= 0) user.setCity(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_PROFILE_PICTURE);
        if (columnIndex >= 0) user.setProfilePicture(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_WORK_TITLE);
        if (columnIndex >= 0) user.setWorkTitle(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_PRICING);
        if (columnIndex >= 0) user.setPricing(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_NIC);
        if (columnIndex >= 0) user.setNic(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_EXPERIENCE);
        if (columnIndex >= 0) user.setExperience(cursor.getString(columnIndex));

        columnIndex = cursor.getColumnIndex(COLUMN_IS_VERIFIED);
        if (columnIndex >= 0) user.setVerified(cursor.getInt(columnIndex) == 1);

        columnIndex = cursor.getColumnIndex(COLUMN_CATEGORY);
        if (columnIndex >= 0) user.setCategory(cursor.getString(columnIndex));

        return user;
    }

}