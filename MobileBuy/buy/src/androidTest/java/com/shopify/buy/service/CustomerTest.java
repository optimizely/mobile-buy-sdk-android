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

package com.shopify.buy.service;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.Suppress;
import android.text.TextUtils;

import com.shopify.buy.dataprovider.BuyClientError;
import com.shopify.buy.dataprovider.Callback;
import com.shopify.buy.extensions.ShopifyAndroidTestCase;
import com.shopify.buy.model.AccountCredentials;
import com.shopify.buy.model.Address;
import com.shopify.buy.model.Customer;
import com.shopify.buy.model.CustomerToken;
import com.shopify.buy.model.Order;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class CustomerTest extends ShopifyAndroidTestCase {

    private static final String PASSWORD = "asdasd";
    private static final String EMAIL = "asd@asda.com";
    private static final String FIRSTNAME = "Testy";
    private static final String LASTNAME = "McTesterson2";
    private static final String WRONG_PASSWORD = "iii";
    private static final String MALFORMED_EMAIL = "aaaj*&^";
    private static final String OTHER_WRONG_PASSWORD = "7g63";

    private Customer customer;
    private List<Order> orders;
    private List<Address> addresses;
    private Address address;
    private CustomerToken customerToken;

    @Test
    public void testCustomerCreation() throws InterruptedException {
        final Customer randomCustomer = USE_MOCK_RESPONSES ? getExistingCustomer() : generateRandomCustomer();
        final AccountCredentials accountCredentials = new AccountCredentials(randomCustomer.getEmail(), PASSWORD, randomCustomer.getFirstName(), randomCustomer.getLastName());

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.createCustomer(accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                assertNotNull(customer);

                if (!USE_MOCK_RESPONSES) {
                    // TODO fix this test for mock data
                    assertEquals(randomCustomer.getEmail(), customer.getEmail());
                    assertEquals(randomCustomer.getFirstName(), customer.getFirstName());
                    assertEquals(randomCustomer.getLastName(), customer.getLastName());
                    assertEquals(customer.getId(), buyClient.getCustomerToken().getCustomerId());
                }

                assertNotNull(buyClient.getCustomerToken());
                assertEquals(false, buyClient.getCustomerToken().getAccessToken().isEmpty());

                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Suppress
    @Test
    public void testCustomerActivation() throws InterruptedException {
        testCustomerLogin();

        final AccountCredentials accountCredentials = new AccountCredentials(customer.getEmail(), PASSWORD, customer.getFirstName(), customer.getLastName());

        final CountDownLatch latch = new CountDownLatch(1);

        // TODO update this test when we start to get real tokens
        buyClient.activateCustomer(customer.getId(), "need activation token not access token", accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                assertNotNull(customer);
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }


    @Test
    public void testCustomerLogin() throws InterruptedException {
        customer = getExistingCustomer();

        final AccountCredentials accountCredentials = new AccountCredentials(customer.getEmail(), PASSWORD);

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.loginCustomer(accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                assertNotNull(buyClient.getCustomerToken());
                assertEquals(false, buyClient.getCustomerToken().getAccessToken().isEmpty());
                assertNotNull(customer);

                CustomerTest.this.customerToken = buyClient.getCustomerToken();
                CustomerTest.this.customer = customer;

                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testCustomerLogout() throws InterruptedException {
        testCustomerLogin();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.logoutCustomer(new Callback<Void>() {
            @Override
            public void success(Void aVoid) {
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testNonExistentCustomerLogout() throws InterruptedException {
        testCustomerLogin();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.setCustomerToken(new CustomerToken(customerToken.getAccessToken(), -customerToken.getCustomerId(), customerToken.getExpiresAt()));
        buyClient.logoutCustomer(new Callback<Void>() {
            @Override
            public void success(Void aVoid) {
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testCustomerRenew() throws InterruptedException {
        testCustomerLogin();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.renewCustomer(new Callback<CustomerToken>() {
            @Override
            public void success(CustomerToken customerToken) {
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testCustomerRecover() throws InterruptedException {
        customer = getExistingCustomer();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.recoverPassword(EMAIL, new Callback<Void>() {
            @Override
            public void success(Void aVoid) {
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testCustomerUpdate() throws InterruptedException {
        testCustomerLogin();

        customer.setLastName("Foo");

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.updateCustomer(customer, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                assertNotNull(customer);
                assertEquals("Foo", customer.getLastName());
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testGetCustomerOrders() throws InterruptedException {
        testCustomerLogin();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.getOrders(new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders) {
                assertNotNull(orders);
                assertEquals(true, orders.size() > 0);
                CustomerTest.this.orders = orders;
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }

        });

        latch.await();
    }

    @Test
    public void testGetOrder() throws InterruptedException {
        testGetCustomerOrders();

        Long orderId = orders.get(0).getId();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.getOrder(orderId, new Callback<Order>() {
            @Override
            public void success(Order order) {
                assertNotNull(order);
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testGetCustomer() throws InterruptedException {
        testCustomerLogin();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.getCustomer(new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                assertNotNull(customer);
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testSetGetToken() throws InterruptedException {
        testCustomerLogin();

        CustomerToken customerToken = buyClient.getCustomerToken();

        // Test setting from an existing CustomerToken
        buyClient.setCustomerToken(null);
        assertEquals(null, buyClient.getCustomerToken());
        CustomerToken manualCustomerToken = new CustomerToken(customerToken);
        buyClient.setCustomerToken(customerToken);
        assertEquals(manualCustomerToken, buyClient.getCustomerToken());

        buyClient.setCustomerToken(null);
        assertEquals(null, buyClient.getCustomerToken());

        // Test setting from an existing access token string
        buyClient.setCustomerToken(null);
        assertEquals(null, buyClient.getCustomerToken());
        manualCustomerToken = new CustomerToken(customerToken.getAccessToken(), customerToken.getCustomerId(), customerToken.getExpiresAt());
        buyClient.setCustomerToken(manualCustomerToken);
        assertEquals(customerToken, buyClient.getCustomerToken());
    }


    @Test
    public void testCreateAddress() throws InterruptedException {
        testCustomerLogin();
        createAddress();
    }

    private void createAddress() throws InterruptedException {
        final Address inputAddress = USE_MOCK_RESPONSES ? getExistingAddress() : generateAddress();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.createAddress(inputAddress, new Callback<Address>() {
            @Override
            public void success(Address address) {
                if (!USE_MOCK_RESPONSES) {
                    // TODO fix this for mock responses
                    assertEquals(true, inputAddress.locationsAreEqual(address));
                    assertEquals(inputAddress.getCompany(), address.getCompany());
                    assertEquals(inputAddress.getFirstName(), address.getFirstName());
                    assertEquals(inputAddress.getLastName(), address.getLastName());
                    assertEquals(inputAddress.getPhone(), address.getPhone());
                }

                CustomerTest.this.address = address;
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testDeleteAddress() throws InterruptedException {
        testCustomerLogin();
        createAddress();
        getAddresses();

        final int addressCount = addresses.size();

        // assert that addresses has the created address
        assertEquals(true, addresses.contains(address));

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        buyClient.deleteAddress(address.getId(), new Callback<Void>() {
            @Override
            public void success(Void response) {
                countDownLatch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail("Expected success");
            }
        });

        // Wait for the delete to finish
        countDownLatch.await();

        // Make sure the addresses list looks correct now
        getAddresses();

        // address is no longer in the list
        assertEquals(false, addresses.contains(address));

        // address count is one less
        assertEquals(addressCount - 1, addresses.size());
    }

    @Test
    public void testGetAddresses() throws InterruptedException {
        testCustomerLogin();
        getAddresses();
    }

    private void getAddresses() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.getAddresses(new Callback<List<Address>>() {
            @Override
            public void success(List<Address> addresses) {
                assertNotNull(addresses);
                assertEquals(true, addresses.size() > 0);
                CustomerTest.this.addresses = addresses;
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testGetAddress() throws InterruptedException {
        testGetAddresses();

        Long addressId = addresses.get(0).getId();

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.getAddress(addressId, new Callback<Address>() {
            @Override
            public void success(Address address) {
                assertNotNull(address);
                CustomerTest.this.address = address;
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }

    @Test
    public void testUpdateAddress() throws InterruptedException {
        testGetAddress();

        address.setCity("Toledo");

        updateAddress(address);
    }

    public void updateAddress(Address address) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.updateAddress(address, new Callback<Address>() {
            @Override
            public void success(Address address) {
                assertNotNull(address);
                CustomerTest.this.address = address;
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                fail(error.getRetrofitErrorBody());
            }
        });

        latch.await();
    }


    @Test
    public void testSetDefaultAddress() throws InterruptedException {
        testGetAddresses();

        Address firstAddress = addresses.get(0);
        Address secondAddress;

        firstAddress.setDefault(true);
        updateAddress(firstAddress);
        getAddresses();
        firstAddress = addresses.get(0);
        secondAddress = addresses.get(1);
        assertEquals(true, firstAddress.isDefault());
        assertEquals(false, secondAddress.isDefault());

        secondAddress.setDefault(true);
        updateAddress(secondAddress);
        getAddresses();
        firstAddress = addresses.get(0);
        secondAddress = addresses.get(1);
        assertEquals(false, firstAddress.isDefault());
        assertEquals(true, secondAddress.isDefault());
    }

    private Customer getExistingCustomer() {
        Customer customer = new Customer();
        customer.setEmail(EMAIL);
        customer.setFirstName(FIRSTNAME);
        customer.setLastName(LASTNAME);

        return customer;
    }

    private Customer generateRandomCustomer() {
        Customer customer = new Customer();
        customer.setEmail(generateRandomString() + "customer" + generateRandomString() + "@example.com");
        customer.setFirstName("Customer");
        customer.setLastName("Randomly Generated");
        return customer;
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private int generateRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


    private Address generateAddress() {
        Address shippingAddress = new Address();
        shippingAddress.setAddress1(String.format("%d - 150 Elgin Street", generateRandomInt(0, 9999)));
        shippingAddress.setAddress2("8th Floor");
        shippingAddress.setCity("Ottawa");
        shippingAddress.setProvinceCode("ON");
        shippingAddress.setCompany(String.format("Shopify Inc. (%s)", generateRandomString()));
        shippingAddress.setFirstName(String.format("%s (%s)", generateRandomString(), FIRSTNAME));
        shippingAddress.setLastName(String.format("%s (%s)", generateRandomString(), LASTNAME));
        shippingAddress.setPhone("1-555-555-5555");
        shippingAddress.setCountryCode("CA");
        shippingAddress.setZip("K1N5T5");
        shippingAddress.setCountry("Canada");
        shippingAddress.setProvince("Ontario");
        return shippingAddress;
    }

    private Address getExistingAddress() {
        Address shippingAddress = new Address();
        shippingAddress.setAddress1("150 Elgin Street");
        shippingAddress.setAddress2("8th Floor");
        shippingAddress.setCity("Ottawa");
        shippingAddress.setProvinceCode("ON");
        shippingAddress.setCompany("Shopify Inc.");
        shippingAddress.setFirstName(FIRSTNAME);
        shippingAddress.setLastName(LASTNAME);
        shippingAddress.setPhone("1-555-555-5555");
        shippingAddress.setCountryCode("CA");
        shippingAddress.setZip("K1N5T5");
        shippingAddress.setCountry("Canada");
        shippingAddress.setProvince("Ontario");
        return shippingAddress;
    }

    @Test
    public void testCustomerCreationDuplicateEmail() throws InterruptedException {

        final Customer input = getExistingCustomer();
        final AccountCredentials accountCredentials = new AccountCredentials(input.getEmail(), PASSWORD, input.getFirstName(), input.getLastName());

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.createCustomer(accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                fail("Should not be able to create multiple accounts with same email");
            }

            @Override
            public void failure(BuyClientError error) {
                assertTrue(!TextUtils.isEmpty(error.getRetrofitErrorBody()));
                if (!error.getErrors("customer", "email").containsKey("taken")) {
                    fail(String.format("Should be getting email already taken error. \nGot \"%s\"", error.getMessage()));
                }
                latch.countDown();
            }
        });

        latch.await();
    }

    @Test
    public void testCustomerInvalidLogin() throws InterruptedException {
        final Customer customer = getExistingCustomer();
        final AccountCredentials accountCredentials = new AccountCredentials(customer.getEmail(), WRONG_PASSWORD);

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.loginCustomer(accountCredentials, new Callback<Customer>() {

            @Override
            public void success(Customer customer) {
                fail("Invalid credentials should not be able to login");
            }

            @Override
            public void failure(BuyClientError error) {
                assertEquals(401, error.getRetrofitResponse().code());
                latch.countDown();
            }
        });

        latch.await();
    }

    @Test
    public void testCreateCustomerInvalidEmailPassword() throws InterruptedException {
        final AccountCredentials accountCredentials = new AccountCredentials(MALFORMED_EMAIL, WRONG_PASSWORD, FIRSTNAME, LASTNAME);

        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.createCustomer(accountCredentials, new Callback<Customer>() {

            @Override
            public void success(Customer customerToken) {
                fail("Invalid email, password, confirmation password. Should not be able to create a customer.");
            }

            @Override
            public void failure(BuyClientError error) {
                assertTrue(!TextUtils.isEmpty(error.getRetrofitErrorBody()));

                final Map<String, String> passwordErrors = error.getErrors("customer", "password");
                assertTrue(passwordErrors.containsKey("too_short"));
                assertEquals("is too short (minimum is 5 characters)", passwordErrors.get("too_short"));

                final Map<String, String> emailErrors = error.getErrors("customer", "email");
                assertTrue(emailErrors.containsKey("invalid"));
                assertEquals("is invalid", emailErrors.get("invalid"));

                assertEquals(422, error.getRetrofitResponse().code());

                latch.countDown();
            }
        });

        latch.await();

    }

    @Test
    public void testActivateCustomer() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.activateCustomer(-1L, "test_activate_customer", new AccountCredentials("test_activate_customer"), new Callback<Customer>() {
            @Override
            public void success(Customer response) {
                fail("Well it's unexpected as probably this customer shouldn't exist");
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    public void testResetPassword() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        buyClient.resetPassword(-1L, "test_reset_password", new AccountCredentials("test_reset_password"), new Callback<Customer>() {
            @Override
            public void success(Customer response) {
                fail("Well it's unexpected as probably this customer shouldn't exist");
                latch.countDown();
            }

            @Override
            public void failure(BuyClientError error) {
                latch.countDown();
            }
        });
        latch.await();
    }
}
