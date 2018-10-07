/*    BlitzMail
 *    Copyright (C) 2013 Torsten Grote
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.blitzmail;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

public class MailStorage {

	public static JSONObject getMails(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences("BlitzMail", MODE_PRIVATE);
		String mails_str = sharedPref.getString("mails", null);

		JSONObject mails = new JSONObject();

		if(mails_str != null) {
			try {
				mails = new JSONObject(mails_str);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return mails;
	}

	public static void saveMail(Context context, JSONObject mail) {
		SharedPreferences sharedPref = context.getSharedPreferences("BlitzMail", MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();

		JSONObject mails = getMails(context);

		try {
			mails.put(mail.getString("id"), mail);
		} catch (JSONException e) {
			throw new AssertionError(e);
		}

		prefEditor.putString("mails", mails.toString());
		prefEditor.apply();
	}

	public static void deleteMail(Context context, String id) {
		SharedPreferences sharedPref = context.getSharedPreferences("BlitzMail", MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();

		JSONObject mails = getMails(context);

		// also delete temporary file
		try {
			if(mails.getJSONObject(id).has("attachments")) {
				JSONArray attachments = mails.getJSONObject(id).getJSONArray("attachments");

				// iterate over all attachments
				for(int i = 0; i < attachments.length(); i++) {
					JSONObject attachment = attachments.getJSONObject(i);

					File file = new File(attachment.getString("path"));
					file.delete();
				}
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}

		// remove actual email
		mails.remove(id);

		String mails_str = null;
		try {
			mails_str = mails.toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d("MailStorage", "Removing mail with id " + id);

		prefEditor.putString("mails", mails_str);
		prefEditor.apply();
	}
}
