package com.example.weatherrut;

import java.util.List;

public class WeatherResponse {
    private Main main;
    private Wind wind;
    private String name;
    private List<Weather> weather;

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public String getName() {
        return name;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public static String getWindDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) return "North";
        else if (degree >= 22.5 && degree < 67.5) return "Northeast";
        else if (degree >= 67.5 && degree < 112.5) return "East";
        else if (degree >= 112.5 && degree < 157.5) return "Southeast";
        else if (degree >= 157.5 && degree < 202.5) return "South";
        else if (degree >= 202.5 && degree < 247.5) return "Southwest";
        else if (degree >= 247.5 && degree < 292.5) return "West";
        else return "Northwest";
    }

    public static class Main {
        private float temp;

        public float getTemp() {
            return temp;
        }
    }

    public static class Wind {
        private float speed;
        private float deg; // Добавьте поле для угла ветра

        public float getSpeed() { return speed; }
        public float getDeg() { return deg; }  // Добавьте геттер для угла
    }


    public static class Weather {
        private String description;
        private String icon;

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

    }

}
