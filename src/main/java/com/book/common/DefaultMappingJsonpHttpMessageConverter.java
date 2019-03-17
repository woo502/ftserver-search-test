package com.book.common;

import java.io.IOException;
import java.io.PrintStream;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DefaultMappingJsonpHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public DefaultMappingJsonpHttpMessageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        setObjectMapper(objectMapper);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException,
                                                                           HttpMessageNotWritableException {
        String jsonpCallback = null;

        RequestAttributes reqAttrs = RequestContextHolder.currentRequestAttributes();
        if (reqAttrs instanceof ServletRequestAttributes) {
            jsonpCallback = ((ServletRequestAttributes) reqAttrs).getRequest().getParameter("callback");
        }

        if (jsonpCallback != null) {
            new PrintStream(outputMessage.getBody()).print(jsonpCallback + "(");
        }

        super.writeInternal(o, outputMessage);

        if (jsonpCallback != null) {
            new PrintStream(outputMessage.getBody()).println(");");
        }
    }
}
