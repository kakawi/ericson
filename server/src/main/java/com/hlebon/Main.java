package com.hlebon;

import com.hlebon.handler.MessageHandler;
import com.hlebon.service.MessageSender;
import com.hlebon.service.Server;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.hlebon");
        context.refresh();

        final Server server = context.getBean(Server.class);
        new Thread(server).start();

        final MessageSender sender = context.getBean(MessageSender.class);
        new Thread(sender).start();

        final MessageHandler handler = context.getBean(MessageHandler.class);
        new Thread(handler).start();
    }
}
