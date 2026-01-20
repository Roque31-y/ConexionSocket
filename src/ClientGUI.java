import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cliente con interfaz grafica para gestion remota de archivos via sockets
 */
public class ClientGUI extends JFrame {
    private JTextField txtIP;
    private JTextField txtPort;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JLabel lblStatus;
    private JPanel statusPanel;
    
    // Panel de archivos
    private DefaultTableModel tableModel;
    private JTable fileTable;
    private JTextArea txtFileContent;
    private JTextField txtFileName;
    
    // Botones de operaciones
    private JButton btnRefresh;
    private JButton btnOpen;
    private JButton btnCreate;
    private JButton btnSave;
    private JButton btnDelete;
    
    // Conexion
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected = false;
    
    // Log
    private JTextArea logArea;
    
    public ClientGUI() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Cliente de Archivos - Socket");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(45, 45, 48));
        
        // Panel superior - Conexion
        JPanel connectionPanel = createConnectionPanel();
        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        
        // Panel central - Split entre archivos y contenido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(new Color(45, 45, 48));
        
        // Panel izquierdo - Lista de archivos
        JPanel fileListPanel = createFileListPanel();
        splitPane.setLeftComponent(fileListPanel);
        
        // Panel derecho - Contenido y log
        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Panel de estado
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus = new JLabel("[ DESCONECTADO ]");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        statusPanel.add(lblStatus);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Deshabilitar botones hasta conectar
        setOperationButtonsEnabled(false);
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(60, 60, 65));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Conexion al Servidor ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.WHITE
        ));
        
        // IP
        JLabel lblIP = new JLabel("IP del Servidor:");
        lblIP.setForeground(Color.WHITE);
        lblIP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblIP);
        
        txtIP = new JTextField("127.0.0.1", 12);
        txtIP.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtIP.setToolTipText("Ingrese la IP del servidor (127.0.0.1 para local)");
        panel.add(txtIP);
        
        // Separador
        panel.add(Box.createHorizontalStrut(10));
        
        // Puerto
        JLabel lblPort = new JLabel("Puerto:");
        lblPort.setForeground(Color.WHITE);
        lblPort.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblPort);
        
        txtPort = new JTextField("5000", 6);
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtPort.setToolTipText("Puerto del servidor");
        panel.add(txtPort);
        
        // Separador
        panel.add(Box.createHorizontalStrut(20));
        
        // Boton conectar
        btnConnect = new JButton("Conectar");
        btnConnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnect.setBackground(new Color(40, 167, 69));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setPreferredSize(new Dimension(130, 35));
        btnConnect.addActionListener(e -> connect());
        panel.add(btnConnect);
        
        // Boton desconectar
        btnDisconnect = new JButton("Desconectar");
        btnDisconnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDisconnect.setBackground(new Color(220, 53, 69));
        btnDisconnect.setForeground(Color.WHITE);
        btnDisconnect.setFocusPainted(false);
        btnDisconnect.setPreferredSize(new Dimension(140, 35));
        btnDisconnect.setEnabled(false);
        btnDisconnect.addActionListener(e -> disconnect());
        panel.add(btnDisconnect);
        
        return panel;
    }
    
    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(50, 50, 55));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Archivos del Servidor ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            Color.WHITE
        ));
        
        // Tabla de archivos
        String[] columns = {"Nombre de Archivo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable = new JTable(tableModel);
        fileTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        fileTable.setRowHeight(25);
        fileTable.setBackground(new Color(40, 40, 45));
        fileTable.setForeground(Color.WHITE);
        fileTable.setSelectionBackground(new Color(0, 122, 204));
        fileTable.setGridColor(new Color(70, 70, 75));
        fileTable.getTableHeader().setBackground(new Color(60, 60, 65));
        fileTable.getTableHeader().setForeground(Color.WHITE);
        fileTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 45));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones para archivos
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        btnPanel.setBackground(new Color(50, 50, 55));
        btnPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        btnRefresh = new JButton("Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshFileList());
        
        btnOpen = new JButton("Abrir");
        btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnOpen.setBackground(new Color(40, 167, 69));
        btnOpen.setForeground(Color.WHITE);
        btnOpen.setFocusPainted(false);
        btnOpen.addActionListener(e -> openSelectedFile());
        
        btnCreate = new JButton("Crear Nuevo");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCreate.setBackground(new Color(255, 193, 7));
        btnCreate.setForeground(Color.BLACK);
        btnCreate.setFocusPainted(false);
        btnCreate.addActionListener(e -> createNewFile());
        
        btnDelete = new JButton("Eliminar");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteSelectedFile());
        
        btnPanel.add(btnRefresh);
        btnPanel.add(btnOpen);
        btnPanel.add(btnCreate);
        btnPanel.add(btnDelete);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(45, 45, 48));
        
        // Panel superior - Editor de contenido
        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        editorPanel.setBackground(new Color(50, 50, 55));
        editorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Editor de Archivo ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            Color.WHITE
        ));
        
        // Nombre del archivo
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBackground(new Color(50, 50, 55));
        namePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JLabel lblFileName = new JLabel("Nombre: ");
        lblFileName.setForeground(Color.WHITE);
        lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        namePanel.add(lblFileName, BorderLayout.WEST);
        
        txtFileName = new JTextField();
        txtFileName.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtFileName.setBackground(new Color(40, 40, 45));
        txtFileName.setForeground(Color.WHITE);
        txtFileName.setCaretColor(Color.WHITE);
        namePanel.add(txtFileName, BorderLayout.CENTER);
        
        btnSave = new JButton("Guardar");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveFile());
        namePanel.add(btnSave, BorderLayout.EAST);
        
        editorPanel.add(namePanel, BorderLayout.NORTH);
        
        // Area de contenido
        txtFileContent = new JTextArea();
        txtFileContent.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtFileContent.setBackground(new Color(30, 30, 32));
        txtFileContent.setForeground(new Color(220, 220, 220));
        txtFileContent.setCaretColor(Color.WHITE);
        txtFileContent.setTabSize(4);
        txtFileContent.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane editorScroll = new JScrollPane(txtFileContent);
        editorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        
        // Panel inferior - Log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(35, 35, 38));
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 105), 1),
            " Log de Operaciones ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            new Color(180, 180, 180)
        ));
        logPanel.setPreferredSize(new Dimension(0, 150));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(35, 35, 38));
        logArea.setForeground(new Color(0, 200, 100));
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.getViewport().setBackground(new Color(35, 35, 38));
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        // Split vertical entre editor y log
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setTopComponent(editorPanel);
        verticalSplit.setBottomComponent(logPanel);
        verticalSplit.setDividerLocation(400);
        verticalSplit.setBackground(new Color(45, 45, 48));
        
        panel.add(verticalSplit, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setOperationButtonsEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
        btnOpen.setEnabled(enabled);
        btnCreate.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        txtFileContent.setEnabled(enabled);
        txtFileName.setEnabled(enabled);
    }
    
    private void connect() {
        try {
            String ip = txtIP.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            
            log("[INFO] Conectando a " + ip + ":" + port + "...");
            
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            isConnected = true;
            
            btnConnect.setEnabled(false);
            btnDisconnect.setEnabled(true);
            txtIP.setEnabled(false);
            txtPort.setEnabled(false);
            setOperationButtonsEnabled(true);
            
            statusPanel.setBackground(new Color(40, 167, 69));
            lblStatus.setText("[ CONECTADO ] " + ip + ":" + port);
            
            log("[OK] Conexion establecida exitosamente");
            
            // Actualizar lista de archivos automaticamente
            refreshFileList();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Puerto invalido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            log("[ERROR] Error de conexion: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "No se pudo conectar al servidor.\n" +
                "Verifique que:\n" +
                "- El servidor este activo\n" +
                "- La IP y puerto sean correctos\n" +
                "- No haya firewall bloqueando\n\n" +
                "Error: " + e.getMessage(), 
                "Error de Conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnect() {
        try {
            if (isConnected) {
                // Enviar comando de desconexion
                FileProtocol request = new FileProtocol(FileProtocol.CMD_DISCONNECT);
                out.writeObject(request);
                out.flush();
            }
        } catch (IOException e) {
            // Ignorar errores al desconectar
        }
        
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {}
        
        isConnected = false;
        
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        txtIP.setEnabled(true);
        txtPort.setEnabled(true);
        setOperationButtonsEnabled(false);
        
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DESCONECTADO ]");
        
        // Limpiar tabla y editor
        tableModel.setRowCount(0);
        txtFileName.setText("");
        txtFileContent.setText("");
        
        log("[INFO] Desconectado del servidor");
    }
    
    private void refreshFileList() {
        if (!isConnected) return;
        
        try {
            FileProtocol request = new FileProtocol(FileProtocol.CMD_LIST_FILES);
            out.writeObject(request);
            out.flush();
            
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                tableModel.setRowCount(0);
                String[] files = response.getFileList();
                if (files != null) {
                    for (String file : files) {
                        tableModel.addRow(new Object[]{file});
                    }
                }
                log("[LISTAR] Lista de archivos actualizada (" + (files != null ? files.length : 0) + " archivos)");
            } else {
                log("[ERROR] Error al obtener lista de archivos");
            }
        } catch (IOException | ClassNotFoundException e) {
            log("[ERROR] Error de comunicacion: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    private void openSelectedFile() {
        if (!isConnected) return;
        
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo de la lista", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            FileProtocol request = new FileProtocol(FileProtocol.CMD_READ_FILE, fileName);
            out.writeObject(request);
            out.flush();
            
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                txtFileName.setText(fileName);
                txtFileContent.setText(response.getContent());
                txtFileContent.setCaretPosition(0);
                log("[LEER] Archivo abierto: " + fileName);
            } else {
                log("[ERROR] Error al abrir archivo: " + response.getContent());
                JOptionPane.showMessageDialog(this, response.getContent(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            log("[ERROR] Error de comunicacion: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    private void createNewFile() {
        if (!isConnected) return;
        
        String fileName = JOptionPane.showInputDialog(this, 
            "Ingrese el nombre del nuevo archivo:", 
            "Crear Nuevo Archivo", JOptionPane.QUESTION_MESSAGE);
        
        if (fileName != null && !fileName.trim().isEmpty()) {
            txtFileName.setText(fileName.trim());
            txtFileContent.setText("");
            log("[CREAR] Preparado para crear nuevo archivo: " + fileName);
        }
    }
    
    private void saveFile() {
        if (!isConnected) return;
        
        String fileName = txtFileName.getText().trim();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre de archivo", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String content = txtFileContent.getText();
        
        try {
            // Verificar si es archivo nuevo o existente
            boolean exists = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (fileName.equals(tableModel.getValueAt(i, 0))) {
                    exists = true;
                    break;
                }
            }
            
            String command = exists ? FileProtocol.CMD_MODIFY_FILE : FileProtocol.CMD_CREATE_FILE;
            FileProtocol request = new FileProtocol(command, fileName, content);
            out.writeObject(request);
            out.flush();
            
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                log("[GUARDAR] Archivo guardado: " + fileName);
                JOptionPane.showMessageDialog(this, 
                    exists ? "Archivo modificado exitosamente" : "Archivo creado exitosamente", 
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
                refreshFileList();
            } else {
                log("[ERROR] Error al guardar: " + response.getContent());
                JOptionPane.showMessageDialog(this, response.getContent(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            log("[ERROR] Error de comunicacion: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    private void deleteSelectedFile() {
        if (!isConnected) return;
        
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo para eliminar", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Esta seguro de eliminar el archivo '" + fileName + "'?", 
            "Confirmar Eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            FileProtocol request = new FileProtocol(FileProtocol.CMD_DELETE_FILE, fileName);
            out.writeObject(request);
            out.flush();
            
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                log("[ELIMINAR] Archivo eliminado: " + fileName);
                
                // Limpiar editor si era el archivo abierto
                if (fileName.equals(txtFileName.getText())) {
                    txtFileName.setText("");
                    txtFileContent.setText("");
                }
                
                refreshFileList();
                JOptionPane.showMessageDialog(this, "Archivo eliminado exitosamente", 
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                log("[ERROR] Error al eliminar: " + response.getContent());
                JOptionPane.showMessageDialog(this, response.getContent(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            log("[ERROR] Error de comunicacion: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    private void handleConnectionError() {
        isConnected = false;
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        txtIP.setEnabled(true);
        txtPort.setEnabled(true);
        setOperationButtonsEnabled(false);
        
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DESCONECTADO ] Error de conexion");
        
        JOptionPane.showMessageDialog(this, 
            "Se perdio la conexion con el servidor", 
            "Error de Conexion", JOptionPane.ERROR_MESSAGE);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}
