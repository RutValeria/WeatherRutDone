package com.example.weatherrut;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "2cf5707b137c3cf1c2cb2e6e81880995";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s";
    private static final String BASE_URL_BY_CITY = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s";

    private EditText etCity;
    private Switch switchTempUnit, switchTheme;
    private ImageView ivWeatherIcon;
    private TextView tvTemperature, tvWeatherDesc, tvWindDirection, tvHumidity, tvWindSpeed, tvCity, tvFeelsLike, tvSunriseSunset;
    private Button btnSearch;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Установка сохранённой темы
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = preferences.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        // Инициализация элементов
        etCity = findViewById(R.id.et_city);
        switchTempUnit = findViewById(R.id.switch_temp_unit);
        switchTheme = findViewById(R.id.switch_theme);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvWindDirection = findViewById(R.id.tv_wind_direction);
        tvHumidity = findViewById(R.id.tv_humidity);
        tvWindSpeed = findViewById(R.id.tv_wind_speed);
        tvCity = findViewById(R.id.tvCity);
        tvFeelsLike = findViewById(R.id.tv_feels_like);
        tvSunriseSunset = findViewById(R.id.tv_sunrise_sunset);
        btnSearch = findViewById(R.id.btn_search);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Установка начального состояния темы
        switchTheme.setChecked(isNightMode);

        // Обработчик переключения темы
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Автоматическое получение погоды при запуске
        getLocationAndFetchWeather();

        // Обработчик на переключение температуры
        switchTempUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String units = isChecked ? "imperial" : "metric"; // Фаренгейт/Цельсий
            if (etCity.getText().toString().trim().isEmpty()) {
                getLocationAndFetchWeather();  // Обновляем погоду по местоположению
            } else {
                fetchWeatherDataByCity(etCity.getText().toString().trim(), units); // Обновляем погоду по городу
            }
        });

        btnSearch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (!city.isEmpty()) {
                String units = switchTempUnit.isChecked() ? "imperial" : "metric";
                fetchWeatherDataByCity(city, units);
            } else {
                getLocationAndFetchWeather();
            }
        });
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String units = switchTempUnit.isChecked() ? "imperial" : "metric";
                        fetchWeatherDataByLocation(latitude, longitude, units);
                    } else {
                        Toast.makeText(MainActivity.this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchWeatherDataByLocation(double latitude, double longitude, String units) {
        String url = String.format(BASE_URL, latitude, longitude, API_KEY, units) + "&lang=ru";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateWeatherUI(response, units);
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Ошибка при обработке данных о погоде", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(MainActivity.this, "Ошибка при получении данных о погоде", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void fetchWeatherDataByCity(String city, String units) {
        String url = String.format(BASE_URL_BY_CITY, city, API_KEY, units) + "&lang=ru";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        updateWeatherUI(response, units);
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Ошибка при обработке данных о погоде", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(MainActivity.this, "Ошибка при получении данных о погоде", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void updateWeatherUI(JSONObject response, String units) throws JSONException {
        JSONObject main = response.getJSONObject("main");
        JSONObject wind = response.getJSONObject("wind");
        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
        JSONObject sys = response.getJSONObject("sys");

        double temperature = main.getDouble("temp");
        double feelsLike = main.getDouble("feels_like");
        String description = weather.getString("description");
        int humidity = main.getInt("humidity");
        double windSpeed = wind.getDouble("speed");
        int windDirection = wind.getInt("deg");
        String cityName = response.getString("name");

        long sunrise = sys.getLong("sunrise");
        long sunset = sys.getLong("sunset");
        int timezoneOffset = response.getInt("timezone");

        tvCity.setText(cityName);
        tvTemperature.setText(String.format("%.1f", temperature) + (units.equals("metric") ? " °C" : " °F"));
        tvFeelsLike.setText("Ощущается как: " + String.format("%.1f", feelsLike) + (units.equals("metric") ? " °C" : " °F"));
        tvWeatherDesc.setText(description);
        tvHumidity.setText("Влажность: " + humidity + "%");
        tvWindSpeed.setText("Скорость ветра: " + windSpeed + " м/с");
        tvWindDirection.setText("Направление ветра: " + getWindDirection(windDirection));
        tvSunriseSunset.setText("Восход: " + formatTime(sunrise, timezoneOffset) + " | Закат: " + formatTime(sunset, timezoneOffset));

        String iconCode = weather.getString("icon");
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(this).load(iconUrl).into(ivWeatherIcon);
    }

    private String getWindDirection(int degree) {
        if (degree >= 0 && degree <= 45) return "Северный";
        if (degree > 45 && degree <= 135) return "Восточный";
        if (degree > 135 && degree <= 225) return "Южный";
        if (degree > 225 && degree <= 315) return "Западный";
        return "Северо-западный";
    }

    private String formatTime(long time, int timezoneOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+" + (timezoneOffset / 3600)));
        return sdf.format(new Date(time * 1000));
    }
}
