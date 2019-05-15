package com.firebase.tinkoffnews;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    class Headers {

        String resultCode = "OK";
        HeaderInformation[] payload;

        public Headers() {
        }

        public Headers(String resultCode, HeaderInformation[] payload) {
            this.resultCode = resultCode;
            this.payload = payload;
        }

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public HeaderInformation[] getPayload() {
            return payload;
        }

        public void setPayload(HeaderInformation[] payload) {
            this.payload = payload;
        }
    }

    SwipeRefreshLayout refreshLayout;
    RecyclerView recycler;
    HeadersAdapter adapter = new HeadersAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() ->  {
            Observable.create((ObservableOnSubscribe<HeaderInformation>) emitter -> {
                adapter.clear();
                Gson gson = new Gson();
                StringBuilder text = new StringBuilder();
                InputStream in = new URL("https://api.tinkoff.ru/v1/news").openStream();
                Scanner scanner = new Scanner(in);
                while (scanner.hasNext()) text.append(scanner.nextLine());
                Headers headers = gson.fromJson(text.toString(), Headers.class);
                for (HeaderInformation information : headers.payload) {
                    emitter.onNext(information);
                }
                emitter.onComplete();
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(headerInformation -> adapter.addItem(headerInformation))
                    .doOnError(throwable -> throwable.printStackTrace())
                    .doOnComplete(()->refreshLayout.setRefreshing(false)).subscribe();
        });
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler.setAdapter(adapter);
        adapter.addItem(new HeaderInformation(1, 1, "name", "А какая вам разница??", new Data(10000000L)));
    }
}
