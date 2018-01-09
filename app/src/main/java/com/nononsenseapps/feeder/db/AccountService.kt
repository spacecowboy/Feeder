package com.nononsenseapps.feeder.db

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.NetworkErrorException
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class AccountService : Service() {
    private var authenticator: Authenticator? = null

    override fun onCreate() {
        super.onCreate()
        this.authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator!!.iBinder
    }

    inner class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {

        override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                    s: String): Bundle {
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun addAccount(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                s: String, s2: String, strings: Array<String>, bundle: Bundle): Bundle? {
            Log.d("JONAS", "addAccount")
            // TODO
            // accountManager.addAccountExplicitly(account, null, null)
            // return bundle with type and name
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                        account: Account, bundle: Bundle): Bundle? {
            // TODO return KEY_BOOLEAN_RESULT with true
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle {
            // TODO return KEY_BOOLEAN_RESULT false
            return super.getAccountRemovalAllowed(response, account)
        }

        @Throws(NetworkErrorException::class)
        override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                  account: Account, s: String, bundle: Bundle): Bundle {
            // TODO return error
            throw UnsupportedOperationException()
        }

        override fun getAuthTokenLabel(s: String): String? {
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun updateCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                       account: Account, s: String, bundle: Bundle): Bundle {
            // TODO return type and name
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                 account: Account, strings: Array<String>): Bundle {
            // TODO check features and respond accordingly
            throw UnsupportedOperationException()
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

