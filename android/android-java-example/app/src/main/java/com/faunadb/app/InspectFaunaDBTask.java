package com.faunadb.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import static com.faunadb.client.query.Language.*;

public class InspectFaunaDBTask extends AsyncTask<String, Void, Collection<FaunaResource>> {

    static {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
    }

    private FaunaClient cli;
    private Activity source;
    private Consumer<Collection<FaunaResource>> callback;

    public InspectFaunaDBTask(Activity source, Consumer<Collection<FaunaResource>> callback) {
        this.source = source;
        this.callback = callback;
        this.cli = null;
    }

    @Override
    protected Collection<FaunaResource> doInBackground(String... strings) {
        if (strings.length == 0)
            return Collections.emptyList();

        try {
            cli = newClient();

            return Arrays.stream(strings)
                    .map(r -> new FaunaResource(r, getResourceData(cli, getExpr(r))))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (cli != null)
                cli.close();
        }
    }

    private Expr getExpr(String resourceName) {
        switch (resourceName.trim().toLowerCase()) {
            case "indexes":
                return Indexes();
            case "databases":
                return Databases();
            default:
                return Classes();
        }
    }


    private List<String> getResourceData(FaunaClient cli, Expr exp) {
        try {
            Value value = cli.query(Paginate(exp)).get();
            Collection<Value.RefV> data = value.at("data").asCollectionOf(Value.RefV.class).get();
            List<String> dataStr = data.stream().map(s -> s.toString()).collect(Collectors.toList());

            Log.i("FAUNA", String.format("Result from db: %s", value.toString()));
            Log.i("FAUNA", String.format("Result data: %s", data.toString()));

            return dataStr;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private FaunaClient newClient() throws MalformedURLException {
        return FaunaClient.builder()
                .withEndpoint(source.getResources().getString(R.string.fauna_endpoint))
                .withSecret(source.getResources().getString(R.string.fauna_secret))
                .build();
    }

    @Override
    protected void onPostExecute(Collection<FaunaResource> resources) {
        if (callback != null)
            callback.accept(resources);
    }
}
