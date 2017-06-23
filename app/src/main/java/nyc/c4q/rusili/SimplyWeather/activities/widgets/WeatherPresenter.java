package nyc.c4q.rusili.SimplyWeather.activities.widgets;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import nyc.c4q.rusili.SimplyWeather.network.GoogleAPI.GoogleLocationAPI;
import nyc.c4q.rusili.SimplyWeather.network.WUnderground.JSON.ResponseConditionsForecast10DayHourly;
import nyc.c4q.rusili.SimplyWeather.network.WUnderground.WundergroundRetrofit;
import nyc.c4q.rusili.SimplyWeather.utilities.Constants;
import nyc.c4q.rusili.SimplyWeather.utilities.ScreenServiceAndReceiver;

public class WeatherPresenter implements BasePresenterInterface {
	private Weather4x2View weather4x2View;
	private WundergroundRetrofit.RetrofitListener retrofitListener;
	private ActivityManager activityManager;

	private static GoogleLocationAPI googleLocationAPI;
	private static WundergroundRetrofit wundergroundRetrofit;

	private final String apiKey = Constants.DEVELOPER_KEY.API_KEY;

	public WeatherPresenter(Weather4x2View weather4x2View){
		this.weather4x2View = weather4x2View;
		initialize();
	}

	@Override
	public void initialize () {}

	@Override
	public void getGoogleAPILocation () {
		googleLocationAPI = googleLocationAPI.getInstance();
		googleLocationAPI.setRetrofitListener(new GoogleLocationAPI.GoogleLocationAPILIstener() {
			@Override
			public void onConnection (int zipCode) {
				getWUndergroundAPIResponse(zipCode);
			}
		});
		googleLocationAPI.getZipCode(weather4x2View.getContext());
	}

	@Override
	public void getWUndergroundAPIResponse (int zipCode) {
		wundergroundRetrofit = wundergroundRetrofit.getInstance();
		wundergroundRetrofit.setRetrofitListener(retrofitListener = new WundergroundRetrofit.RetrofitListener() {
			@Override
			public void onConditionsForecast10DayHourlytRetrieved (ResponseConditionsForecast10DayHourly jsonObject) {
				weather4x2View.updateWidgetViews(jsonObject);
			}
		});
		wundergroundRetrofit.getConditionsForecast10DayHourlyForecast(apiKey, zipCode);
	}

	public void isMyServiceRunning (Context context, Class <?> serviceClass) {
		activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (!serviceClass.getName().equals(service.service.getClassName())) {
				context.startService(new Intent(context, ScreenServiceAndReceiver.class));
			}
		}
	}
}