import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor con interfaz grafica para gestion remota de archivos
 */
public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JButton btnStartServer;
    private JButton btnStopServer;
    private JTextField txtPort;
    private JTextField txtDirectory;
    private JButton btnSelectDir;
    private JLabel lblStatus;
    private JPanel statusPanel;
    
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Thread serverThread;
    private String workingDirectory;
    
    public ServerGUI() {
        initComponents();
        workingDirectory = System.getProperty("user.home") + "/ServerFiles";
        txtDirectory.setText(workingDirectory);
        
        // Crear directorio de trabajo si no existe
        File dir = new File(workingDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    private void initComponents() {
        setTitle("Servidor de Archivos - Socket");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(45, 45, 48));
        
        // Panel de configuracion
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(new Color(60, 60, 65));
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Configuracion del Servidor ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.WHITE
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Puerto
        JLabel lblPort = new JLabel("Puerto:");
        lblPort.setForeground(Color.WHITE);
        lblPort.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(lblPort, gbc);
        
        txtPort = new JTextField("5000", 10);
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 0;
        configPanel.add(txtPort, gbc);
        
        // Directorio
        JLabel lblDir = new JLabel("Directorio:");
        lblDir.setForeground(Color.WHITE);
        lblDir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(lblDir, gbc);
        
        txtDirectory = new JTextField(30);
        txtDirectory.setFont(new Font("Consolas", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1;
        configPanel.add(txtDirectory, gbc);
        
        btnSelectDir = new JButton("Seleccionar");
        btnSelectDir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSelectDir.setBackground(new Color(70, 70, 75));
        btnSelectDir.setForeground(Color.WHITE);
        btnSelectDir.setFocusPainted(false);
        btnSelectDir.addActionListener(e -> selectDirectory());
        gbc.gridx = 2; gbc.gridy = 1;
        configPanel.add(btnSelectDir, gbc);
        
        // Botones de control
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(60, 60, 65));
        
        btnStartServer = new JButton("Iniciar Servidor");
        btnStartServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStartServer.setBackground(new Color(40, 167, 69));
        btnStartServer.setForeground(Color.WHITE);
        btnStartServer.setFocusPainted(false);
        btnStartServer.setPreferredSize(new Dimension(180, 40));
        btnStartServer.addActionListener(e -> startServer());
        
        btnStopServer = new JButton("Detener Servidor");
        btnStopServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStopServer.setBackground(new Color(220, 53, 69));
        btnStopServer.setForeground(Color.WHITE);
        btnStopServer.setFocusPainted(false);
        btnStopServer.setPreferredSize(new Dimension(180, 40));
        btnStopServer.setEnabled(false);
        btnStopServer.addActionListener(e -> stopServer());
        
        buttonPanel.add(btnStartServer);
        buttonPanel.add(btnStopServer);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3;
        configPanel.add(buttonPanel, gbc);
        
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // Panel de log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(30, 30, 32));
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Registro de Actividades del Cliente ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.WHITE
        ));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 32));
        logArea.setForeground(new Color(0, 255, 127));
        logArea.setCaretColor(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setBackground(new Color(30, 30, 32));
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Boton para limpiar log
        JButton btnClearLog = new JButton("Limpiar Log");
        btnClearLog.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClearLog.setBackground(new Color(70, 70, 75));
        btnClearLog.setForeground(Color.WHITE);
        btnClearLog.addActionListener(e -> logArea.setText(""));
        
        JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logButtonPanel.setBackground(new Color(30, 30, 32));
        logButtonPanel.add(btnClearLog);
        logPanel.add(logButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        // Panel de estado
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus = new JLabel("[ DETENIDO ] Servidor Detenido");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        statusPanel.add(lblStatus);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void selectDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(workingDirectory));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            workingDirectory = chooser.getSelectedFile().getAbsolutePath();
            txtDirectory.setText(workingDirectory);
            log("[INFO] Directorio de trabajo cambiado a: " + workingDirectory);
        }
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            workingDirectory = txtDirectory.getText().trim();
            
            // Crear directorio si no existe
            File dir = new File(workingDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            btnStartServer.setEnabled(false);
            btnStopServer.setEnabled(true);
            txtPort.setEnabled(false);
            txtDirectory.setEnabled(false);
            btnSelectDir.setEnabled(false);
            
            statusPanel.setBackground(new Color(40, 167, 69));
            lblStatus.setText("[ ACTIVO ] Servidor Activo en puerto " + port);
            
            log("[OK] Servidor iniciado en puerto " + port);
            log("[INFO] Directorio de trabajo: " + workingDirectory);
            log("[INFO] IP Local: " + InetAddress.getLocalHost().getHostAddress());
            log("[ESPERA] Esperando conexiones de clientes...");
            
            // Hilo para aceptar conexiones
            serverThread = new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientIP = clientSocket.getInetAddress().getHostAddress();
                        log("[CONEXION] Cliente conectado desde: " + clientIP);
                        
                        // Manejar cliente en un nuevo hilo
                        new Thread(new ClientHandler(clientSocket)).start();
                    } catch (IOException e) {
                        if (isRunning) {
                            log("[ERROR] Error aceptando conexion: " + e.getMessage());
                        }
                    }
                }
            });
            serverThread.start();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Puerto invalido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            log("[ERROR] Error al iniciar servidor: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al iniciar servidor: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("[AVISO] Error al cerrar servidor: " + e.getMessage());
        }
        
        btnStartServer.setEnabled(true);
        btnStopServer.setEnabled(false);
        txtPort.setEnabled(true);
        txtDirectory.setEnabled(true);
        btnSelectDir.setEnabled(true);
        
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DETENIDO ] Servidor Detenido");
        
        log("[STOP] Servidor detenido");
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Manejador de clientes
     */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String clientIP;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientIP = socket.getInetAddress().getHostAddress();
        }
        
        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                while (!socket.isClosed()) {
                    try {
                        FileProtocol request = (FileProtocol) in.readObject();
                        FileProtocol response = processRequest(request);
                        out.writeObject(response);
                        out.flush();
                    } catch (EOFException e) {
                        break;
                    } catch (ClassNotFoundException e) {
                        log("[ERROR] Error de protocolo con cliente " + clientIP);
                        break;
                    }
                }
            } catch (IOException e) {
                log("[DESCONEXION] Cliente " + clientIP + " desconectado");
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {}
            }
        }
        
        private FileProtocol processRequest(FileProtocol request) {
            FileProtocol response = new FileProtocol();
            String command = request.getCommand();
            
            try {
                switch (command) {
                    case FileProtocol.CMD_LIST_FILES:
                        log("[LISTAR] [" + clientIP + "] Solicito listar archivos");
                        File dir = new File(workingDirectory);
                        String[] files = dir.list();
                        response.setCommand(FileProtocol.RESP_FILE_LIST);
                        response.setFileList(files != null ? files : new String[0]);
                        response.setResponse(FileProtocol.RESP_OK);
                        break;
                        
                    case FileProtocol.CMD_READ_FILE:
                        String fileName = request.getFileName();
                        log("[LEER] [" + clientIP + "] Leyendo archivo: " + fileName);
                        File file = new File(workingDirectory, fileName);
                        if (file.exists() && file.isFile()) {
                            StringBuilder content = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    content.append(line).append("\n");
                                }
                            }
                            response.setCommand(FileProtocol.RESP_FILE_CONTENT);
                            response.setContent(content.toString());
                            response.setResponse(FileProtocol.RESP_OK);
                            log("[OK] [" + clientIP + "] Archivo leido exitosamente: " + fileName);
                        } else {
                            response.setResponse(FileProtocol.RESP_ERROR);
                            response.setContent("Archivo no encontrado: " + fileName);
                            log("[ERROR] [" + clientIP + "] Archivo no encontrado: " + fileName);
                        }
                        break;
                        
                    case FileProtocol.CMD_CREATE_FILE:
                    case FileProtocol.CMD_WRITE_FILE:
                        String newFileName = request.getFileName();
                        String newContent = request.getContent();
                        log("[ESCRIBIR] [" + clientIP + "] Creando/Escribiendo archivo: " + newFileName);
                        File newFile = new File(workingDirectory, newFileName);
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
                            writer.write(newContent != null ? newContent : "");
                        }
                        response.setResponse(FileProtocol.RESP_OK);
                        response.setContent("Archivo creado/escrito exitosamente");
                        log("[OK] [" + clientIP + "] Archivo creado/escrito: " + newFileName);
                        break;
                        
                    case FileProtocol.CMD_MODIFY_FILE:
                        String modFileName = request.getFileName();
                        String modContent = request.getContent();
                        log("[MODIFICAR] [" + clientIP + "] Modificando archivo: " + modFileName);
                        File modFile = new File(workingDirectory, modFileName);
                        if (modFile.exists()) {
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(modFile))) {
                                writer.write(modContent != null ? modContent : "");
                            }
                            response.setResponse(FileProtocol.RESP_OK);
                            response.setContent("Archivo modificado exitosamente");
                            log("[OK] [" + clientIP + "] Archivo modificado: " + modFileName);
                        } else {
                            response.setResponse(FileProtocol.RESP_ERROR);
                            response.setContent("Archivo no encontrado");
                            log("[ERROR] [" + clientIP + "] No se pudo modificar, archivo no existe: " + modFileName);
                        }
                        break;
                        
                    case FileProtocol.CMD_DELETE_FILE:
                        String delFileName = request.getFileName();
                        log("[ELIMINAR] [" + clientIP + "] Eliminando archivo: " + delFileName);
                        File delFile = new File(workingDirectory, delFileName);
                        if (delFile.exists() && delFile.delete()) {
                            response.setResponse(FileProtocol.RESP_OK);
                            response.setContent("Archivo eliminado exitosamente");
                            log("[OK] [" + clientIP + "] Archivo eliminado: " + delFileName);
                        } else {
                            response.setResponse(FileProtocol.RESP_ERROR);
                            response.setContent("No se pudo eliminar el archivo");
                            log("[ERROR] [" + clientIP + "] No se pudo eliminar: " + delFileName);
                        }
                        break;
                        
                    case FileProtocol.CMD_DISCONNECT:
                        log("[DESCONEXION] [" + clientIP + "] Cliente solicito desconexion");
                        response.setResponse(FileProtocol.RESP_OK);
                        response.setContent("Desconectado exitosamente");
                        try {
                            socket.close();
                        } catch (IOException e) {}
                        break;
                        
                    default:
                        response.setResponse(FileProtocol.RESP_ERROR);
                        response.setContent("Comando desconocido: " + command);
                        log("[AVISO] [" + clientIP + "] Comando desconocido: " + command);
                }
            } catch (IOException e) {
                response.setResponse(FileProtocol.RESP_ERROR);
                response.setContent("Error: " + e.getMessage());
                log("[ERROR] [" + clientIP + "] Error procesando solicitud: " + e.getMessage());
            }
            
            return response;
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}
