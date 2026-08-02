package com.example.b07taam2026;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, manifest = Config.NONE)
public class ExampleUnitTest {

    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private DatabaseReference mockDatabase;
    @Mock
    private AuthManager.AuthCallback authCallback;

    @Mock
    private SignUpPresenter.View mockSignUpView;
    @Mock
    private LoginPresenter.View mockLoginView;
    @Mock
    private SignUpManager mockSignUpManager;
    @Mock
    private AuthManager mockAuthManager;
    @Mock
    private RoleManager mockRoleManager;

    private AuthManager authManager;
    private RoleManager roleManager;
    private SignUpPresenter signUpPresenter;
    private LoginPresenter loginPresenter;
    private AutoCloseable closeable;

    @Config(
            sdk = 33,
            manifest = Config.NONE,
            instrumentedPackages = {"com.example.b07taam2026"}
    )
    @Before
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);

        authManager = new AuthManager(mockAuth);
        roleManager = new RoleManager(mockDatabase);

        signUpPresenter = new SignUpPresenter(mockSignUpView, mockSignUpManager);
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
    }


    @Test
    public void testLoginSuccess() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass123", true);

        verify(mockAuthManager).login(eq("test@test.com"), eq("pass123"), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleCallback.class);
        verify(mockRoleManager).isAdmin(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onResult(true);

        ArgumentCaptor<RoleManager.UsernameCallback> userCaptor = ArgumentCaptor.forClass(RoleManager.UsernameCallback.class);
        verify(mockRoleManager).fetchUsername(eq("uid123"), userCaptor.capture());
        userCaptor.getValue().onResult("JohnDoe");

        verify(mockLoginView).navigateToHome(true, "uid123", "JohnDoe");
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
    public void testLoginRoleError() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "password123", false);

        verify(mockAuthManager).login(anyString(), anyString(), authCaptor.capture());
        authCaptor.getValue().onSuccess("uid123");

        ArgumentCaptor<RoleManager.RoleCallback> roleCaptor = ArgumentCaptor.forClass(RoleManager.RoleCallback.class);
        verify(mockRoleManager).isAdmin(eq("uid123"), roleCaptor.capture());
        roleCaptor.getValue().onError("Database Offline");

        verify(mockLoginView).setLoginEnabled(true);
        verify(mockLoginView).showError("Role error: Database Offline");
    }

    @Test
    public void testLoginDetachDuringAuth() {
        ArgumentCaptor<AuthManager.AuthCallback> authCaptor = ArgumentCaptor.forClass(AuthManager.AuthCallback.class);
        loginPresenter.login("test@test.com", "pass", false);
        verify(mockAuthManager).login(anyString(), anyString(), authCaptor.capture());

        loginPresenter.detachView(); // DETACH
        authCaptor.getValue().onFailure("error");

        verify(mockLoginView, never()).showError(anyString());
    }

    // SignUp Presenter Tests
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
