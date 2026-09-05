package org.chimeramc.launcher.core.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.microsoft.identity.client.AcquireTokenParameters;
import com.microsoft.identity.client.IAuthenticationCallback;
import com.microsoft.identity.client.ICurrentAccount;
import com.microsoft.identity.client.ILifecycleCallback;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Microsoft OAuth Integration using official MSAL library
 * Provides official Microsoft login flows for Chimera Launcher
 */
public class MicrosoftOAuthManager {
    private static final String TAG = "MicrosoftOAuth";
    
    // Microsoft Azure App Registration Configuration
    // Users must register their own app at https://portal.azure.com
    private static final String DEFAULT_CLIENT_ID = "YOUR_CLIENT_ID_HERE";
    private static final String[] DEFAULT_SCOPES = {"XboxLive.signIn", "offline_access"};
    private static final String REDIRECT_URI_SCHEME = "msauth";
    
    private IMultipleAccountPublicClientApplication multiAccountApp;
    private ISingleAccountPublicClientApplication singleAccountApp;
    private final Context context;
    private boolean isInitialized = false;
    
    public MicrosoftOAuthManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Initialize MSAL Public Client Application
     * Must be called before any authentication operations
     */
    public void initialize(@NonNull String clientId, @NonNull String redirectUri) {
        if (isInitialized) {
            Log.w(TAG, "MSAL already initialized");
            return;
        }
        
        String configJson = createMsalConfig(clientId, redirectUri);
        
        try {
            File configFile = new File(context.getFilesDir(), "msal_config.json");
            java.io.FileWriter writer = new java.io.FileWriter(configFile);
            writer.write(configJson);
            writer.close();
            
            PublicClientApplication.createMultipleAccountPublicClientApplication(
                context,
                configFile.getAbsolutePath(),
                new IMultipleAccountPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        multiAccountApp = application;
                        isInitialized = true;
                        Log.i(TAG, "MSAL Multi-account app initialized successfully");
                    }
                    
                    @Override
                    public void onError(MsalException exception) {
                        Log.e(TAG, "Failed to initialize MSAL multi-account app", exception);
                    }
                }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error creating MSAL configuration", e);
        }
    }
    
    /**
     * Create MSAL configuration JSON programmatically
     */
    private String createMsalConfig(String clientId, String redirectUri) {
        return "{\n" +
                "  \"authorization_user_agent\": \"DEFAULT\",\n" +
                "  \"minimum_required_broker_protocol_version\": \"3.0\",\n" +
                "  \"account_mode\": \"MULTIPLE\",\n" +
                "  \"broker_redirect_uri_registered\": false,\n" +
                "  \"environment\": \"Production\",\n" +
                "  \"client_id\": \"" + clientId + "\",\n" +
                "  \"redirect_uri\": \"" + redirectUri + "\",\n" +
                "  \"authorities\": [\n" +
                "    {\n" +
                "      \"type\": \"AAD\",\n" +
                "      \"audience\": {\n" +
                "        \"type\": \"PersonalAzureAD\",\n" +
                "        \"tenant_id\": \"common\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"AAD\",\n" +
                "      \"audience\": {\n" +
                "        \"type\": \"PersonalAzureAD\",\n" +
                "        \"tenant_id\": \"organizations\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"AAD\",\n" +
                "      \"audience\": {\n" +
                "        \"type\": \"PersonalAzureAD\",\n" +
                "        \"tenant_id\": \"consumers\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
    
    /**
     * Start interactive sign-in flow with Microsoft
     * @param activity The calling activity for launching browser
     * @param callback Callback for authentication result
     */
    public void signIn(@NonNull Activity activity, @NonNull MicrosoftAuthCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("MSAL not initialized. Call initialize() first."));
            return;
        }
        
        AcquireTokenParameters parameters = new AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .loginHint("")
                .withScopes(Arrays.asList(DEFAULT_SCOPES))
                .withCallback(new IAuthenticationCallback() {
                    @Override
                    public void onSuccess(com.microsoft.identity.client.IAuthenticationResult authenticationResult) {
                        Log.i(TAG, "Successfully authenticated: " + authenticationResult.getAccount().getUsername());
                        callback.onSuccess(authenticationResult);
                    }
                    
                    @Override
                    public void onError(MsalException exception) {
                        Log.e(TAG, "Authentication failed", exception);
                        callback.onError(exception);
                    }
                    
                    @Override
                    public void onCancel() {
                        Log.i(TAG, "Authentication cancelled by user");
                        callback.onCancelled();
                    }
                })
                .build();
        
        multiAccountApp.acquireToken(parameters);
    }
    
    /**
     * Silent sign-in using cached credentials
     * @param callback Callback for authentication result
     */
    public void signInSilently(@NonNull MicrosoftAuthCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("MSAL not initialized"));
            return;
        }
        
        multiAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(ICurrentAccount activeAccount) {
                if (activeAccount != null) {
                    acquireTokenSilent(activeAccount, callback);
                } else {
                    callback.onError(new MsalUiRequiredException("no_account", "No account found"));
                }
            }
            
            @Override
            public void onAccountChanged(@NonNull ICurrentAccount priorAccount, @NonNull ICurrentAccount currentAccount) {
                if (currentAccount != null) {
                    acquireTokenSilent(currentAccount, callback);
                }
            }
            
            @Override
            public void onAccountRemoved(@NonNull ICurrentAccount removedAccount) {
                callback.onError(new MsalUiRequiredException("account_removed", "Account was removed"));
            }
        });
    }
    
    private void acquireTokenSilent(ICurrentAccount account, MicrosoftAuthCallback callback) {
        multiAccountApp.acquireTokenSilentAsync(
            Arrays.asList(DEFAULT_SCOPES),
            account.getAccountIdentifier(),
            null,
            false,
            new IAuthenticationCallback() {
                @Override
                public void onSuccess(com.microsoft.identity.client.IAuthenticationResult authenticationResult) {
                    callback.onSuccess(authenticationResult);
                }
                
                @Override
                public void onError(MsalException exception) {
                    callback.onError(exception);
                }
                
                @Override
                public void onCancel() {
                    callback.onCancelled();
                }
            }
        );
    }
    
    /**
     * Sign out and remove account from MSAL
     */
    public void signOut(@NonNull MicrosoftAuthCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("MSAL not initialized"));
            return;
        }
        
        multiAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(ICurrentAccount activeAccount) {
                if (activeAccount != null) {
                    multiAccountApp.removeAccountAsync(activeAccount.getAccountIdentifier(), 
                        new ISingleAccountPublicClientApplication.RemoveAccountCallback() {
                            @Override
                            public void onRemoved() {
                                Log.i(TAG, "Account removed successfully");
                                callback.onSuccess(null);
                            }
                            
                            @Override
                            public void onError(MsalException exception) {
                                callback.onError(exception);
                            }
                        });
                }
            }
            
            @Override
            public void onAccountChanged(@NonNull ICurrentAccount priorAccount, @NonNull ICurrentAccount currentAccount) {}
            
            @Override
            public void onAccountRemoved(@NonNull ICurrentAccount removedAccount) {}
        });
    }
    
    /**
     * Get list of signed in accounts
     */
    public void getAccounts(@NonNull AccountsCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("MSAL not initialized"));
            return;
        }
        
        multiAccountApp.getAccountsAsync(new ISingleAccountPublicClientApplication.GetAccountsCallback() {
            @Override
            public void onTaskCompleted(List<ICurrentAccount> resultList) {
                callback.onSuccess(resultList);
            }
            
            @Override
            public void onError(MsalException exception) {
                callback.onError(exception);
            }
        });
    }
    
    /**
     * Check if any account is currently signed in
     */
    public void isSignedIn(@NonNull IsSignedInCallback callback) {
        getAccounts(new AccountsCallback() {
            @Override
            public void onSuccess(List<ICurrentAccount> accounts) {
                callback.onResult(!accounts.isEmpty());
            }
            
            @Override
            public void onError(MsalException exception) {
                callback.onResult(false);
            }
        });
    }
    
    /**
     * Callback interface for authentication results
     */
    public interface MicrosoftAuthCallback {
        void onSuccess(com.microsoft.identity.client.IAuthenticationResult result);
        void onError(MsalException exception);
        void onCancelled();
    }
    
    /**
     * Callback interface for account list
     */
    public interface AccountsCallback {
        void onSuccess(List<ICurrentAccount> accounts);
        void onError(MsalException exception);
    }
    
    /**
     * Simple callback for boolean result
     */
    public interface IsSignedInCallback {
        void onResult(boolean isSignedIn);
    }
}
