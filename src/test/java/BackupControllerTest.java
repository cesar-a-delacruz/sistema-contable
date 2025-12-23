import com.nutrehogar.sistemacontable.base.domain.repository.BackupRepository;
import com.nutrehogar.sistemacontable.ui_2.view.service.BackupView;
import com.nutrehogar.sistemacontable.controller.service.BackupController;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BackupControllerTest {

    // Dependencias simuladas del controlador
    @Mock
    private BackupRepository mockRepository;
    @Mock
    private BackupView mockView;
    @Mock
    private Session mockSession;
    @Mock
    private JFrame mockFrame;
    @Mock
    private JTable mockTblData;
    @Mock
    private JButton mockBtnAdd;
    @Mock
    private JButton mockBtnEdit;
    @Mock
    private JButton mockBtnRestore;

    private BackupController controller;

    // Datos de prueba para simular archivos de respaldo
    private List<Path> mockBackupFiles;

    @BeforeEach
    void setUp() {
        // Configuración básica de la vista simulada
        when(mockView.getTblData()).thenReturn(mockTblData);
        when(mockView.getBtnAdd()).thenReturn(mockBtnAdd);
        when(mockView.getBtnEdit()).thenReturn(mockBtnEdit);
        when(mockView.getBtnRestore()).thenReturn(mockBtnRestore);

        // Archivos ficticios de respaldo
        mockBackupFiles = new ArrayList<>();
        mockBackupFiles.add(Paths.get("backup1.sqlite"));
        mockBackupFiles.add(Paths.get("backup2.sqlite"));
        mockBackupFiles.add(Paths.get("backup3.sqlite"));
    }

    // =========================================================================
    // PRUEBAS DE LÓGICA PRINCIPAL
    // =========================================================================

    @Test
    @DisplayName("Debe generar correctamente la ruta de un archivo de backup")
    void testCreateFilePathForBackup_CorrectFormat() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        doNothing().when(controller).loadData();
        doNothing().when(controller).setupViewListeners();

        String result = controller.createFilePathForBackup("test_backup");

        assertNotNull(result);
        assertTrue(result.endsWith("test_backup.sqlite"));
        assertTrue(result.contains(File.separator));
    }

    @Test
    @DisplayName("Debe generar un nombre válido basado en la fecha actual")
    void testCreateNameByDate_CorrectFormat() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        doNothing().when(controller).loadData();
        doNothing().when(controller).setupViewListeners();

        String result = controller.createNameByDate();

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}"));

        LocalDateTime parsed = LocalDateTime.parse(result, BackupController.FILE_DATE_FORMATTER);
        LocalDateTime now = LocalDateTime.now();

        assertTrue(parsed.isBefore(now.plusMinutes(1)) && parsed.isAfter(now.minusMinutes(1)));
    }

    @Test
    @DisplayName("Debe devolver 'Desconocido' si la ruta no existe")
    void testGetModificationDate_InvalidPath() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        doNothing().when(controller).loadData();
        doNothing().when(controller).setupViewListeners();

        String result = controller.getModificationDate(Paths.get("invalid/path/file.sqlite"));

        assertEquals("Desconocido", result);
    }

    // =========================================================================
    // PRUEBAS DE LA TABLA (MODEL)
    // =========================================================================

    @Test
    @DisplayName("El modelo debe tener exactamente 2 columnas")
    void testBackupTableModel_GetColumnCount() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals(2, tableModel.getColumnCount());
    }

    @Test
    @DisplayName("Los nombres de las columnas deben coincidir")
    void testBackupTableModel_GetColumnName() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals("Nombre", tableModel.getColumnName(0));
        assertEquals("Última Modificación", tableModel.getColumnName(1));
    }

    @Test
    @DisplayName("El número de filas debe coincidir con la cantidad de datos")
    void testBackupTableModel_GetRowCount() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(mockBackupFiles);

        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals(3, tableModel.getRowCount());
    }

    @Test
    @DisplayName("Debe retornar correctamente cada valor de la tabla")
    void testBackupTableModel_GetValueAt() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        Path testPath = Paths.get("mybackup.sqlite");
        controller.setData(List.of(testPath));
        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals("mybackup.sqlite", tableModel.getValueAt(0, 0));
        assertNotNull(tableModel.getValueAt(0, 1));
    }

    @Test
    @DisplayName("El tipo de columna debe ser String")
    void testBackupTableModel_GetColumnClass() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals(String.class, tableModel.getColumnClass(0));
        assertEquals(String.class, tableModel.getColumnClass(1));
    }

    @Test
    @DisplayName("Columnas inválidas deben devolver cadena vacía")
    void testBackupTableModel_GetValueAt_InvalidColumn() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(List.of(Paths.get("backup.sqlite")));

        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals("", tableModel.getValueAt(0, 99));
    }

    // =========================================================================
    // PRUEBAS DE COMPONENTES Y LISTENERS
    // =========================================================================

    @Test
    @DisplayName("El controlador debe registrar sus listeners al iniciar")
    void testInitialize_CallsSetupViewListeners() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        verify(mockBtnAdd, atLeastOnce()).addActionListener(any());
        verify(mockBtnRestore, atLeastOnce()).addActionListener(any());
        verify(mockTblData, atLeastOnce()).addMouseListener(any(MouseListener.class));
    }

    @Test
    @DisplayName("Debe activar el botón de restaurar al seleccionar una fila")
    void testSetElementSelected_EnablesRestoreButton() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(mockBackupFiles);

        MouseEvent mockEvent = mock(MouseEvent.class);
        when(mockEvent.getPoint()).thenReturn(new java.awt.Point(10, 10));
        when(mockTblData.rowAtPoint(any())).thenReturn(0);
        when(mockTblData.getSelectedRow()).thenReturn(0);

        controller.setElementSelected(mockEvent);

        verify(mockBtnRestore).setEnabled(true);
        assertEquals(mockBackupFiles.get(0), controller.getSelected());
    }

    @Test
    @DisplayName("Debe desactivar botón si no hay selección válida")
    void testSetElementSelected_DisablesRestoreButtonWhenNoSelection() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(mockBackupFiles);

        MouseEvent mockEvent = mock(MouseEvent.class);
        when(mockTblData.rowAtPoint(any())).thenReturn(0);
        when(mockTblData.getSelectedRow()).thenReturn(-1);

        controller.setElementSelected(mockEvent);

        verify(mockBtnRestore).setEnabled(false);
    }

    @Test
    @DisplayName("Un clic fuera de la tabla no debe generar acciones")
    void testSetElementSelected_ClickOutsideRows() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        MouseEvent mockEvent = mock(MouseEvent.class);
        when(mockTblData.rowAtPoint(any())).thenReturn(-1);

        controller.setElementSelected(mockEvent);

        verify(mockBtnRestore, never()).setEnabled(anyBoolean());
    }

    @Test
    @DisplayName("Índice fuera de rango debe desactivar el botón restaurar")
    void testSetElementSelected_SelectedRowTooLarge() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(mockBackupFiles);

        MouseEvent mockEvent = mock(MouseEvent.class);
        when(mockTblData.rowAtPoint(any())).thenReturn(0);
        when(mockTblData.getSelectedRow()).thenReturn(999);

        controller.setElementSelected(mockEvent);

        verify(mockBtnRestore).setEnabled(false);
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    @Test
    void testGetTblData() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        assertEquals(mockTblData, controller.getTblData());
    }

    @Test
    void testGetBtnAdd() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        assertEquals(mockBtnAdd, controller.getBtnAdd());
    }

    @Test
    void testGetBtnRestore() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        assertEquals(mockBtnRestore, controller.getBtnRestore());
    }

    @Test
    void testGetView() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        assertEquals(mockView, controller.getView());
    }

    // =========================================================================
    // CASOS ESPECIALES
    // =========================================================================

    @Test
    @DisplayName("Si no hay datos, el modelo debe reportar 0 filas")
    void testBackupTableModel_EmptyData() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));
        controller.setData(new ArrayList<>());

        BackupController.BackupTableModel tableModel = controller.new BackupTableModel();

        assertEquals(0, tableModel.getRowCount());
    }

    @Test
    @DisplayName("Debe generar rutas válidas aún con caracteres especiales")
    void testCreateFilePathForBackup_SpecialCharacters() {
        controller = spy(new BackupController(mockRepository, mockView, mockSession, mockFrame));

        String result = controller.createFilePathForBackup("backup_2024-12-08_special");

        assertNotNull(result);
        assertTrue(result.contains("backup_2024-12-08_special.sqlite"));
    }
}
