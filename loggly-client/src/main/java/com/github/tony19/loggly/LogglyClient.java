/**
 * Copyright (C) 2015 Anthony K. Trinh
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tony19.loggly;

import android.text.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Loggly client
 *
 * @author xm mobile team
 */
public class LogglyClient implements ILogglyClient {

    private static final String API_URL = "https://logs-01.loggly.com/";
    private static LogglyClient mInstance;

    private final ILogglyRestService loggly;

    private final String token;

    private final Map<String, String> tags;

    /**
     * Creates a Loggly client
     *
     * @param token Loggly customer token
     *              http://loggly.com/docs/customer-token-authentication-token/
     */
    public static LogglyClient getInstance(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be empty");
        }

        if (mInstance == null) {
            mInstance = new LogglyClient(token);
        } else if (!mInstance.token.equals(token)) {
            mInstance = new LogglyClient(token);
        }

        return mInstance;
    }

    private LogglyClient(String token) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.token = token;
        this.loggly = restAdapter.create(ILogglyRestService.class);
        this.tags = new HashMap<>();

        mInstance = this;
    }

    /**
     * Sets the tags to use for Loggly messages. The list of
     * strings are converted into a single CSV (trailing/leading
     * spaces stripped from each entry).
     *
     * @param tags CSV or list of tags
     */
    public void addTags(HashMap<String, String> tags) {
        if (tags.isEmpty()) {
            return;
        }
        this.tags.putAll(tags);
    }

    public void addTag(String name, String value) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            return;
        }

        this.tags.put(name, value);
    }

    public void removeTag(String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }

        this.tags.remove(name);
    }

    private String getTags() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> map : tags.entrySet()) {
            if (!TextUtils.isEmpty(stringBuilder)) {
                stringBuilder.append(",");
            }
            stringBuilder.append(String.format("%s-%s", map.getKey(), map.getValue()));
        }

        return stringBuilder.toString();
    }

    /**
     * Posts a log message asynchronously to Loggly
     *
     * @param message message to be logged
     */
    public void log(String message) {
        log(message, null);
    }

    /**
     * Posts a log message asynchronously to Loggly
     *
     * @param message  message to be logged
     * @param callback callback to be invoked on completion of the post
     */
    public void log(String message, final Callback callback) {
        if (message == null) return;

        Observable<LogglyResponse> call = loggly.log(token, getTags(), message);
        call.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<LogglyResponse>() {
            @Override
            public void call(LogglyResponse logglyResponse) {

                if (callback == null) {
                    return;
                }

                callback.success();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (callback == null) {
                    return;
                }

                callback.failure(throwable.getMessage());
            }
        });
    }

    /**
     * Posts several log messages in bulk to Loggly asynchronously
     *
     * @param messages messages to be logged
     * @param callback callback to be invoked on completion of the post
     */
    public void logBulk(Collection<String> messages) {
        logBulk(messages, null);
    }

    /**
     * Posts several log messages in bulk to Loggly asynchronously
     *
     * @param messages messages to be logged
     * @param callback callback to be invoked on completion of the post
     */
    public void logBulk(Collection<String> messages, final Callback callback) {
        if (messages == null) return;


        String parcel = joinStrings(messages);
        if (parcel.isEmpty()) return;

        log(parcel, callback);
    }

    /**
     * Combines a collection of messages to be sent to Loggly.
     * In order to preserve event boundaries, the new lines in
     * each message are replaced with '\r', which get stripped
     * by Loggly.
     *
     * @param messages messages to be combined
     * @return a single string containing all the messages
     */
    private String joinStrings(Collection<String> messages) {
        StringBuilder b = new StringBuilder();
        for (String s : messages) {
            if (s == null || s.isEmpty()) {
                continue;
            }

            // Preserve new-lines in this event by replacing them
            // with "\r". Otherwise, they're processed as event
            // delimiters, resulting in unintentional multiple events.
            b.append(s.replaceAll("[\r\n]", "\r")).append('\n');
        }
        return b.toString();
    }
}
