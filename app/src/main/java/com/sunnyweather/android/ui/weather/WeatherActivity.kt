package com.sunnyweather.android.ui.weather

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.ui.place.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val decorView = window.decorView
//        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)

        viewModel.let {
            if (it.locationLat.isEmpty()) {
                it.locationLat = intent.getStringExtra("location_lat") ?: ""
            }
            if (it.locationLng.isEmpty()) {
                    it.locationLng = intent.getStringExtra("location_lng") ?: ""
            }
            if (it.placeName.isEmpty()) {
                it.placeName = intent.getStringExtra("place_name") ?: ""
            }

            it.weatherLiveData.observe(this) { result ->
                val weather = result.getOrNull()
                if (weather != null) {
                    showWeatherInfo(weather)
                } else {
                    Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }

            it.refreshWeather(it.locationLng, it.locationLat)
        }
    }

    private fun showWeatherInfo(weather: Weather) {
        val placeName: TextView = findViewById(R.id.placeName)
        val currentTemp: TextView = findViewById(R.id.currentTemp)
        val currentSky: TextView = findViewById(R.id.currentSky)
        val currentAQI: TextView = findViewById(R.id.currentAQI)
        val forecastLayout: LinearLayout = findViewById(R.id.forecastLayout)
        val nowLayout: RelativeLayout = findViewById(R.id.nowLayout)

        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
//        fill data in now.xml
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
//        fill data in forecast.xml
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temerature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                forecastLayout, false)
            val dateInfo: TextView = view.findViewById(R.id.dateInfo)
            val skyIcon: ImageView = view.findViewById(R.id.skyIcon)
            val skyInfo: TextView = view.findViewById(R.id.skyInfo)
            val temperatureInfo: TextView = view.findViewById(R.id.temperatureInfo)

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            getSky(skycon.value).let {
                skyIcon.setImageResource(it.icon)
                skyInfo.text = it.info
            }
            val temperatureText = "${temerature.min.toInt()} ~ ${temerature.max.toInt()} ℃"
            temperatureInfo.text = temperatureText

            forecastLayout.addView(view)
        }
//        fill data in life_index.xml
        val coldRiskText: TextView = findViewById(R.id.coldRiskText)
        val dressingText: TextView = findViewById(R.id.dressingText)
        val ultravioletText: TextView = findViewById(R.id.ultravioletText)
        val carWashingText: TextView = findViewById(R.id.carWashingText)
        val lifeIndex = daily.lifeIndex
        lifeIndex.let {
            coldRiskText.text = it.coldRisk[0].desc
            dressingText.text = it.dressing[0].desc
            ultravioletText.text = it.ultraviolet[0].desc
            carWashingText.text = it.carWashing[0].desc
        }

//        make the ScrollView visible
        val weatherLayout: ScrollView = findViewById(R.id.weatherLayout)
        weatherLayout.visibility = View.VISIBLE
    }
}