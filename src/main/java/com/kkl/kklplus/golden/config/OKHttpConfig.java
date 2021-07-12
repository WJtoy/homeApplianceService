package com.kkl.kklplus.golden.config;

import com.kkl.kklplus.golden.http.config.GoldenProperties;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties({GoldenProperties.class})
@Configuration
public class OKHttpConfig {

    @Bean
    public OkHttpClient okHttpClient(GoldenProperties goldenProperties) {
        return new OkHttpClient().newBuilder()
                .connectTimeout(goldenProperties.getOkhttp().getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(goldenProperties.getOkhttp().getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(goldenProperties.getOkhttp().getReadTimeout(), TimeUnit.SECONDS)
                .pingInterval(goldenProperties.getOkhttp().getPingInterval(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(goldenProperties.getOkhttp().getRetryOnConnectionFailure())
                .build();
    }

}
