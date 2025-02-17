package com.ringcentral;

import com.ringcentral.definitions.*;
import okhttp3.*;
import okio.BufferedSink;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class RestClient {
    public static final String SANDBOX_SERVER = "https://platform.devtest.ringcentral.com";
    public static final String PRODUCTION_SERVER = "https://platform.ringcentral.com";
    private static final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType textMediaType = MediaType.parse("text/plain; charset=utf-8");
    public String clientId;
    public String clientSecret;
    public String server;
    public OkHttpClient httpClient;
    public TokenInfo token;
    public List<HttpEventListener> httpEventListeners = new ArrayList<>();

    public RestClient(String clientId, String clientSecret, String server, OkHttpClient okHttpClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.server = server;
        if (okHttpClient == null) {
            this.httpClient = new OkHttpClient();
        } else {
            this.httpClient = okHttpClient;
        }
    }

    public RestClient(String clientId, String clientSecret, String server) {
        this(clientId, clientSecret, server, null);
    }

    public RestClient(String clientId, String clientSecret, Boolean production, OkHttpClient okHttpClient) {
        this(clientId, clientSecret, production ? PRODUCTION_SERVER : SANDBOX_SERVER, okHttpClient);
    }


    public RestClient(String clientId, String clientSecret, Boolean production) {
        this(clientId, clientSecret, production, null);
    }

    public void autoRefresh(long period) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    refresh();
                } catch (IOException | RestException e) {
                    e.printStackTrace();
                }
            }
        }, 0, period);
    }

    public void autoRefresh() {
        this.autoRefresh(1000L * 60 * 30);
    }

    public TokenInfo refresh() throws IOException, RestException {
        GetTokenRequest getTokenRequest = new GetTokenRequest()
            .grant_type("refresh_token")
            .refresh_token(token.refresh_token);
        return authorize(getTokenRequest);
    }

    private String basicKey() {
        return new String(Base64.getEncoder().encode(MessageFormat.format("{0}:{1}", clientId, clientSecret).getBytes()));
    }

    private String authorizationHeader(String endpoint) {
        if (endpoint.equals("/restapi/oauth/token") || endpoint.equals("/restapi/oauth/revoke")) {
            return MessageFormat.format("Basic {0}", basicKey());
        }
        return MessageFormat.format("Bearer {0}", token.access_token);
    }

    public void revoke() throws IOException, RestException {
        if (token == null) {
            return;
        }
        RevokeTokenRequest revokeTokenRequest = new RevokeTokenRequest().token(token.access_token);
        this.restapi(null).oauth().revoke().post(revokeTokenRequest);
        token = null;
    }

    public TokenInfo authorize(String username, String extension, String password) throws IOException, RestException {
        GetTokenRequest getTokenRequest = new GetTokenRequest()
            .grant_type("password")
            .username(username)
            .extension(extension)
            .password(password);
        return authorize(getTokenRequest);
    }

    public TokenInfo authorize(String authCode, String redirectUri) throws IOException, RestException {
        GetTokenRequest getTokenRequest = new GetTokenRequest()
            .grant_type("authorization_code")
            .code(authCode)
            .redirect_uri(redirectUri);
        return authorize(getTokenRequest);
    }

    public TokenInfo authorize(String jwt) throws IOException, RestException {
        GetTokenRequest getTokenRequest = new GetTokenRequest()
            .grant_type("urn:ietf:params:oauth:grant-type:jwt-bearer")
            .assertion(jwt);
        return authorize(getTokenRequest);
    }

    public TokenInfo authorize(GetTokenRequest getTokenRequest) throws IOException, RestException {
        token = this.restapi(null).oauth().token().post(getTokenRequest);
        return token;
    }

    public ResponseBody get(String endpoint) throws IOException, RestException {
        return request(HttpMethod.GET, endpoint, null, null);
    }

    public ResponseBody get(String endpoint, Object queryParameters) throws IOException, RestException {
        return request(HttpMethod.GET, endpoint, queryParameters, null);
    }

    public ResponseBody delete(String endpoint) throws IOException, RestException {
        return request(HttpMethod.DELETE, endpoint, null, null);
    }

    public ResponseBody delete(String endpoint, Object queryParameters) throws IOException, RestException {
        return request(HttpMethod.DELETE, endpoint, queryParameters, null);
    }

    public ResponseBody post(String endpoint) throws IOException, RestException {
        return request(HttpMethod.POST, endpoint, null, null, ContentType.JSON);
    }

    public ResponseBody post(String endpoint, Object object) throws IOException, RestException {
        return request(HttpMethod.POST, endpoint, null, object, ContentType.JSON);
    }

    public ResponseBody post(String endpoint, Object object, Object queryParameters) throws IOException, RestException {
        return request(HttpMethod.POST, endpoint, queryParameters, object, ContentType.JSON);
    }

    public ResponseBody post(String endpoint, Object object, Object queryParameters, ContentType contentType) throws IOException, RestException {
        return request(HttpMethod.POST, endpoint, queryParameters, object, contentType);
    }

    public ResponseBody put(String endpoint) throws IOException, RestException {
        return request(HttpMethod.PUT, endpoint, null, null);
    }

    public ResponseBody put(String endpoint, Object object) throws IOException, RestException {
        return request(HttpMethod.PUT, endpoint, null, object);
    }

    public ResponseBody put(String endpoint, Object object, Object queryParameters) throws IOException, RestException {
        return request(HttpMethod.PUT, endpoint, queryParameters, object);
    }

    public ResponseBody put(String endpoint, Object object, Object queryParameters, ContentType contentType) throws IOException, RestException {
        return request(HttpMethod.PUT, endpoint, queryParameters, object, contentType);
    }

    public ResponseBody patch(String endpoint, Object object) throws IOException, RestException {
        return request(HttpMethod.PATCH, endpoint, null, object);
    }

    public ResponseBody patch(String endpoint, Object object, Object queryParameters) throws IOException, RestException {
        return request(HttpMethod.PATCH, endpoint, queryParameters, object);
    }

    public ResponseBody request(HttpMethod httpMethod, String endpoint, Object queryParameters, Object body) throws IOException, RestException {
        return request(httpMethod, endpoint, queryParameters, body, ContentType.JSON);
    }

    public ResponseBody request(HttpMethod httpMethod, String endpoint, Object queryParameters, Object body, ContentType contentType) throws IOException, RestException {
        RequestBody requestBody = null;
        switch (contentType) {
            case JSON:
                if (body != null && body.getClass().equals(String.class)) { // PUT text
                    requestBody = RequestBody.create((String) body, textMediaType);
                } else {
                    requestBody = RequestBody.create(body == null ? "" : Utils.gson.toJson(body), jsonMediaType);
                }
                break;
            case FORM:
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                for (Field field : body.getClass().getFields()) {
                    Object value = null;
                    try {
                        value = field.get(body);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (value != null) {
                        formBodyBuilder = formBodyBuilder.add(field.getName(), value.toString());
                    }
                }
                requestBody = formBodyBuilder.build();
                break;
            case MULTIPART:
                MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
                List<Attachment> attachments = new ArrayList<Attachment>();
                Map<String, Object> fields = new HashMap<String, Object>();
                String attachmentName = "attachment";
                for (Field field : body.getClass().getFields()) {
                    Object value = null;
                    try {
                        value = field.get(body);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (value != null) {
                        if (field.getType() == Attachment.class) {
                            attachmentName = field.getName();
                            attachments.add((Attachment) value);
                        } else if (field.getType() == Attachment[].class) {
                            for (Attachment a : (Attachment[]) value) {
                                attachments.add(a);
                            }
                        } else {
                            fields.put(field.getName(), value);
                        }
                    }
                }
                if (fields.size() > 0) {
                    multipartBodyBuilder.addPart(RequestBody.create(Utils.gson.toJson(fields), jsonMediaType));
                }
                for (Attachment attachment : attachments) {
                    multipartBodyBuilder.addFormDataPart(attachmentName, attachment.filename, new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            if (attachment.contentType == null) {
                                return null;
                            }
                            return MediaType.parse(attachment.contentType);
                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            sink.write(attachment.content);
                        }
                    });
                }
                requestBody = multipartBodyBuilder.setType(MultipartBody.FORM).build();
                break;
            default:
                break;
        }
        return request(httpMethod, endpoint, queryParameters, requestBody);
    }

    // this method returns the raw response instead of just the body
    public Response requestRaw(HttpMethod httpMethod, String endpoint, Object queryParameters, RequestBody
        requestBody) throws IOException, RestException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(server).newBuilder(endpoint);

        if (queryParameters != null) {
            for (Field field : queryParameters.getClass().getFields()) {
                Object value = null;
                try {
                    value = field.get(queryParameters);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (value != null) {
                    if (value.getClass().isArray()) { // ?a=hello&a=world
                        for (int i = 0; i < Array.getLength(value); i++) {
                            urlBuilder = urlBuilder.addQueryParameter(field.getName(), Array.get(value, i).toString());
                        }
                    } else {
                        urlBuilder = urlBuilder.addQueryParameter(field.getName(), value.toString());
                    }
                }
            }
        }

        HttpUrl httpUrl = urlBuilder.build();

        Request.Builder builder = new Request.Builder().url(httpUrl);
        switch (httpMethod) {
            case GET:
                builder = builder.get();
                break;
            case POST:
                builder = builder.post(requestBody);
                break;
            case PUT:
                builder = builder.put(requestBody);
                break;
            case DELETE:
                builder = builder.delete();
                break;
            case PATCH:
                builder = builder.patch(requestBody);
                break;
            default:
                break;
        }

        String userAgentHeader = String.format("RC-JAVA-SDK Java %s %s", System.getProperty("java.version"), System.getProperty("os.name"));
        Request request = builder.addHeader("Authorization", authorizationHeader(endpoint))
            .addHeader("X-User-Agent", userAgentHeader)
            .build();

        Response response = httpClient.newCall(request).execute();
        int statusCode = response.code();
        if (statusCode < 200 || statusCode > 299) {
            throw new RestException(response, request);
        }
        for (HttpEventListener httpEventListener : httpEventListeners) {
            httpEventListener.afterHttpCall(response, request);
        }
        return response;
    }

    public static Logger logger = Logger.getLogger("com.ringcentral");
    public ResponseBody request(HttpMethod httpMethod, String endpoint, Object queryParameters, RequestBody
        requestBody) throws IOException, RestException {
        try {
            Response response = requestRaw(httpMethod, endpoint, queryParameters, requestBody);
            logger.fine(String.format("[HTTP %s %s] %s %s", httpMethod.toString(), response.code(), this.server, endpoint));
            return response.peekBody(Long.MAX_VALUE);
        }catch (RestException re) {
            Response response = re.response;
            logger.fine(String.format("[HTTP %s %s] %s %s", httpMethod.toString(), response.code(), this.server, endpoint));
            throw re;
        }
    }

    public String authorizeUri(AuthorizeRequest request) {
        if (request.response_type == null) {
            request.response_type = "code";
        }

        if (request.client_id == null) {
            request.client_id = clientId;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.server).newBuilder().addPathSegments("restapi/oauth/authorize");
        for (Field field : request.getClass().getFields()) {
            Object value = null;
            try {
                value = field.get(request);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value != null) {
                urlBuilder = urlBuilder.addQueryParameter(field.getName(), value.toString());
            }
        }
        return urlBuilder.build().toString();
    }

    public String authorizeUri(String redirectUri, String state) {
        AuthorizeRequest authorizeRequest = new AuthorizeRequest().redirect_uri(redirectUri).state(state);
        return authorizeUri(authorizeRequest);
    }

    public String authorizeUri(String redirectUri) {
        return authorizeUri(redirectUri, "");
    }

    // top level paths
    public com.ringcentral.paths.restapi.Index restapi(String apiVersion) {
        return new com.ringcentral.paths.restapi.Index(this, apiVersion);
    }

    public com.ringcentral.paths.restapi.Index restapi() {
        return new com.ringcentral.paths.restapi.Index(this, "v1.0");
    }

    public com.ringcentral.paths.scim.Index scim(String version) {
        return new com.ringcentral.paths.scim.Index(this, version);
    }

    public com.ringcentral.paths.scim.Index scim() {
        return new com.ringcentral.paths.scim.Index(this, "v2");
    }

    public com.ringcentral.paths.analytics.Index analytics() {
        return new com.ringcentral.paths.analytics.Index(this);
    }
}
