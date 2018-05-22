/**
 * Copyright (C) 2015 Anthony K. Trinh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tony19.loggly;

import java.util.Collection;

/**
 * Loggly client interface
 *
 * @author tony19@gmail.com
 * @since 1.0.3
 */
public interface ILogglyClient{

    /**
     * Callback for asynchronous logging
     */
    interface Callback {
        /**
         * Function to be called when the log request was successfully sent to Loggly
         */
        void success();

        /**
         * Function to be called when the log request failed
         * @param error message details about the failure
         */
        void failure(String error);
    }

    /**
     * Writes a single log event asynchronously
     * @param message message to be logged
     * @param callback callback to be invoked on completion
     */
    void log(String message, Callback callback);

    /**
     * Writes multiple log events at once asynchronously
     * @param messages log events to be written
     * @param callback callback to be invoked on completion
     */
    void logBulk(Collection<String> messages, Callback callback);
}
