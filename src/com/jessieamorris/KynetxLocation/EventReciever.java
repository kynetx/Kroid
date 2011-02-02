package com.jessieamorris.KynetxLocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
 
public class EventReciever extends BroadcastReceiver {
	
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Log.i("jessieamorris", "EVENT: " + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, com.jessieamorris.KynetxLocation.KynetxService.class);
			context.startService(i);
		} else if(intent.getAction().equals(SMS_RECEIVED)){
			Bundle extras = intent.getExtras();
			if(extras != null){
				Object[] pdus = (Object[])extras.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for(int i = 0; i < pdus.length; i++){
					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				}
				if(messages.length > -1){
					Log.i("jessieamorris", "SMS recieved: " + messages[0].getMessageBody());
					Log.i("jessieamorris", "SMS from: " + messages[0].getOriginatingAddress());
				}
			}
		}
	}
}