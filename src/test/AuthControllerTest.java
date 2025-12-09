import com.nutrehogar.sistemacontable.application.config.Context;
import com.nutrehogar.sistemacontable.application.config.PasswordHasher;
import com.nutrehogar.sistemacontable.base.domain.repository.UserRepository;
import com.nutrehogar.sistemacontable.controller.service.AuthControllerTestable;
import com.nutrehogar.sistemacontable.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserRepository mockUserRepository;
    @Mock private Context mockContext;

    @Captor private ArgumentCaptor<User> userCaptor;

    private AuthControllerTestable controllerTestable;
    private User testUserPlain;
    private User testUserHashed;

    private final String TEST_PASSWORD = "testPassword123";

    @BeforeEach
    void setUp() {
        controllerTestable = new AuthControllerTestable(mockUserRepository, mockContext);

        // Usuario con contraseña en texto plano (caso antiguo)
        testUserPlain = new User();
        testUserPlain.setUsername("plainUser");
        testUserPlain.setPassword(TEST_PASSWORD);

        // Usuario con contraseña hasheada (caso moderno)
        testUserHashed = new User();
        testUserHashed.setUsername("hashedUser");
        testUserHashed.setPassword(PasswordHasher.hashPassword(TEST_PASSWORD));
    }

    // =========================================================================
    // PRUEBAS DE LOGIN EXITOSO
    // =========================================================================

    @Test
    @DisplayName("Login OK → Contraseña hasheada correcta")
    void testAttemptLogin_Success_HashedPassword() {
        controllerTestable.attemptLogin(testUserHashed, TEST_PASSWORD);

        assertEquals(testUserHashed, controllerTestable.authenticatedUser);
        verify(mockContext).registerBean(eq(User.class), userCaptor.capture());
        assertEquals(testUserHashed, userCaptor.getValue());
    }

    @Test
    @DisplayName("Login OK → Contraseña en texto plano correcta")
    void testAttemptLogin_Success_PlainPassword() {
        controllerTestable.attemptLogin(testUserPlain, TEST_PASSWORD);

        assertEquals(testUserPlain, controllerTestable.authenticatedUser);
        verify(mockContext).registerBean(eq(User.class), userCaptor.capture());
        assertEquals(testUserPlain, userCaptor.getValue());
    }

    // =========================================================================
    // PRUEBAS DE ERROR EN LOGIN
    // =========================================================================

    @Test
    @DisplayName("Login Fallido → Contraseña incorrecta (Hashed)")
    void testAttemptLogin_Failure_WrongPasswordHashed() {
        controllerTestable.attemptLogin(testUserHashed, "wrong-password");

        assertNull(controllerTestable.authenticatedUser);
        assertEquals("Contraseña Incorrecta.", controllerTestable.lastMessage);
        verify(mockContext, never()).registerBean(any(), any());
    }

    @Test
    @DisplayName("Login Fallido → Contraseña incorrecta (Plain)")
    void testAttemptLogin_Failure_WrongPasswordPlain() {
        controllerTestable.attemptLogin(testUserPlain, "wrong-password");

        assertNull(controllerTestable.authenticatedUser);
        assertEquals("Contraseña Incorrecta.", controllerTestable.lastMessage);
    }

    @Test
    @DisplayName("Login Fallido → Usuario seleccionado es NULL")
    void testAttemptLogin_Failure_UserIsNull() {
        controllerTestable.attemptLogin(null, TEST_PASSWORD);

        assertNull(controllerTestable.authenticatedUser);
        assertEquals("Usuario no seleccionado", controllerTestable.lastMessage);
        verify(mockContext, never()).registerBean(any(), any());
    }
}
