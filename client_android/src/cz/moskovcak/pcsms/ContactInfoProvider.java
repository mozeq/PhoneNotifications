package cz.moskovcak.pcsms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class ContactInfoProvider {
	
	public static String getNameForNumber(Context context, String number) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mColumns = {PhoneLookup.DISPLAY_NAME, PhoneLookup.NUMBER};
        String selection = PhoneLookup.NUMBER + " LIKE ?";
        /* number is always in the full format, so PhoneLookup.NUMBER
         * is either the same or subset of it
         */
        String[] selectionArgs = {"%"+number+"%"};
        Cursor mCursor = context.getContentResolver().query(uri, mColumns, selection, selectionArgs, null);



        String contactName = "Unknown";

        if (mCursor != null) {
            int nameColumnIndex = mCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
            mCursor.moveToFirst(); 
            contactName = mCursor.getString(nameColumnIndex);
            System.out.println("found: " + contactName);
        }
        else
            System.out.println("cursor is null");
        
       return contactName;
	}
}
