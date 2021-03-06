/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.shopify.buy.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Represents the token object associated with a logged in Customer
 */
public class CustomerToken {

    @SerializedName("access_token")
    protected String accessToken;

    @SerializedName("customer_id")
    protected Long customerId;

    @SerializedName("expires_at")
    protected Date expiresAt;

    public CustomerToken() {
    }

    public CustomerToken(final CustomerToken other) {
        if (other != null) {
            accessToken = other.accessToken;
            customerId = other.customerId;
            expiresAt = other.expiresAt;
        }
    }

    /**
     * Create a CustomerToken
     *
     * @param accessToken  The access token, not null
     * @param customerId  The customerId, not null
     * @param expiresAt The date the token expires, optional.
     */
    public CustomerToken(String accessToken, Long customerId, Date expiresAt) {
        this.accessToken = accessToken;
        this.customerId = customerId;
        this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerToken that = (CustomerToken) o;

        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null)
            return false;
        return customerId != null ? customerId.equals(that.customerId) : that.customerId == null;

    }

    @Override
    public int hashCode() {
        int result = accessToken != null ? accessToken.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        return result;
    }
}
