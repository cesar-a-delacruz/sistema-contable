import com.nutrehogar.sistemacontable.base.domain.repository.UserRepository;
import com.nutrehogar.sistemacontable.controller.crud.UserController;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.domain.type.PermissionType;
import com.nutrehogar.sistemacontable.report.ReportService;
import com.nutrehogar.sistemacontable.ui.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui.view.crud.DefaultUserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import javax.swing.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository mockRepo;
    @Mock
    private ReportService mockReport;
    @Mock
    private User mockCurrentUser;

    @Mock
    private DefaultUserView mockView;
    @Mock
    private JComboBox<PermissionType> mockCbxPermissions;
    @Mock
    private JTextField mockTxtUsername;
    @Mock
    private JTextField mockTxtPassword;
    @Mock
    private JCheckBox mockChkIsEnable;

    @Mock
    private JButton mockBtnAdd, mockBtnEdit, mockBtnDelete, mockBtnSave, mockBtnUpdate;
    @Mock
    private JTable mockTable;

    @Mock
    private CustomComboBoxModel<PermissionType> cbxModelPermissions;

    private UserController controller;
    private User testUser;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {

        // Vista: se devuelven los componentes necesarios para el CRUD
        when(mockView.getCbxPermissions()).thenReturn(mockCbxPermissions);
        when(mockView.getTxtUsername()).thenReturn(mockTxtUsername);
        when(mockView.getTxtPassword()).thenReturn(mockTxtPassword);
        when(mockView.getChkIsEnable()).thenReturn(mockChkIsEnable);

        // Botones y tabla usados por la clase padre CRUDController
        lenient().when(mockView.getBtnAdd()).thenReturn(mockBtnAdd);
        lenient().when(mockView.getBtnEdit()).thenReturn(mockBtnEdit);
        lenient().when(mockView.getBtnDelete()).thenReturn(mockBtnDelete);
        lenient().when(mockView.getBtnSave()).thenReturn(mockBtnSave);
        lenient().when(mockView.getBtnUpdate()).thenReturn(mockBtnUpdate);
        lenient().when(mockView.getTblData()).thenReturn(mockTable);

        // Se usa un Spy para poder interceptar métodos internos sin perder lógica real
        controller = Mockito.spy(new UserController(mockRepo, mockView, mockReport, mockCurrentUser));

        // Métodos automáticos del ciclo de vida del controlador se bloquean
        lenient().doNothing().when(controller).initialize();
        lenient().doNothing().when(controller).setupViewListeners();
        lenient().doNothing().when(controller).showMessage(anyString());

        // Inyección de un modelo de ComboBox mockeado mediante reflexión
        Field field = UserController.class.getField("cbxModelPermissions");
        field.setAccessible(true);
        field.set(controller, cbxModelPermissions);

        lenient().doReturn(cbxModelPermissions).when(controller).getCbxModelPermissions();

        // Restablecemos contadores para evitar verificaciones falsas después del
        // constructor
        reset(mockCbxPermissions, mockTxtUsername, mockTxtPassword, mockChkIsEnable);

        // Usuario base utilizado en varios tests
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEnable(true);
        testUser.setPermissions(PermissionType.ADMIN);
        testUser.setUser(mockCurrentUser);
    }

    // ============================================================
    // VALIDACIÓN DE CAMPOS (checkFields)
    // ============================================================

    @Test
    @DisplayName("checkFields → Datos válidos → true")
    void testCheckFieldsValid() {
        // El permiso debe ser válido para que la validación continúe
        lenient().doReturn(PermissionType.ADMIN).when(cbxModelPermissions).getSelectedItem();

        when(mockTxtUsername.getText()).thenReturn("validuser");
        when(mockTxtPassword.getText()).thenReturn("validpass123");

        assertTrue(controller.checkFields());
        verify(controller, never()).showMessage(anyString());
    }

    @Test
    @DisplayName("checkFields → Username vacío → false y mensaje")
    void testCheckFieldsEmptyUsername() {
        when(mockTxtUsername.getText()).thenReturn("");

        lenient().doReturn(PermissionType.ADMIN).when(cbxModelPermissions).getSelectedItem();

        assertFalse(controller.checkFields());
        verify(controller).showMessage("Ningun campo puede estar vació.");
    }

    @Test
    @DisplayName("checkFields → Username muy corto → false y mensaje")
    void testCheckFieldsUsernameTooShort() {
        lenient().doReturn(PermissionType.ADMIN).when(cbxModelPermissions).getSelectedItem();

        when(mockTxtUsername.getText()).thenReturn("a".repeat(User.MIN_LENGTH - 1));
        when(mockTxtPassword.getText()).thenReturn("validpass123");

        assertFalse(controller.checkFields());
        verify(controller).showMessage("El nombre de usuario no puede ser menor a: " + User.MIN_LENGTH);
    }

    @Test
    @DisplayName("checkFields → Password muy largo → false y mensaje")
    void testCheckFieldsPasswordTooLong() {
        lenient().doReturn(PermissionType.ADMIN).when(cbxModelPermissions).getSelectedItem();

        when(mockTxtUsername.getText()).thenReturn("validuser");
        when(mockTxtPassword.getText()).thenReturn("b".repeat(User.MAX_LENGTH + 1));

        assertFalse(controller.checkFields());
        verify(controller).showMessage("La contraseña no puede ser mayor a: " + User.MAX_LENGTH);
    }

    // ============================================================
    // PREPARAR PARA GUARDAR (prepareToSave)
    // ============================================================

    @Test
    @DisplayName("prepareToSave → Datos válidos → retorna User")
    void testPrepareToSaveValid() {
        final PermissionType EXPECTED_PERMISSION = PermissionType.ADMIN;

        doReturn(true).when(controller).checkFields();
        doReturn(EXPECTED_PERMISSION).when(cbxModelPermissions).getSelectedItem();

        when(mockTxtUsername.getText()).thenReturn("newuser");
        when(mockTxtPassword.getText()).thenReturn("newpass123");
        when(mockChkIsEnable.isSelected()).thenReturn(true);

        User u = controller.prepareToSave();

        assertNotNull(u);
        assertEquals(EXPECTED_PERMISSION, u.getPermissions());
    }

    @Test
    @DisplayName("prepareToSave → Datos inválidos → null")
    void testPrepareToSaveInvalid() {
        doReturn(false).when(controller).checkFields();

        assertNull(controller.prepareToSave());
    }

    // ============================================================
    // PREPARAR PARA ACTUALIZAR (prepareToUpdate)
    // ============================================================

    @Test
    @DisplayName("prepareToUpdate → Datos válidos → actualiza User")
    void testPrepareToUpdateValid() {
        final PermissionType EXPECTED_PERMISSION = PermissionType.CONTRIBUTE;

        controller.setSelected(testUser);
        doReturn(true).when(controller).checkFields();
        doReturn(EXPECTED_PERMISSION).when(cbxModelPermissions).getSelectedItem();

        when(mockTxtUsername.getText()).thenReturn("updatedUser");
        when(mockTxtPassword.getText()).thenReturn("newpassword");
        when(mockChkIsEnable.isSelected()).thenReturn(false);

        User u = controller.prepareToUpdate();

        assertNotNull(u);
        assertEquals(EXPECTED_PERMISSION, u.getPermissions());
    }

    // ============================================================
    // CARGAR FORMULARIO (prepareToEdit)
    // ============================================================

    @Test
    @DisplayName("prepareToEdit → Carga datos del usuario seleccionado")
    void testPrepareToEdit() {
        controller.setSelected(testUser);

        controller.prepareToEdit();

        verify(mockTxtUsername).setText(testUser.getUsername());
        verify(mockTxtPassword).setText(testUser.getPassword());
        verify(mockChkIsEnable).setSelected(testUser.isEnable());
        verify(mockCbxPermissions).setSelectedItem(testUser.getPermissions());
    }

    // ============================================================
    // PREPARAR PARA ELIMINAR (prepareToDelete)
    // ============================================================

    @Test
    @DisplayName("prepareToDelete → Retorna ID del usuario seleccionado")
    void testPrepareToDelete() {
        controller.setSelected(testUser);

        Integer id = controller.prepareToDelete();

        assertEquals(testUser.getId(), id);
    }
}
