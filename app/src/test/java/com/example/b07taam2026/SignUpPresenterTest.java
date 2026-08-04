package com.example.b07taam2026;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        verify(mockSignUpManager, never()).register(anyString(), anyString(), anyString(), any());
        verify(mockSignUpView, never()).setCreateEnabled(anyBoolean());
    }

    @Test
    public void testSignUpEmptyUsername() {
        signUpPresenter.signUp("test@test.com", "", "pass123", "pass123");
        verify(mockSignUpView).showError("Please fill out all fields");
        verify(mockSignUpManager, never()).register(anyString(), anyString(), anyString(), any());
        verify(mockSignUpView, never()).setCreateEnabled(anyBoolean());
    }

    @Test
    public void testSignUpEmptyPassword() {
        signUpPresenter.signUp("test@test.com", "user", "", "");
        verify(mockSignUpView).showError("Please fill out all fields");
        verify(mockSignUpManager, never()).register(anyString(), anyString(), anyString(), any());
        verify(mockSignUpView, never()).setCreateEnabled(anyBoolean());
    }

    @Test
    public void testSignUpShortPassword() {
        signUpPresenter.signUp("a@b.com", "user", "123", "123");
        verify(mockSignUpView).showError("Password must be at least 6 characters");
        verify(mockSignUpManager, never()).register(anyString(), anyString(), anyString(), any());
        verify(mockSignUpView, never()).setCreateEnabled(anyBoolean());
    }

    @Test
    public void testSignupDifferentPassword(){
        signUpPresenter.signUp("a@b.com", "user", "password", "different");
        verify(mockSignUpView).showError("Passwords do not match");
        verify(mockSignUpManager, never()).register(anyString(), anyString(), anyString(), any());
        verify(mockSignUpView, never()).setCreateEnabled(anyBoolean());
    }

    @Test
    public void testSignUpSuccess() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpManager).register(eq("test@test.com"), eq("John"), eq("password123"), captor.capture());

        captor.getValue().onSuccess("uid123", "John");
        verify(mockSignUpView).showMessage("Account created!");
        verify(mockSignUpView).navigateToHome("uid123", "John");
        verify(mockSignUpView, never()).showError(anyString());
    }

    @Test
    public void testSignUpFailure() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpManager).register(eq("test@test.com"), eq("John"), eq("password123"), captor.capture());

        captor.getValue().onFailure("Network Error");

        verify(mockSignUpView).setCreateEnabled(true);
        verify(mockSignUpView).showError("Sign up failed: Network Error");
        verify(mockSignUpView, never()).navigateToHome(anyString(), anyString());
    }

    @Test
    public void testSignUpDetachViewOnSuccess() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);
        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");
        verify(mockSignUpManager).register(eq("test@test.com"), eq("John"), eq("password123"), captor.capture());

        signUpPresenter.detachView();

        captor.getValue().onSuccess("uid123", "John");

        verify(mockSignUpView, never()).showMessage(anyString());
        verify(mockSignUpView, never()).navigateToHome(anyString(), anyString());
    }

    @Test
    public void testSignUpDetachViewOnFailure() {
        ArgumentCaptor<SignUpManager.SignUpCallback> captor = ArgumentCaptor.forClass(SignUpManager.SignUpCallback.class);

        signUpPresenter.signUp("test@test.com", "John", "password123", "password123");
        verify(mockSignUpManager).register(eq("test@test.com"), eq("John"), eq("password123"), captor.capture());

        signUpPresenter.detachView();

        captor.getValue().onFailure("Server Error");

        verify(mockSignUpView).setCreateEnabled(false);
        verify(mockSignUpView, never()).setCreateEnabled(true);
        verify(mockSignUpView, never()).showError(anyString());
    }
}
