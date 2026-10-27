package io.github.qppocr.server;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

@SolonMain
public class QppOcrSolonServer {

    public static void main(String[] args) {
        Solon.start(QppOcrSolonServer.class, args);
    }
}
