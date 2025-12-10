package com.nutrehogar.sistemacontable.controller.service;

import com.nutrehogar.sistemacontable.application.config.Context;
import com.nutrehogar.sistemacontable.application.config.PasswordHasher;
import com.nutrehogar.sistemacontable.domain.model.User;

/**
 * Versión simplificada del AuthController diseñada específicamente para pruebas
 * unitarias.
 * No utiliza componentes Swing ni ventanas, y expone directamente los valores
 * relevantes
 * para poder validar la lógica sin depender de la interfaz gráfica.
 */
public class AuthControllerTestable {
    private final Context context;

    public User authenticatedUser;
    public String lastMessage;

    public AuthControllerTestable(Context context) {
        this.context = context;
    }

    /**
     * Método equivalente al proceso de login, pero sin interacción con UI.
     * Permite evaluar únicamente la lógica de validación de contraseñas.
     */
    public void attemptLogin(User user, String passwordEntered) {

        if (user == null) {
            lastMessage = "Usuario no seleccionado";
            return;
        }

        String passwordStored = user.getPassword();
        boolean passwordCorrect;

        // Detección automática del tipo de contraseña: texto plano vs. hash bcrypt
        if (passwordStored.startsWith("$2a$")) {
            passwordCorrect = PasswordHasher.verifyPassword(passwordEntered, passwordStored);
        } else {
            passwordCorrect = passwordEntered.equals(passwordStored);
        }

        if (passwordCorrect) {
            authenticatedUser = user;
            context.registerBean(User.class, user); // Efecto equivalente al AuthController real
        } else {
            authenticatedUser = null;
            lastMessage = "Contraseña Incorrecta.";
        }
    }
}
