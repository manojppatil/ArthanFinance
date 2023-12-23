package com.av.arthanfinance.fcm;


import android.app.NotificationManager;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;

import com.clevertap.android.sdk.CleverTapAPI;


import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

//******************* PLEASE MAKE SURE TO ADD THE GOOGLE_SERVICES.JSON FILE IN TO THE PROJECT DIRECTORY***************//
public class FcmMessageListenerService extends FirebaseMessagingService {

	CleverTapAPI clevertapDefaultInstance;
	@Override
	public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);

		RemoteMessage.Notification notification = remoteMessage.getNotification();
		try {
			if (remoteMessage.getData().size() > 0) {
				Bundle extras = new Bundle();
				for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
					extras.putString(entry.getKey(), entry.getValue());
					Log.d("key,value", entry.getKey()+" and "+entry.getValue());
				}

				NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);


				if (info.fromCleverTap) {

						CleverTapAPI.createNotification(getApplicationContext(), extras);

				} else {
					Map<String, String> data = remoteMessage.getData();
					Log.d("FROM", remoteMessage.getFrom());
				}
			}
		} catch (Throwable t) {
			Log.d("MYFCMLIST", "Error parsing FCM message", t);
		}

	}

	@Override
	public void onNewToken(@NonNull String s) {
		super.onNewToken(s);
		clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
		assert clevertapDefaultInstance != null;
		clevertapDefaultInstance.pushFcmRegistrationId(s,true);
		CleverTapAPI.createNotificationChannel(this,"arthik_channel","arthik_channel","Channel for Push in App", NotificationManager.IMPORTANCE_HIGH,true);
	}


}