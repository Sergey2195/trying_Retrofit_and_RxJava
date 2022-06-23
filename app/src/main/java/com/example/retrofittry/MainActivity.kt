package com.example.retrofittry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class FactListResponce(
    @SerializedName("fact")
    val fact: String,
    @SerializedName("length")
    val length: Int
)

interface CatApi{
    @GET("./fact")
    fun getFactString(): Single<FactListResponce>
}

class MainActivity : AppCompatActivity() {
    lateinit var factApi: CatApi
    lateinit var tvText: TextView
    lateinit var btnNext: Button
    lateinit var httpLoginException: HttpLoggingInterceptor
    lateinit var okHttpClient: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
        apiRequest()
        btnNext.setOnClickListener {
            apiRequest()
        }
    }
    private fun setup(){
        tvText = findViewById(R.id.tvFact)
        btnNext = findViewById(R.id.btnNext)
        httpLoginException = HttpLoggingInterceptor()
        httpLoginException.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoginException)
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl("https://catfact.ninja")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        factApi = retrofit.create(CatApi::class.java)
        compositeDisposable = CompositeDisposable()
    }
    private fun apiRequest(){
        compositeDisposable.add(factApi.getFactString()
            .subscribeOn(Schedulers.io())
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe({
                Log.i("myRetrofit", it.fact)
                Log.i("myRetrofit", it.length.toString())
                when (it.length){
                    in 0..80 -> tvText.textSize = 50F
                    in 81..150 -> tvText.textSize = 40F
                    in 151..300 -> tvText.textSize = 30F
                    else -> tvText.textSize = 20F
                }
                tvText.text = it.fact
            },{
                Log.i("myRetrofit", it.message.toString())
            })
        )
    }

}