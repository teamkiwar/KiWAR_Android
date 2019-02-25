package com.yg.engine.util.android;

import com.yg.engine.util.android.assets.Handler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class AndroidURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("assets".equals(protocol)) {
            return new Handler();
        } else if ("content".equals(protocol)){
            return new com.yg.engine.util.android.content.Handler();
        }
        return null;
    }
}
