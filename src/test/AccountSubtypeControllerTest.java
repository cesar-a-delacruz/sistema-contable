import com.nutrehogar.sistemacontable.base.domain.repository.AccountSubtypeRepository;
import com.nutrehogar.sistemacontable.controller.crud.AccountSubtypeController;
import com.nutrehogar.sistemacontable.domain.model.AccountSubtype;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.domain.type.AccountType;
import com.nutrehogar.sistemacontable.report.ReportService;
import com.nutrehogar.sistemacontable.ui.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui.view.crud.DefaultAccountSubtypeView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountSubtypeControllerTest {

    // --- Dependencias de la vista y servicios ---
    @Mock private AccountSubtypeRepository mockRepository;
    @Mock private DefaultAccountSubtypeView mockView;
    @Mock private ReportService mockReportService;
    @Mock private User mockUser;

    // --- Componentes de la vista ---
    @Mock private JComboBox<AccountType> mockCbxAccountType;
    @Mock private JTextField mockTxtAccountSubtypeId;
    @Mock private JTextField mockTxtAccountSubtypeName;
    @Mock private JTable mockTblData;
    @Mock private JButton mockBtnAdd;
    @Mock private JButton mockBtnEdit;
    @Mock private JButton mockBtnDelete;
    @Mock private JButton mockBtnSave;
    @Mock private JLabel mockLblAccountTypeId;
    @Mock private PlainDocument mockPlainDocument;

    // --- Controlador probado (Spy) ---
    private AccountSubtypeController controller;

    // --- Objeto seleccionado simulado ---
    private AccountSubtype testSubtype;

    // ID canónico usado en delete y cargado por getCanonicalId()
    private static final int TEST_CANONICAL_ID_TO_DELETE = 111;

    // ID simple almacenado internamente (usa prepareToEdit)
    private static final int TEST_SAVED_ID = 11;

    @BeforeEach
    void setUp() {

        // Vista enlazada a Mocks
        when(mockView.getCbxAccountType()).thenReturn(mockCbxAccountType);
        when(mockView.getTxtAccountSubtypeId()).thenReturn(mockTxtAccountSubtypeId);
        when(mockView.getTxtAccountSubtypeName()).thenReturn(mockTxtAccountSubtypeName);
        when(mockView.getLblAccountTypeId()).thenReturn(mockLblAccountTypeId);
        when(mockView.getBtnAdd()).thenReturn(mockBtnAdd);
        when(mockView.getBtnEdit()).thenReturn(mockBtnEdit);
        when(mockView.getBtnDelete()).thenReturn(mockBtnDelete);
        when(mockView.getBtnSave()).thenReturn(mockBtnSave);
        when(mockView.getBtnUpdate()).thenReturn(mockBtnSave);
        when(mockView.getTblData()).thenReturn(mockTblData);
        when(mockTxtAccountSubtypeId.getDocument()).thenReturn(mockPlainDocument);

        // Objeto seleccionado por el controlador
        testSubtype = Mockito.spy(new AccountSubtype(mockUser));
        testSubtype.setAccountType(AccountType.ASSETS);
        testSubtype.setId(TEST_SAVED_ID);
        testSubtype.setName("Activo Corriente");

        // Mock de getters de AccountSubtype
        when(testSubtype.getCanonicalId()).thenReturn(String.valueOf(TEST_CANONICAL_ID_TO_DELETE));
        when(testSubtype.getId()).thenReturn(TEST_SAVED_ID);
        when(testSubtype.getAccountType()).thenReturn(AccountType.ASSETS);

        // Spy del controlador real
        AccountSubtypeController realController =
                new AccountSubtypeController(mockRepository, mockView, mockReportService, mockUser);

        controller = Mockito.spy(realController);

        // Se neutralizan efectos secundarios para centrarse en la lógica
        doNothing().when(controller).showMessage(anyString());
        doNothing().when(controller).setTblModel(any());
        doNothing().when(controller).loadData();
        doNothing().when(controller).updateView();

        // El controlador siempre devuelve el objeto seleccionado
        doReturn(testSubtype).when(controller).getSelected();
    }

    // Devuelve el modelo interno del combo para pruebas
    private CustomComboBoxModel<AccountType> setupInternalComboBoxModel() {
        CustomComboBoxModel<AccountType> internal = controller.getCbxModelAccountType();
        internal.setSelectedItem(AccountType.ASSETS);
        return internal;
    }

    // =========================================================================
    //      PRUEBAS DE LA LÓGICA DE CREACIÓN (prepareToSave)
    // =========================================================================
    @Nested
    @DisplayName("Lógica de Creación (prepareToSave)")
    class PrepareToSaveTests {

        private static final String SUBTYPE_CODE_INPUT = "12";
        private static final int EXPECTED_COMPOSED_ID = 112; // 11 + 12

        @BeforeEach
        void resetCbxAccountType() {
            setupInternalComboBoxModel();
        }

        @Test
        @DisplayName("Retorna un AccountSubtype válido cuando los datos son correctos")
        void testPrepareToSave_Success() {

            when(mockTxtAccountSubtypeId.getText()).thenReturn(SUBTYPE_CODE_INPUT);
            when(mockTxtAccountSubtypeName.getText()).thenReturn("Pasivo Corriente");
            when(mockRepository.existsById(anyInt())).thenReturn(false);

            AccountSubtype result = controller.prepareToSave();

            assertNotNull(result);
            assertEquals(EXPECTED_COMPOSED_ID, result.getId());
            assertEquals("Pasivo Corriente", result.getName());
            assertEquals(AccountType.ASSETS, result.getAccountType());
            verify(controller, never()).showMessage(anyString());
        }

        @Test
        @DisplayName("Falla si ya existe un subtipo con el ID compuesto generado")
        void testPrepareToSave_Failure_IdAlreadyExists() {

            when(mockTxtAccountSubtypeId.getText()).thenReturn(String.valueOf(TEST_SAVED_ID));
            when(mockTxtAccountSubtypeName.getText()).thenReturn("Test Subtype");
            when(mockRepository.existsById(anyInt())).thenReturn(true);

            AccountSubtype result = controller.prepareToSave();

            assertNull(result);
            verify(controller).showMessage(contains("Ya existe un subtipo cuenta con el codigo:"));
        }
    }

    // =========================================================================
    //      PRUEBAS DE UPDATE, DELETE Y FLUJO DE VISTA
    // =========================================================================
    @Nested
    @DisplayName("Edición, Eliminación y Flujo de Vista")
    class PrepareToUpdateAndFlowTests {

        @BeforeEach
        void resetMocks() {
            Mockito.reset(testSubtype);
        }

        @Test
        @DisplayName("prepareToDelete: retorna el ID canónico de 3 dígitos")
        void testPrepareToDelete_ReturnsSelectedId() {

            Integer result = controller.prepareToDelete();

            assertEquals(TEST_CANONICAL_ID_TO_DELETE, result);
        }

        @Test
        @DisplayName("prepareToEdit: carga datos y bloquea campos clave")
        void testPrepareToEdit_LoadsDataAndDisablesFields() {

            controller.prepareToEdit();

            verify(mockTxtAccountSubtypeName).setText("Activo Corriente");
            verify(mockTxtAccountSubtypeId).setText(String.valueOf(TEST_SAVED_ID));
            verify(mockTxtAccountSubtypeId).setEnabled(false);

            verify(mockCbxAccountType, atLeastOnce()).setSelectedItem(AccountType.ASSETS);
            verify(mockCbxAccountType).setEnabled(false);
        }

        @Test
        @DisplayName("prepareToAdd: limpia campos y habilita entradas")
        void testPrepareToAdd_ClearsFieldsAndEnables() {

            controller.prepareToAdd();

            verify(mockTxtAccountSubtypeId, atLeastOnce()).setEnabled(true);
            verify(mockCbxAccountType, atLeastOnce()).setEnabled(true);

            verify(mockTxtAccountSubtypeName, atLeastOnce()).setText("");
            verify(mockTxtAccountSubtypeId, atLeastOnce()).setText("");

            verify(mockCbxAccountType, atLeastOnce()).setSelectedItem(AccountType.ASSETS);
        }
    }
}
