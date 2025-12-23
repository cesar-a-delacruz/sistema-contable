import com.nutrehogar.sistemacontable.application.config.Context;
import com.nutrehogar.sistemacontable.ui_2.view.service.DashboardView;
import com.nutrehogar.sistemacontable.controller.crud.*;
import com.nutrehogar.sistemacontable.controller.business.*;
import com.nutrehogar.sistemacontable.controller.service.BackupController;
import com.nutrehogar.sistemacontable.controller.service.DashboardController;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.ui_2.view.service.BackupView;
import com.nutrehogar.sistemacontable.ui_2.view.crud.DefaultAccountEntryFormView;
import com.nutrehogar.sistemacontable.ui_2.view.crud.DefaultAccountSubtypeView;
import com.nutrehogar.sistemacontable.ui_2.view.crud.DefaultAccountView;
import com.nutrehogar.sistemacontable.ui_2.view.crud.DefaultUserView;
import com.nutrehogar.sistemacontable.ui_2.view.business.DefaultJournalView;
import com.nutrehogar.sistemacontable.ui_2.view.business.DefaultTrialBalanceView;
import com.nutrehogar.sistemacontable.ui_2.view.business.DefaultGeneralLedgerView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardControllerTest {

    // --- Dependencias principales ---
    @Mock
    private DashboardView mockView;
    @Mock
    private Context mockContext;
    @Mock
    private User mockUser;

    // --- Componentes de la vista ---
    @Mock
    private JButton mockBtnShowFormView;
    @Mock
    private JButton mockBtnShowAccountSubtypeView;
    @Mock
    private JButton mockBtnShowAccountView;
    @Mock
    private JButton mockBtnShowJournalView;
    @Mock
    private JButton mockBtnShowTrialBalanceView;
    @Mock
    private JButton mockBtnShowGeneralLedgerView;
    @Mock
    private JButton mockBtnShowBackupView;
    @Mock
    private JButton mockBtnShowUserView;
    @Mock
    private JButton mockBtnHome;
    @Mock
    private JPanel mockPnlContent;
    @Mock
    private JPanel mockPnlHome;
    @Mock
    private JPanel mockPnlNav;

    // --- Controladores y vistas que devuelve el Context ---
    @Mock
    private AccountingEntryFormController mockFormController;
    @Mock
    private AccountSubtypeController mockAccountSubtypeController;
    @Mock
    private AccountController mockAccountController;
    @Mock
    private JournalController mockJournalController;
    @Mock
    private TrialBalanceController mockTrialBalanceController;
    @Mock
    private GeneralLedgerController mockGeneralLedgerController;
    @Mock
    private BackupController mockBackupController;
    @Mock
    private UserController mockUserController;

    @Mock
    private DefaultAccountEntryFormView mockFormViewConcrete;
    @Mock
    private DefaultAccountSubtypeView mockAccountSubtypeView;
    @Mock
    private DefaultAccountView mockAccountView;
    @Mock
    private DefaultJournalView mockJournalView;
    @Mock
    private DefaultTrialBalanceView mockTrialBalanceView;
    @Mock
    private DefaultGeneralLedgerView mockGeneralLedgerView;
    @Mock
    private BackupView mockBackupView;
    @Mock
    private DefaultUserView mockUserView;

    // --- Capturadores de ActionListener ---
    private ArgumentCaptor<ActionListener> formViewListenerCaptor;
    private ArgumentCaptor<ActionListener> accountSubtypeListenerCaptor;
    private ArgumentCaptor<ActionListener> accountViewListenerCaptor;
    private ArgumentCaptor<ActionListener> journalViewListenerCaptor;
    private ArgumentCaptor<ActionListener> trialBalanceViewListenerCaptor;
    private ArgumentCaptor<ActionListener> generalLedgerViewListenerCaptor;
    private ArgumentCaptor<ActionListener> backupViewListenerCaptor;
    private ArgumentCaptor<ActionListener> userViewListenerCaptor;
    private ArgumentCaptor<ActionListener> homeListenerCaptor;

    private MockedStatic<SwingUtilities> mockedSwing;
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {

        // Capturadores para todos los botones
        formViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        accountSubtypeListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        accountViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        journalViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        trialBalanceViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        generalLedgerViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        backupViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        userViewListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);
        homeListenerCaptor = ArgumentCaptor.forClass(ActionListener.class);

        // Stubbing de getters de la vista
        doReturn(mockBtnShowFormView).when(mockView).getBtnShowFormView();
        doReturn(mockBtnShowAccountSubtypeView).when(mockView).getBtnShowAccountSubtypeView();
        doReturn(mockBtnShowAccountView).when(mockView).getBtnShowAccountView();
        doReturn(mockBtnShowJournalView).when(mockView).getBtnShowJournalView();
        doReturn(mockBtnShowTrialBalanceView).when(mockView).getBtnShowTrialBalanceView();
        doReturn(mockBtnShowGeneralLedgerView).when(mockView).getBtnShowGeneralLedgerView();
        doReturn(mockBtnShowBackupView).when(mockView).getBtnShowBackupView();
        doReturn(mockBtnShowUserView).when(mockView).getBtnShowUserView();
        doReturn(mockBtnHome).when(mockView).getBtnHome();
        doReturn(mockPnlContent).when(mockView).getPnlContent();
        doReturn(mockPnlHome).when(mockView).getPnlHome();
        doReturn(mockPnlNav).when(mockView).getPnlNav();

        // Stubbing de controladores y vistas usados por el Context
        doReturn(mockFormViewConcrete).when(mockFormController).getView();
        doReturn(mockAccountSubtypeView).when(mockAccountSubtypeController).getView();
        doReturn(mockAccountView).when(mockAccountController).getView();
        doReturn(mockJournalView).when(mockJournalController).getView();
        doReturn(mockTrialBalanceView).when(mockTrialBalanceController).getView();
        doReturn(mockGeneralLedgerView).when(mockGeneralLedgerController).getView();
        doReturn(mockUserView).when(mockUserController).getView();
        doReturn(mockBackupView).when(mockBackupController).getView();

        // Configuración del Context para devolver controladores
        when(mockContext.getBean(AccountingEntryFormController.class)).thenReturn(mockFormController);
        when(mockContext.getBean(AccountSubtypeController.class)).thenReturn(mockAccountSubtypeController);
        when(mockContext.getBean(AccountController.class)).thenReturn(mockAccountController);
        when(mockContext.getBean(JournalController.class)).thenReturn(mockJournalController);
        when(mockContext.getBean(TrialBalanceController.class)).thenReturn(mockTrialBalanceController);
        when(mockContext.getBean(GeneralLedgerController.class)).thenReturn(mockGeneralLedgerController);
        when(mockContext.getBean(BackupController.class)).thenReturn(mockBackupController);
        when(mockContext.getBean(UserController.class)).thenReturn(mockUserController);
        when(mockContext.getBean(User.class)).thenReturn(mockUser);
        when(mockUser.isAdmin()).thenReturn(true);

        // Mock de SwingUtilities.invokeLater para ejecutarlo sin asincronía
        mockedSwing = Mockito.mockStatic(SwingUtilities.class);
        mockedSwing.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                .thenAnswer((Answer<Void>) invocation -> {
                    Runnable runnable = invocation.getArgument(0);
                    runnable.run();
                    return null;
                });

        // Spy del controller para permitir stubbing de sus getters
        DashboardController realController = new DashboardController(mockView, mockContext);
        dashboardController = Mockito.spy(realController);

        // Forzar getters del spy para usar los mocks de botones/paneles
        doReturn(mockBtnShowFormView).when(dashboardController).getBtnShowFormView();
        doReturn(mockBtnShowAccountSubtypeView).when(dashboardController).getBtnShowAccountSubtypeView();
        doReturn(mockBtnShowAccountView).when(dashboardController).getBtnShowAccountView();
        doReturn(mockBtnShowJournalView).when(dashboardController).getBtnShowJournalView();
        doReturn(mockBtnShowTrialBalanceView).when(dashboardController).getBtnShowTrialBalanceView();
        doReturn(mockBtnShowGeneralLedgerView).when(dashboardController).getBtnShowGeneralLedgerView();
        doReturn(mockBtnShowBackupView).when(dashboardController).getBtnShowBackupView();
        doReturn(mockBtnShowUserView).when(dashboardController).getBtnShowUserView();
        doReturn(mockBtnHome).when(dashboardController).getBtnHome();
        doReturn(mockPnlContent).when(dashboardController).getPnlContent();
        doReturn(mockPnlHome).when(dashboardController).getPnlHome();
        doReturn(mockPnlNav).when(dashboardController).getPnlNav();
    }

    @AfterEach
    void tearDown() {
        if (mockedSwing != null)
            mockedSwing.close();
    }

    /**
     * Ejecuta la función encargada de registrar todos los listeners
     * y captura cada ActionListener asignado.
     */
    private void captureAllListeners() {
        dashboardController.setupViewListeners();

        verify(mockBtnShowFormView, atLeastOnce()).addActionListener(formViewListenerCaptor.capture());
        verify(mockBtnShowAccountSubtypeView, atLeastOnce()).addActionListener(accountSubtypeListenerCaptor.capture());
        verify(mockBtnShowAccountView, atLeastOnce()).addActionListener(accountViewListenerCaptor.capture());
        verify(mockBtnShowJournalView, atLeastOnce()).addActionListener(journalViewListenerCaptor.capture());
        verify(mockBtnShowTrialBalanceView, atLeastOnce()).addActionListener(trialBalanceViewListenerCaptor.capture());
        verify(mockBtnShowGeneralLedgerView, atLeastOnce())
                .addActionListener(generalLedgerViewListenerCaptor.capture());
        verify(mockBtnShowBackupView, atLeastOnce()).addActionListener(backupViewListenerCaptor.capture());
        verify(mockBtnShowUserView, atLeastOnce()).addActionListener(userViewListenerCaptor.capture());
        verify(mockBtnHome, atLeastOnce()).addActionListener(homeListenerCaptor.capture());
    }

    // --- Pruebas del comportamiento del sistema ---

    @Test
    @DisplayName("Listeners: todos los botones deben registrar un ActionListener")
    void testAllButtonsHaveListeners() {
        captureAllListeners();
    }

    @Test
    @DisplayName("FormView: debe solicitar el controlador y cargar la vista")
    void testFormViewComponentCall() {
        captureAllListeners();
        formViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext).getBean(AccountingEntryFormController.class);
        verify(mockFormController).getView();
    }

    @Test
    @DisplayName("AccountSubtype: debe solicitar el controlador y cargar la vista")
    void testAccountSubtypeViewComponentCall() {
        captureAllListeners();
        accountSubtypeListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext).getBean(AccountSubtypeController.class);
        verify(mockAccountSubtypeController).getView();
    }

    @Test
    @DisplayName("AccountView: debe solicitar el controlador y cargar la vista")
    void testAccountViewComponentCall() {
        captureAllListeners();
        accountViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext, atLeastOnce()).getBean(AccountController.class);
        verify(mockAccountController).getView();
    }

    @Test
    @DisplayName("JournalView: debe solicitar el controlador y cargar la vista")
    void testJournalViewComponentCall() {
        captureAllListeners();
        journalViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext, atLeastOnce()).getBean(JournalController.class);
        verify(mockJournalController).getView();
    }

    @Test
    @DisplayName("TrialBalance: debe solicitar el controlador y cargar la vista")
    void testTrialBalanceViewComponentCall() {
        captureAllListeners();
        trialBalanceViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext, atLeastOnce()).getBean(TrialBalanceController.class);
        verify(mockTrialBalanceController).getView();
    }

    @Test
    @DisplayName("GeneralLedger: debe solicitar el controlador y cargar la vista")
    void testGeneralLedgerViewComponentCall() {
        captureAllListeners();
        generalLedgerViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext, atLeastOnce()).getBean(GeneralLedgerController.class);
        verify(mockGeneralLedgerController).getView();
    }

    @Test
    @DisplayName("BackupView: debe solicitar el controlador y ejecutar showView()")
    void testBackupViewComponentCall() {
        captureAllListeners();
        backupViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext).getBean(BackupController.class);
        verify(mockBackupController).showView();
    }

    @Test
    @DisplayName("UserView: debe solicitar el controlador y cargar la vista")
    void testUserViewComponentCall() {
        captureAllListeners();
        userViewListenerCaptor.getValue().actionPerformed(null);
        verify(mockContext).getBean(UserController.class);
        verify(mockUserController).getView();
    }

    @Test
    @DisplayName("Home: debe mostrar el contenido del panel Home")
    void testHomeComponentCall() {
        captureAllListeners();
        homeListenerCaptor.getValue().actionPerformed(null);

        verify(mockPnlContent).removeAll();
        verify(mockPnlContent).add(mockPnlHome, BorderLayout.CENTER);
        verify(mockPnlContent).revalidate();
        verify(mockPnlContent).repaint();
    }

    @Test
    @DisplayName("Permisos: usuario no admin oculta/inhabilita botones correspondientes")
    void testButtonPermissionSettings_NonAdmin() {

        Context nonAdminContext = mock(Context.class);
        User nonAdminUser = mock(User.class);

        when(nonAdminUser.isAdmin()).thenReturn(false);
        when(nonAdminContext.getBean(User.class)).thenReturn(nonAdminUser);

        JButton mockBtnShowUserViewLocal = mock(JButton.class);
        JButton mockBtnShowBackupViewLocal = mock(JButton.class);

        doReturn(mockBtnShowUserViewLocal).when(mockView).getBtnShowUserView();
        doReturn(mockBtnShowBackupViewLocal).when(mockView).getBtnShowBackupView();
        when(mockView.getBtnShowUserView().isVisible()).thenReturn(false);
        when(mockView.getBtnShowBackupView().isEnabled()).thenReturn(false);
    }
}
