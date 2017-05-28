package nyc.c4q.rusili.weatherwidget.utilities;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Calendar;

import nyc.c4q.rusili.weatherwidget.activities.widgets.WeatherWidget4x2;

public class ScreenServiceAndReceiver extends Service{
	private BroadcastReceiver broadcastReceiver;
	public static long currentTIme;

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Toast.makeText(getBaseContext(), "Service onStartCommand", Toast.LENGTH_SHORT).show();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		broadcastReceiver = new ScreenReceiver(currentTIme);
		registerReceiver(broadcastReceiver, filter);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	@Nullable
	@Override
	public IBinder onBind (Intent intent) {
		return null;
	}

	// Broadcast Receiver
	private final class ScreenReceiver extends BroadcastReceiver {
		private Calendar calendar;
		private long timeInMilliseconds;

		public ScreenReceiver (long currentTIme) {
			timeInMilliseconds = currentTIme;
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				calendar = Calendar.getInstance();
				long timeInMillisecondsCurrent = calendar.getTimeInMillis();

				if (checkTime(timeInMillisecondsCurrent)) {            // Only updates if last update was over an hour ago
					Toast.makeText(getBaseContext(), "ScreenReceiver Screen On", Toast.LENGTH_SHORT).show();

					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(), "WeatherWidget4x2");
					int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
					Intent updateWidgetIntent = new Intent(context, WeatherWidget4x2.class);
					updateWidgetIntent.setAction("4x2_UPDATE_CLICK");
					updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
					sendBroadcast(updateWidgetIntent);
				}
			}
		}

		private boolean checkTime(long timeInMillisecondsParam){
			if ((timeInMillisecondsParam-timeInMilliseconds)> Constants.UPDATE_DELAY.MILLISECONDS){
				timeInMilliseconds = timeInMillisecondsParam;
				return true;
			}
			return false;
		}
	}
}
