package com.faunadb.app

import android.app.Activity
import android.os.AsyncTask
import android.util.Log

import com.faunadb.client.FaunaClient
import com.faunadb.client.query.Expr
import com.faunadb.client.types.Value

import java.net.MalformedURLException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.function.Consumer
import java.util.stream.Collectors

import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory

import com.faunadb.client.query.Language.*

class InspectFaunaDBTask(
        private val source: Activity,
        private val callback: Consumer<Collection<FaunaResource>>?
            ) : AsyncTask<String, Void, Collection<FaunaResource>>() {

    private var cli: FaunaClient? = null

    override fun doInBackground(vararg strings: String): Collection<FaunaResource> {
        if (strings.isEmpty())
            return emptyList()

        return try {
            cli = newClient()

            strings.map { r -> FaunaResource(r, getResourceData(cli, getExpr(r))) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            if (cli != null)
                cli!!.close()
        }
    }

    private fun getExpr(resourceName: String): Expr {
        return when (resourceName.trim { it <= ' ' }.toLowerCase()) {
            "indexes" -> Indexes()
            "databases" -> Databases()
            else -> Classes()
        }
    }


    private fun getResourceData(cli: FaunaClient?, exp: Expr): List<String> {
        return try {
            val value = cli!!.query(Paginate(exp)).get()
            val data = value.at("data").asCollectionOf(Value.RefV::class.java).get()
            val dataStr = data.map { s -> s.toString() }

            Log.i("FAUNA", "Result from db: $value")
            Log.i("FAUNA", "Result data: $data")

            dataStr
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

    }

    @Throws(MalformedURLException::class)
    private fun newClient(): FaunaClient {
        return FaunaClient.builder()
                .withEndpoint(source.resources.getString(R.string.fauna_endpoint))
                .withSecret(source.resources.getString(R.string.fauna_secret))
                .build()
    }

    override fun onPostExecute(resources: Collection<FaunaResource>) {
        callback?.accept(resources)
    }

    companion object {

        init {
            InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
        }
    }
}
