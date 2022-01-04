package com.mromanak.dockercomposeintegrationtstst.utils.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class RestApiTest {

    protected RestTemplate restTemplate = new RestTemplate();

    private <ReqType, ResType> ResponseEntity<ResType> makeRequest(
            RequestEntity<ReqType> requestEntity,
            ParameterizedTypeReference<ResType> type
    ) {
        return restTemplate.exchange(requestEntity, type);
    }

    protected <ResType> ResponseEntity<ResType> makeGetRequest(URI uri, ParameterizedTypeReference<ResType> type) {
        RequestEntity<Void> requestEntity = RequestEntity.get(uri).build();
        return makeRequest(requestEntity, type);
    }

    protected <ReqType, ResType> ResponseEntity<ResType> makePostRequest(
            URI uri, ReqType body,
            ParameterizedTypeReference<ResType> type
    ) {
        RequestEntity<ReqType> requestEntity = RequestEntity.post(uri).body(body);
        return makeRequest(requestEntity, type);
    }

    protected <ResType> ResponseEntity<ResType> makeDeleteRequest(URI uri, ParameterizedTypeReference<ResType> type) {
        RequestEntity<Void> requestEntity = RequestEntity.delete(uri).build();
        return makeRequest(requestEntity, type);
    }
}
