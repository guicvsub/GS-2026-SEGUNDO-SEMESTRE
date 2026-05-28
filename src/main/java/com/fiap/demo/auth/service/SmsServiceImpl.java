package com.fiap.demo.auth.service;

import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void enviarSms(String cpf, String mensagem) {
        System.out.println("------------------------------------------");
        System.out.println("SIMULADOR SMS - CPF: " + cpf);
        System.out.println("MENSAGEM: " + mensagem);
        System.out.println("------------------------------------------");
    }
}
