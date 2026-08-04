package com.example.b07taam2026;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SignUpPresenterTest {
    @Mock
    private SignUpPresenter.View mockSignUpView;
    @Mock
    private SignUpManager mockSignUpManager;
    private SignUpPresenter signUpPresenter;
    private AutoCloseable closeable;

    @Before
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        signUpPresenter = new SignUpPresenter(mockSignUpView, mockSignUpManager);
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testSignUpEmptyEmail() {
        signUpPresenter.signUp("", "user", "pass123", "pass123");
        verify(mockSignUpView).showError("Please fill out all fields");
    }

    @Test
    public void testSignUpShortPassword() {
        signUpPresenter.signUp("a@b.com", "user", "123", "123");
        verify(mockSignUpView).showError("Password must be at least 6 characters");
    }

    @Test
    public void testSignupDifferentPassword(){
        signUpPresenter.signUp("a@b.com", "user", "password", "different");
        verify(mockSignUpView).showError("Passwords do not match");
    }

    @Test
    public void testSignUpSuccess() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpManager).register(anyString(), anyString(), anyString(), captor.capture());

        captor.getValue().onSuccess("uid123", "John");
        verify(mockSignUpView).showMessage("Account created!");
        verify(mockSignUpView).navigateToHome("uid123", "John");
    }

    @Test
    public void testSignUpFailure() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpManager).register(anyString(), anyString(), anyString(), captor.capture());

        captor.getValue().onFailure("Network Error");
        verify(mockSignUpView).setCreateEnabled(true);
        verify(mockSignUpView).showError("Sign up failed: Network Error");
    }

    @Test
    public void testSignUpDetachViewOnSuccess() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");
        verify(mockSignUpManager).register(anyString(), anyString(), anyString(), captor.capture());

        signUpPresenter.detachView();

        captor.getValue().onSuccess("uid123", "John");

        verify(mockSignUpView, never()).showMessage(anyString());
        verify(mockSignUpView, never()).navigateToHome(anyString(), anyString());
    }

    @Test
    public void testSignUpDetachViewOnFailure() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);

        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");
        verify(mockSignUpManager).register(anyString(), anyString(), anyString(), captor.capture());

        signUpPresenter.detachView();

        captor.getValue().onFailure("Server Error");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpView, never()).setCreateEnabled(true);
        verify(mockSignUpView, never()).showError(anyString());
    }
}
