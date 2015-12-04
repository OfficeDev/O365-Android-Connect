/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connect;

/**
 * Callback interface for Office 365 operations
 * such as discovering a service or sending email.
 * @param <T> The result of the operation in case of success.
 */
interface OperationCallback<T> {
    /**
     * The method to call in case of success.
     * @param result The result of the operation.
     */
    void onSuccess(T result);

    /**
     * The method to call in case of failure.
     * @param e The exception or reason of failure.
     */
    void onError(Exception e);
}