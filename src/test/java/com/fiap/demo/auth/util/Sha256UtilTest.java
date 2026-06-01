package com.fiap.demo.auth.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Sha256UtilTest {

    @Test
    void testHash() {
        String senha = "Farm@2026";
        String hash = Sha256Util.hash(senha);
        
        // Verifica se o hash não é nulo e não está vazio
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        
        // Verifica se o hash tem 64 caracteres (SHA-256 em hexadecimal)
        assertEquals(64, hash.length());
        
        // Verifica se o hash é consistente (mesma senha = mesmo hash)
        String hash2 = Sha256Util.hash(senha);
        assertEquals(hash, hash2);
        
        // Verifica se senhas diferentes produzem hashes diferentes
        String hash3 = Sha256Util.hash("OutraSenha123");
        assertNotEquals(hash, hash3);
    }

    @Test
    void testMatches() {
        String senha = "Farm@2026";
        String hash = Sha256Util.hash(senha);
        
        // Verifica se a senha correta corresponde ao hash
        assertTrue(Sha256Util.matches(senha, hash));
        
        // Verifica se a senha incorreta não corresponde ao hash
        assertFalse(Sha256Util.matches("SenhaErrada", hash));
        
        // Verifica se senha vazia não corresponde
        assertFalse(Sha256Util.matches("", hash));
    }
}
