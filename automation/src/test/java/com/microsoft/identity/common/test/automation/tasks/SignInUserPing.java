package com.microsoft.identity.common.test.automation.tasks;

import com.microsoft.identity.common.test.automation.actors.User;
import com.microsoft.identity.common.test.automation.interactions.CloseKeyboard;
import com.microsoft.identity.common.test.automation.ui.identityproviders.PingFederate.SignInPage;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.EnterValueIntoTarget;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;


public class SignInUserPing extends SignInUser {


    @Override
    public <T extends Actor> void performAs(T actor) {
        User user = (User)actor;
        user.attemptsTo(
                WaitUntil.the(SignInPage.USERNAME_FIELD, isVisible()).forNoMoreThan(10).seconds(),
                Enter.theValue(user.getCredential().userName.split("@")[0]).into(SignInPage.USERNAME_FIELD),
                //Not using static method here to avoid logging the password via instrumentation... this won't show up as a step
                new EnterValueIntoTarget(user.getCredential().password, SignInPage.PASSWORD_FIELD),
                new CloseKeyboard(),
                WaitUntil.the(SignInPage.SIGN_IN_BUTTON, isVisible()).forNoMoreThan(10).seconds(),
                Click.on(SignInPage.SIGN_IN_BUTTON)
        );
    }
}