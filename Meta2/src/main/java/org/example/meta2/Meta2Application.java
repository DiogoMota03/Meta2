package org.example.meta2;

import googol.gateway.Gateway;
import googol.gateway.IGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

@SpringBootApplication
public class Meta2Application {

    public static void main(String[] args) {
        SpringApplication.run(Meta2Application.class, args);
    }

}
