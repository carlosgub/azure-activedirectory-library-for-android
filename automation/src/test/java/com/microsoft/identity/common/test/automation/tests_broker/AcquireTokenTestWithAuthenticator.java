//  Copyright (c) Microsoft Corporation.
//  All rights reserved.
//
//  This code is licensed under the MIT License.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files(the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions :
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

package com.microsoft.identity.common.test.automation.tests_broker;

import com.microsoft.identity.common.test.automation.actors.User;
import com.microsoft.identity.common.test.automation.model.Constants;
import com.microsoft.identity.common.test.automation.questions.AccessTokenFromAuthenticationResult;
import com.microsoft.identity.common.test.automation.tasks.AcquireToken;
import com.microsoft.identity.common.test.automation.tasks.authenticatorapp.WorkplaceLeave;
import com.microsoft.identity.common.test.automation.ui.Results;
import com.microsoft.identity.common.test.automation.utility.Scenario;
import com.microsoft.identity.common.test.automation.utility.TestConfigurationQuery;

import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import net.thucydides.junit.annotations.TestData;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import io.appium.java_client.service.local.AppiumDriverLocalService;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SerenityParameterizedRunner.class)
public class AcquireTokenTestWithAuthenticator {

    @TestData
    public static Collection<Object[]> FederationProviders() {


        return Arrays.asList(new Object[][]{
                {"ADFSv2"}
        });

    }

    static AppiumDriverLocalService appiumService = null;

    @Managed(driver = "Appium")
    WebDriver hisMobileDevice;

    @BeforeClass
    public static void startAppiumServer() throws IOException {
        appiumService = AppiumDriverLocalService.buildDefaultService();
        appiumService.start();
    }

    @AfterClass
    public static void stopAppiumServer() {
        appiumService.stop();
    }

    private User james;
    private String federationProvider;

    @Steps
    AcquireToken acquireToken;

    @Steps
    WorkplaceLeave workplaceLeave;


    public AcquireTokenTestWithAuthenticator(String federationProvider) {
        this.federationProvider = federationProvider;
    }

    @Before
    public void jamesCanUseAMobileDevice() {
        TestConfigurationQuery query = new TestConfigurationQuery();
        query.federationProvider = this.federationProvider;
        query.isFederated = true;
        query.userType = "Member";
        james = getUser(query);
        james.can(BrowseTheWeb.with(hisMobileDevice));
    }

    private static User getUser(TestConfigurationQuery query) {

        Scenario scenario = Scenario.GetScenario(query);

        User newUser = User.named("james");
        newUser.setFederationProvider(scenario.getTestConfiguration().getUsers().getFederationProvider());
        newUser.setTokenRequest(scenario.getTokenRequest());
        newUser.getTokenRequest().setRedirectUri(Constants.AUTOMATION_TEST_APP_BROKER_REDIRECT_URI);
        newUser.setCredential(scenario.getCredential());
        newUser.setSilentTokenRequest(scenario.getSilentTokenRequest());
        newUser.getSilentTokenRequest().setRedirectUri(Constants.AUTOMATION_TEST_APP_BROKER_REDIRECT_URI);
        newUser.setWorkplaceJoined(false);
        return newUser;
    }

    @Test
    public void should_be_able_to_acquire_token_with_broker() {

        givenThat(james).wasAbleTo(
                acquireToken.withBroker(),
                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
                );

        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), notNullValue()));

        james.attemptsTo(workplaceLeave);
    }

    @Test
    public void should_be_able_to_acquire_token_with_broker_with_prompt_always() {

        givenThat(james).wasAbleTo(
                acquireToken
                        .withBroker()
                        .withPrompt("Always"),
                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
        );

        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), notNullValue()));

        james.attemptsTo(workplaceLeave);
    }

    @Test
    public void should_be_able_to_acquire_token_with_broker_with_prompt_null() {

        givenThat(james).wasAbleTo(
                acquireToken
                        .withBroker()
                        .withPrompt(null),
                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
        );

        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), notNullValue()));

        james.attemptsTo(workplaceLeave);
    }

    @Test
    public void should_be_able_to_acquire_token_with_broker_with_prompt_auto() {

        givenThat(james).wasAbleTo(
                acquireToken
                        .withBroker()
                        .withPrompt("Auto"),
                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
        );

        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), notNullValue()));

        james.attemptsTo(workplaceLeave);
    }

    @Test
    public void should_be_able_to_acquire_token_with_broker_with_login_hint() {

        givenThat(james).wasAbleTo(
                acquireToken
                        .withBroker()
                        .withUserIdentifier(james.getCredential().userName),
                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
        );

        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), notNullValue()));

        james.attemptsTo(workplaceLeave);
    }

    //TODO : This test is currently broken as we need to fix tap account in authenticator app.Enable this once it's fixed
//    @Test
//    public void should_be_able_to_acquire_token_with_force_prompt() {
//
//        givenThat(james).wasAbleTo(
//                acquireToken
//                        .withBroker()
//                        .withPrompt("Auto"),
//                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds()
//        );
//
//        String accessToken1 = james.asksFor(AccessTokenFromAuthenticationResult.displayed());
//
//        james.setWorkplaceJoined(true);
//
//        when(james).attemptsTo(
//                clickDone,
//                acquireToken
//                        .withBroker()
//                        .withPrompt("FORCE_PROMPT")
//                        .withUserIdentifier(null),
//                WaitUntil.the(Results.RESULT_FIELD, isVisible()).forNoMoreThan(10).seconds());
//
//        then(james).should(seeThat(AccessTokenFromAuthenticationResult.displayed(), not(accessToken1)));
//
//        james.attemptsTo(workplaceLeave);
//
//    }

}