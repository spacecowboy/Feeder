package com.nononsenseapps.feeder.db

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.setBoolean
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString
import com.nononsenseapps.feeder.util.setupSync

private const val TAG = "FEEDERACCOUNT"

class AccountService : Service() {
    private var authenticator: Authenticator? = null

    override fun onCreate() {
        super.onCreate()
        this.authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator!!.iBinder
    }

    inner class Authenticator(val context: Context) : AbstractAccountAuthenticator(context) {

        override fun editProperties(response: AccountAuthenticatorResponse,
                                    accountType: String): Bundle {
            Log.d(TAG, "editProperties")
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun addAccount(response: AccountAuthenticatorResponse,
                                accountType: String,
                                authTokenType: String?,
                                requiredFeatures: Array<String>?,
                                options: Bundle): Bundle {
            Log.d(TAG, "addAccount")
            context.setupSync()
            return bundle {
                setString(AccountManager.KEY_ACCOUNT_NAME to ACCOUNT_NAME)
                setString(AccountManager.KEY_ACCOUNT_TYPE to ACCOUNT_TYPE)
            }
        }

        @Throws(NetworkErrorException::class)
        override fun confirmCredentials(response: AccountAuthenticatorResponse,
                                        account: Account,
                                        options: Bundle): Bundle? {
            Log.d(TAG, "confirmCredentials")
            return bundle {
                setBoolean(AccountManager.KEY_BOOLEAN_RESULT to true)
            }
        }

        @Throws(NetworkErrorException::class)
        override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle {
            Log.d(TAG, "getAccountRemovalAllowed")
            return bundle {
                setBoolean(AccountManager.KEY_BOOLEAN_RESULT to true)
            }
        }

        @Throws(NetworkErrorException::class)
        override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                  account: Account, accountType: String, options: Bundle): Bundle {
            Log.d(TAG, "getAuthToken")
            return bundle {
                setLong(AccountManager.KEY_ERROR_CODE to 99L)
                setString(AccountManager.KEY_ERROR_MESSAGE to "Auth token not supported")
            }
        }

        override fun getAuthTokenLabel(s: String): String? {
            Log.d(TAG, "getAuthTokenLabel")
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun updateCredentials(response: AccountAuthenticatorResponse,
                                       account: Account,
                                       authTokenType: String?,
                                       options: Bundle?): Bundle {
            Log.d(TAG, "updateCredentials")
            return bundle {
                setString(AccountManager.KEY_ACCOUNT_NAME to ACCOUNT_NAME)
                setString(AccountManager.KEY_ACCOUNT_TYPE to ACCOUNT_TYPE)
            }
        }

        @Throws(NetworkErrorException::class)
        override fun hasFeatures(response: AccountAuthenticatorResponse,
                                 account: Account,
                                 features: Array<String>): Bundle {
            Log.d(TAG, "hasFeatures")
            return bundle {
                setBoolean(AccountManager.KEY_BOOLEAN_RESULT to false)
            }
        }
    }

    companion object {
        private val ACCOUNT_NAME = "feeder"
        private val ACCOUNT_TYPE = "com.nononsenseapps.feeder.account"

        fun Account(): Account {
            return Account(ACCOUNT_NAME, ACCOUNT_TYPE)
        }
    }
}

