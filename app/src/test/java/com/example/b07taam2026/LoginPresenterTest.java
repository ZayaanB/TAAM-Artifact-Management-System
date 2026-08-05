package com.example.b07taam2026;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginPresenterTest {
    @Mock
    private LoginPresenter.View mockLoginView;
    @Mock
    private AuthManager mockAuthManager;
    @Mock
    private RoleManager mockRoleManager;
    private LoginPresenter loginPresenter;
    private AutoCloseable closeable;

    @Before
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        loginPresenter = new LoginPresenter(mockLoginView, mockAuthManager, mockRoleManager);
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testLoginEmptyFields() {
        loginPresenter.login("", "password123", true);
        verify(mockLoginView).showError("Enter both fields");
        verify(mockAuthManager, never()).login(anyString(), anyString(), any());
        verify(mockLoginView, never()).setLoginEnabled(anyBoolean());
    }

    @Test
    public void testLoginEmptyPassword() {
        loginPresenter.login("test@test.com", "", false);
        verify(mockLoginView).showError("Enter both fields");
        verify(mockAuthManager, never()).login(anyString(), anyString(), any());
        verify(mockLoginView, never()).setLoginEnabled(anyBoolean());
    }

    @Test
    public void testLoginSuccess() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass123", true);

        verify(mockAuthManager).login(eq("test@test.com"), eq("pass123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleStringCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleStringCallback.class);
        verify(mockRoleManager).fetchRole(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onResult("admin");

        ArgumentCaptor<RoleManager.UsernameCallback> userCaptor = ArgumentCaptor.forClass(RoleManager.UsernameCallback.class);
        verify(mockRoleManager).fetchUsername(eq("uid123"), userCaptor.capture());
        userCaptor.getValue().onResult("JohnDoe");

        verify(mockLoginView).navigateToHome("admin", "uid123", "JohnDoe");
    }

    @Test
    public void testLoginFailure() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "wrong", false);

        verify(mockAuthManager).login(eq("test@test.com"), eq("wrong"), authCaptor.capture());
        authCaptor.getValue().onFailure("Invalid password");

        verify(mockLoginView).setLoginEnabled(true);
        verify(mockLoginView).showError("Login failed: Invalid password");
    }

    @Test
    public void testAutoLoginSuccess() {
        when(mockAuthManager.isLoggedIn()).thenReturn(true);
        when(mockAuthManager.getCurrentUid()).thenReturn("uid123");
        loginPresenter.tryAutoLogin(true);
        verify(mockLoginView).setLoginEnabled(false);
    }

    @Test
    public void testAutoLoginDisabled() {
        loginPresenter.tryAutoLogin(false);
        verify(mockLoginView, never()).setLoginEnabled(anyBoolean());
    }

    @Test
    public void testAutoLoginNotLoggedIn() {
        when(mockAuthManager.isLoggedIn()).thenReturn(false);
        loginPresenter.tryAutoLogin(true);
        verify(mockLoginView, never()).setLoginEnabled(anyBoolean());
        verify(mockRoleManager, never()).fetchRole(anyString(), any());
    }

    @Test
    public void testLoginRoleError() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "password123", false);

        verify(mockAuthManager).login(eq("test@test.com"), eq("password123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleStringCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleStringCallback.class);
        verify(mockRoleManager).fetchRole(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onError("Database Offline");

        verify(mockLoginView).setLoginEnabled(true);
        verify(mockLoginView).showError("Role error: Database Offline");
    }

    @Test
    public void testLoginDetachDuringAuth() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass", false);
        verify(mockAuthManager).login(eq("test@test.com"), eq("pass"), authCaptor.capture());

        loginPresenter.detachView(); // DETACH
        authCaptor.getValue().onFailure("error");

        verify(mockLoginView, never()).showError(anyString());
        verify(mockLoginView, never()).setLoginEnabled(true);
    }

    @Test
    public void testLoginDetachDuringAuthSuccess() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass", true);
        verify(mockAuthManager).login(eq("test@test.com"), eq("pass"), authCaptor.capture());

        loginPresenter.detachView(); // DETACH
        authCaptor.getValue().onSuccess("uid123");

        verify(mockLoginView, never()).persistKeepSignedIn(anyBoolean());
        verify(mockLoginView, never()).setLoginEnabled(true);
        verify(mockLoginView, never()).navigateToHome(anyString(), anyString(), anyString());
    }

    @Test
    public void testLoginDetachDuringRoleFetch() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "password123", false);
        verify(mockAuthManager).login(eq("test@test.com"), eq("password123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleStringCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleStringCallback.class);
        verify(mockRoleManager).fetchRole(eq("uid123"), roleCaptor.capture());

        loginPresenter.detachView(); // DETACH
        roleCaptor.getValue().onError("Database Offline");

        verify(mockLoginView, never()).showError(anyString());
        verify(mockLoginView, never()).setLoginEnabled(true);
    }

    @Test
    public void testLoginDetachDuringUsernameFetch() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "password123", false);
        verify(mockAuthManager).login(eq("test@test.com"), eq("password123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleStringCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleStringCallback.class);
        verify(mockRoleManager).fetchRole(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onResult("user");

        ArgumentCaptor<RoleManager.UsernameCallback> userCaptor = ArgumentCaptor.forClass(RoleManager.UsernameCallback.class);
        verify(mockRoleManager).fetchUsername(eq("uid123"), userCaptor.capture());

        loginPresenter.detachView(); // DETACH
        userCaptor.getValue().onResult("JohnDoe");

        verify(mockLoginView, never()).navigateToHome(anyString(), anyString(), anyString());
        verify(mockLoginView, never()).setLoginEnabled(true);
    }

    @Test
    public void testLoginSuccessNullUsername() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass123", false);

        verify(mockAuthManager).login(eq("test@test.com"), eq("pass123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleStringCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleStringCallback.class);
        verify(mockRoleManager).fetchRole(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onResult("user");

        ArgumentCaptor<RoleManager.UsernameCallback> userCaptor = ArgumentCaptor.forClass(RoleManager.UsernameCallback.class);
        verify(mockRoleManager).fetchUsername(eq("uid123"), userCaptor.capture());
        userCaptor.getValue().onResult(null); // null username → fallback

        verify(mockLoginView).navigateToHome("user", "uid123", "test@test.com");
    }
}
