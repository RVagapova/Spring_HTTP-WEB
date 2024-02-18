package ru.netology;

import org.apache.hc.core5.http.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    //http://localhost:63342/http-server/01_web/http-server/public/forms.html?login=1&password=2
    private String method;
    private List<String> headers;
    private String body;
    private String path;
    private List<NameValuePair> params;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    public Map<String, List<String>> getQueryParams() {
        Map<String, List<String>> queryParams = new HashMap<>();
        for (NameValuePair param : params) {
            if (queryParams.containsKey(param.getName())) {
                queryParams.get(param.getName()).add(param.getValue());
            } else {
                List<String> values = new ArrayList<>();
                values.add(param.getValue());
                queryParams.put(param.getName(), values);
            }
        }
        return queryParams;
    }

    public String getQueryParam(String name) {
        return params.stream()
                .filter(par -> par.getName().equals(name))
                .findFirst()
                .get().getValue();
    }

}
