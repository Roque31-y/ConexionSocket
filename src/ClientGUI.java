// ============================================================================
// IMPORTACIONES - Bibliotecas necesarias para el funcionamiento del cliente
// ============================================================================

// Importa todas las clases de Swing para crear la interfaz grafica
import javax.swing.*;

// Importa clases para bordes decorativos
import javax.swing.border.*;

// Importa clases para el manejo de tablas
import javax.swing.table.*;

// Importa clases de AWT para layouts, colores y fuentes
import java.awt.*;

// Importa clases para entrada/salida de datos
import java.io.*;

// Importa clases para comunicacion por red (sockets)
import java.net.*;

// Importa clase para formatear fechas y horas
import java.text.SimpleDateFormat;

// Importa clase Date para obtener la hora actual
import java.util.Date;

/**
 * Clase ClientGUI - Cliente con interfaz grafica para gestion remota de archivos
 * 
 * Esta clase crea un cliente que:
 * 1. Se conecta a un servidor mediante sockets
 * 2. Permite realizar operaciones sobre archivos remotos
 * 3. Muestra una interfaz grafica con lista de archivos y editor
 * 
 * Extiende JFrame para crear una ventana grafica
 */
public class ClientGUI extends JFrame {
    
    // ============================================================================
    // ATRIBUTOS DE CONEXION (campos de texto y botones)
    // ============================================================================
    
    // Campo de texto para ingresar la IP del servidor
    private JTextField txtIP;
    
    // Campo de texto para ingresar el puerto del servidor
    private JTextField txtPort;
    
    // Boton para establecer conexion con el servidor
    private JButton btnConnect;
    
    // Boton para cerrar la conexion con el servidor
    private JButton btnDisconnect;
    
    // Etiqueta que muestra el estado de conexion
    private JLabel lblStatus;
    
    // Panel de la barra de estado (cambia de color segun estado)
    private JPanel statusPanel;
    
    // ============================================================================
    // ATRIBUTOS DEL PANEL DE ARCHIVOS
    // ============================================================================
    
    // Modelo de datos para la tabla (permite agregar/eliminar filas)
    private DefaultTableModel tableModel;
    
    // Tabla que muestra la lista de archivos del servidor
    private JTable fileTable;
    
    // Area de texto para mostrar/editar el contenido del archivo
    private JTextArea txtFileContent;
    
    // Campo de texto para el nombre del archivo
    private JTextField txtFileName;
    
    // ============================================================================
    // BOTONES DE OPERACIONES
    // ============================================================================
    
    // Boton para actualizar la lista de archivos
    private JButton btnRefresh;
    
    // Boton para abrir/leer un archivo seleccionado
    private JButton btnOpen;
    
    // Boton para crear un nuevo archivo
    private JButton btnCreate;
    
    // Boton para guardar cambios en un archivo
    private JButton btnSave;
    
    // Boton para eliminar un archivo
    private JButton btnDelete;
    
    // ============================================================================
    // ATRIBUTOS DE CONEXION DE RED
    // ============================================================================
    
    // Socket para la conexion con el servidor
    // A diferencia de ServerSocket, este se CONECTA a un servidor
    private Socket socket;
    
    // Stream para enviar objetos al servidor
    private ObjectOutputStream out;
    
    // Stream para recibir objetos del servidor
    private ObjectInputStream in;
    
    // Bandera que indica si hay una conexion activa
    private boolean isConnected = false;
    
    // ============================================================================
    // AREA DE LOG
    // ============================================================================
    
    // Area de texto para mostrar el registro de operaciones
    private JTextArea logArea;
    
    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================
    
    /**
     * Constructor de ClientGUI
     * Inicializa todos los componentes de la interfaz
     */
    public ClientGUI() {
        initComponents();  // Construye la interfaz grafica
    }
    
    // ============================================================================
    // METODO DE INICIALIZACION DE COMPONENTES
    // ============================================================================
    
    /**
     * Construye y configura todos los componentes de la interfaz
     */
    private void initComponents() {
        // Titulo de la ventana
        setTitle("Cliente de Archivos - Socket");
        
        // Al cerrar la ventana, termina la aplicacion
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Tamanio de la ventana (ancho x alto)
        setSize(1000, 700);
        
        // Centra la ventana en la pantalla
        setLocationRelativeTo(null);
        
        // ========================================================================
        // PANEL PRINCIPAL
        // ========================================================================
        
        // Crea el panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Agrega margen interno al panel
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Color de fondo gris oscuro
        mainPanel.setBackground(new Color(45, 45, 48));
        
        // ========================================================================
        // PANEL DE CONEXION (parte superior)
        // ========================================================================
        
        // Crea el panel de conexion
        JPanel connectionPanel = createConnectionPanel();
        
        // Lo agrega en la parte norte (arriba)
        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        
        // ========================================================================
        // PANEL DIVIDIDO (lista de archivos | editor)
        // ========================================================================
        
        // JSplitPane divide la ventana en dos partes redimensionables
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Posicion inicial del divisor (300 pixeles desde la izquierda)
        splitPane.setDividerLocation(300);
        
        splitPane.setBackground(new Color(45, 45, 48));
        
        // Panel izquierdo: lista de archivos
        JPanel fileListPanel = createFileListPanel();
        splitPane.setLeftComponent(fileListPanel);
        
        // Panel derecho: editor y log
        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);
        
        // Agrega el panel dividido en el centro
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // ========================================================================
        // PANEL DE ESTADO (parte inferior)
        // ========================================================================
        
        // Panel para mostrar el estado de conexion
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(220, 53, 69));  // Rojo = desconectado
        
        // Etiqueta con el estado
        lblStatus = new JLabel("[ DESCONECTADO ]");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        statusPanel.add(lblStatus);
        
        // Agrega en la parte sur (abajo)
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Agrega el panel principal a la ventana
        add(mainPanel);
        
        // Deshabilita los botones de operacion hasta que haya conexion
        setOperationButtonsEnabled(false);
    }
    
    // ============================================================================
    // METODO PARA CREAR EL PANEL DE CONEXION
    // ============================================================================
    
    /**
     * Crea el panel superior con los controles de conexion
     * @return JPanel configurado con los controles de conexion
     */
    private JPanel createConnectionPanel() {
        // Panel con FlowLayout centrado
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(60, 60, 65));
        
        // Borde con titulo
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Conexion al Servidor ",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.WHITE
        ));
        
        // --------------------------------------------------------------------
        // Campo de IP
        // --------------------------------------------------------------------
        
        // Etiqueta "IP del Servidor:"
        JLabel lblIP = new JLabel("IP del Servidor:");
        lblIP.setForeground(Color.WHITE);
        lblIP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblIP);
        
        // Campo de texto con valor por defecto 127.0.0.1 (localhost)
        // 127.0.0.1 es la direccion de la propia maquina
        txtIP = new JTextField("127.0.0.1", 12);
        txtIP.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtIP.setToolTipText("Ingrese la IP del servidor (127.0.0.1 para local)");
        panel.add(txtIP);
        
        // Espacio horizontal entre elementos
        panel.add(Box.createHorizontalStrut(10));
        
        // --------------------------------------------------------------------
        // Campo de Puerto
        // --------------------------------------------------------------------
        
        JLabel lblPort = new JLabel("Puerto:");
        lblPort.setForeground(Color.WHITE);
        lblPort.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblPort);
        
        // Puerto por defecto: 5000 (debe coincidir con el servidor)
        txtPort = new JTextField("5000", 6);
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtPort.setToolTipText("Puerto del servidor");
        panel.add(txtPort);
        
        panel.add(Box.createHorizontalStrut(20));
        
        // --------------------------------------------------------------------
        // Boton Conectar
        // --------------------------------------------------------------------
        
        btnConnect = new JButton("Conectar");
        btnConnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnect.setBackground(new Color(40, 167, 69));  // Verde
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setPreferredSize(new Dimension(130, 35));
        
        // Al hacer clic, llama al metodo connect()
        btnConnect.addActionListener(e -> connect());
        panel.add(btnConnect);
        
        // --------------------------------------------------------------------
        // Boton Desconectar
        // --------------------------------------------------------------------
        
        btnDisconnect = new JButton("Desconectar");
        btnDisconnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDisconnect.setBackground(new Color(220, 53, 69));  // Rojo
        btnDisconnect.setForeground(Color.WHITE);
        btnDisconnect.setFocusPainted(false);
        btnDisconnect.setPreferredSize(new Dimension(140, 35));
        btnDisconnect.setEnabled(false);  // Deshabilitado hasta conectar
        
        // Al hacer clic, llama al metodo disconnect()
        btnDisconnect.addActionListener(e -> disconnect());
        panel.add(btnDisconnect);
        
        return panel;
    }
    
    // ============================================================================
    // METODO PARA CREAR EL PANEL DE LISTA DE ARCHIVOS
    // ============================================================================
    
    /**
     * Crea el panel izquierdo con la lista de archivos
     * @return JPanel con la tabla de archivos y botones de operacion
     */
    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(50, 50, 55));
        
        // Borde con titulo
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Archivos del Servidor ",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            Color.WHITE
        ));
        
        // --------------------------------------------------------------------
        // Tabla de archivos
        // --------------------------------------------------------------------
        
        // Define las columnas de la tabla
        String[] columns = {"Nombre de Archivo"};
        
        // Crea el modelo de la tabla (controla los datos)
        // Se sobreescribe isCellEditable para que las celdas no sean editables
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // No permite editar celdas directamente
            }
        };
        
        // Crea la tabla con el modelo
        fileTable = new JTable(tableModel);
        fileTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        fileTable.setRowHeight(25);  // Altura de cada fila
        fileTable.setBackground(new Color(40, 40, 45));
        fileTable.setForeground(Color.WHITE);
        fileTable.setSelectionBackground(new Color(0, 122, 204));  // Color al seleccionar
        fileTable.setGridColor(new Color(70, 70, 75));  // Color de las lineas
        
        // Configura el encabezado de la tabla
        fileTable.getTableHeader().setBackground(new Color(60, 60, 65));
        fileTable.getTableHeader().setForeground(Color.WHITE);
        fileTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Agrega scroll a la tabla
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 45));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // --------------------------------------------------------------------
        // Panel de botones de operacion
        // --------------------------------------------------------------------
        
        // GridLayout con 2 filas y 2 columnas
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        btnPanel.setBackground(new Color(50, 50, 55));
        btnPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Boton ACTUALIZAR - Recarga la lista de archivos
        btnRefresh = new JButton("Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.setBackground(new Color(70, 130, 180));  // Azul
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshFileList());
        
        // Boton ABRIR - Lee el archivo seleccionado
        btnOpen = new JButton("Abrir");
        btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnOpen.setBackground(new Color(40, 167, 69));  // Verde
        btnOpen.setForeground(Color.WHITE);
        btnOpen.setFocusPainted(false);
        btnOpen.addActionListener(e -> openSelectedFile());
        
        // Boton CREAR NUEVO - Prepara para crear un archivo
        btnCreate = new JButton("Crear Nuevo");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCreate.setBackground(new Color(255, 193, 7));  // Amarillo
        btnCreate.setForeground(Color.BLACK);
        btnCreate.setFocusPainted(false);
        btnCreate.addActionListener(e -> createNewFile());
        
        // Boton ELIMINAR - Borra el archivo seleccionado
        btnDelete = new JButton("Eliminar");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDelete.setBackground(new Color(220, 53, 69));  // Rojo
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteSelectedFile());
        
        // Agrega los botones al panel
        btnPanel.add(btnRefresh);
        btnPanel.add(btnOpen);
        btnPanel.add(btnCreate);
        btnPanel.add(btnDelete);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ============================================================================
    // METODO PARA CREAR EL PANEL DERECHO (editor y log)
    // ============================================================================
    
    /**
     * Crea el panel derecho con el editor de archivos y el log
     * @return JPanel con el editor y el area de log
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(45, 45, 48));
        
        // ====================================================================
        // PANEL DEL EDITOR
        // ====================================================================
        
        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        editorPanel.setBackground(new Color(50, 50, 55));
        editorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Editor de Archivo ",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            Color.WHITE
        ));
        
        // --------------------------------------------------------------------
        // Barra superior: nombre del archivo y boton guardar
        // --------------------------------------------------------------------
        
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBackground(new Color(50, 50, 55));
        namePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Etiqueta "Nombre:"
        JLabel lblFileName = new JLabel("Nombre: ");
        lblFileName.setForeground(Color.WHITE);
        lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        namePanel.add(lblFileName, BorderLayout.WEST);
        
        // Campo de texto para el nombre del archivo
        txtFileName = new JTextField();
        txtFileName.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtFileName.setBackground(new Color(40, 40, 45));
        txtFileName.setForeground(Color.WHITE);
        txtFileName.setCaretColor(Color.WHITE);
        namePanel.add(txtFileName, BorderLayout.CENTER);
        
        // Boton GUARDAR
        btnSave = new JButton("Guardar");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveFile());
        namePanel.add(btnSave, BorderLayout.EAST);
        
        editorPanel.add(namePanel, BorderLayout.NORTH);
        
        // --------------------------------------------------------------------
        // Area de contenido del archivo
        // --------------------------------------------------------------------
        
        // Area de texto para editar el contenido
        txtFileContent = new JTextArea();
        txtFileContent.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtFileContent.setBackground(new Color(30, 30, 32));
        txtFileContent.setForeground(new Color(220, 220, 220));
        txtFileContent.setCaretColor(Color.WHITE);
        txtFileContent.setTabSize(4);  // Tamanio del tabulador
        txtFileContent.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Agrega scroll al area de texto
        JScrollPane editorScroll = new JScrollPane(txtFileContent);
        editorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        
        // ====================================================================
        // PANEL DEL LOG
        // ====================================================================
        
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(35, 35, 38));
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 105), 1),
            " Log de Operaciones ",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            new Color(180, 180, 180)
        ));
        
        // Altura fija para el panel de log
        logPanel.setPreferredSize(new Dimension(0, 150));
        
        // Area de texto para el log
        logArea = new JTextArea();
        logArea.setEditable(false);  // Solo lectura
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(35, 35, 38));
        logArea.setForeground(new Color(0, 200, 100));  // Texto verde
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.getViewport().setBackground(new Color(35, 35, 38));
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        // ====================================================================
        // DIVIDIR VERTICALMENTE: Editor arriba, Log abajo
        // ====================================================================
        
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setTopComponent(editorPanel);
        verticalSplit.setBottomComponent(logPanel);
        verticalSplit.setDividerLocation(400);  // Posicion del divisor
        verticalSplit.setBackground(new Color(45, 45, 48));
        
        panel.add(verticalSplit, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ============================================================================
    // METODO PARA HABILITAR/DESHABILITAR BOTONES
    // ============================================================================
    
    /**
     * Habilita o deshabilita todos los botones de operacion
     * @param enabled true para habilitar, false para deshabilitar
     */
    private void setOperationButtonsEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
        btnOpen.setEnabled(enabled);
        btnCreate.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        txtFileContent.setEnabled(enabled);
        txtFileName.setEnabled(enabled);
    }
    
    // ============================================================================
    // METODO PARA CONECTAR AL SERVIDOR
    // ============================================================================
    
    /**
     * Establece conexion con el servidor
     */
    private void connect() {
        try {
            // Obtiene la IP del campo de texto
            String ip = txtIP.getText().trim();
            
            // Obtiene el puerto y lo convierte a entero
            int port = Integer.parseInt(txtPort.getText().trim());
            
            log("[INFO] Conectando a " + ip + ":" + port + "...");
            
            // ================================================================
            // CREAR EL SOCKET - Conexion al servidor
            // ================================================================
            // El constructor Socket(ip, port) intenta conectarse al servidor
            // Si el servidor no esta disponible, lanza una excepcion
            socket = new Socket(ip, port);
            
            // ================================================================
            // CREAR STREAMS DE COMUNICACION
            // ================================================================
            // Estos streams permiten enviar y recibir objetos Java
            // IMPORTANTE: Crear output ANTES que input
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Marca la conexion como activa
            isConnected = true;
            
            // ================================================================
            // ACTUALIZAR INTERFAZ
            // ================================================================
            
            btnConnect.setEnabled(false);       // Deshabilita conectar
            btnDisconnect.setEnabled(true);     // Habilita desconectar
            txtIP.setEnabled(false);            // Deshabilita campos
            txtPort.setEnabled(false);
            setOperationButtonsEnabled(true);   // Habilita botones de operacion
            
            // Cambia la barra de estado a verde
            statusPanel.setBackground(new Color(40, 167, 69));
            lblStatus.setText("[ CONECTADO ] " + ip + ":" + port);
            
            log("[OK] Conexion establecida exitosamente");
            
            // Actualiza automaticamente la lista de archivos
            refreshFileList();
            
        } catch (NumberFormatException e) {
            // Error si el puerto no es un numero
            JOptionPane.showMessageDialog(this, "Puerto invalido", "Error", JOptionPane.ERROR_MESSAGE);
            
        } catch (IOException e) {
            // Error de conexion (servidor no disponible, red caida, etc.)
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
    
    // ============================================================================
    // METODO PARA DESCONECTAR DEL SERVIDOR
    // ============================================================================
    
    /**
     * Cierra la conexion con el servidor
     */
    private void disconnect() {
        try {
            if (isConnected) {
                // Envia comando de desconexion al servidor
                FileProtocol request = new FileProtocol(FileProtocol.CMD_DISCONNECT);
                out.writeObject(request);
                out.flush();
            }
        } catch (IOException e) {
            // Ignorar errores al desconectar
        }
        
        // Cierra los streams y el socket
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            // Ignorar errores al cerrar
        }
        
        // Actualiza el estado
        isConnected = false;
        
        // Restaura la interfaz
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        txtIP.setEnabled(true);
        txtPort.setEnabled(true);
        setOperationButtonsEnabled(false);
        
        // Cambia la barra de estado a rojo
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DESCONECTADO ]");
        
        // Limpia la tabla y el editor
        tableModel.setRowCount(0);  // Elimina todas las filas
        txtFileName.setText("");
        txtFileContent.setText("");
        
        log("[INFO] Desconectado del servidor");
    }
    
    // ============================================================================
    // METODO PARA ACTUALIZAR LA LISTA DE ARCHIVOS
    // ============================================================================
    
    /**
     * Solicita al servidor la lista de archivos y actualiza la tabla
     */
    private void refreshFileList() {
        // Verifica que haya conexion
        if (!isConnected) return;
        
        try {
            // Crea solicitud de tipo LIST_FILES
            FileProtocol request = new FileProtocol(FileProtocol.CMD_LIST_FILES);
            
            // Envia la solicitud al servidor
            out.writeObject(request);
            out.flush();  // Fuerza el envio inmediato
            
            // Espera y recibe la respuesta
            FileProtocol response = (FileProtocol) in.readObject();
            
            // Verifica si la operacion fue exitosa
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                // Limpia la tabla actual
                tableModel.setRowCount(0);
                
                // Obtiene la lista de archivos
                String[] files = response.getFileList();
                
                // Agrega cada archivo como una fila en la tabla
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
            handleConnectionError();  // Maneja el error de conexion
        }
    }
    
    // ============================================================================
    // METODO PARA ABRIR UN ARCHIVO
    // ============================================================================
    
    /**
     * Lee el contenido del archivo seleccionado y lo muestra en el editor
     */
    private void openSelectedFile() {
        if (!isConnected) return;
        
        // Obtiene el indice de la fila seleccionada (-1 si no hay seleccion)
        int selectedRow = fileTable.getSelectedRow();
        
        if (selectedRow == -1) {
            // No hay archivo seleccionado
            JOptionPane.showMessageDialog(this, "Seleccione un archivo de la lista", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtiene el nombre del archivo de la tabla
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Crea solicitud de lectura
            FileProtocol request = new FileProtocol(FileProtocol.CMD_READ_FILE, fileName);
            out.writeObject(request);
            out.flush();
            
            // Recibe la respuesta
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                // Muestra el nombre y contenido en el editor
                txtFileName.setText(fileName);
                txtFileContent.setText(response.getContent());
                txtFileContent.setCaretPosition(0);  // Mueve el cursor al inicio
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
    
    // ============================================================================
    // METODO PARA CREAR UN NUEVO ARCHIVO
    // ============================================================================
    
    /**
     * Prepara el editor para crear un nuevo archivo
     */
    private void createNewFile() {
        if (!isConnected) return;
        
        // Muestra dialogo para ingresar el nombre del nuevo archivo
        String fileName = JOptionPane.showInputDialog(this, 
            "Ingrese el nombre del nuevo archivo:", 
            "Crear Nuevo Archivo", JOptionPane.QUESTION_MESSAGE);
        
        // Verifica que se ingreso un nombre
        if (fileName != null && !fileName.trim().isEmpty()) {
            // Muestra el nombre en el editor y limpia el contenido
            txtFileName.setText(fileName.trim());
            txtFileContent.setText("");
            log("[CREAR] Preparado para crear nuevo archivo: " + fileName);
        }
    }
    
    // ============================================================================
    // METODO PARA GUARDAR UN ARCHIVO
    // ============================================================================
    
    /**
     * Guarda el archivo actual en el servidor (nuevo o modificado)
     */
    private void saveFile() {
        if (!isConnected) return;
        
        // Obtiene el nombre del archivo
        String fileName = txtFileName.getText().trim();
        
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre de archivo", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtiene el contenido del editor
        String content = txtFileContent.getText();
        
        try {
            // Verifica si el archivo ya existe en la lista
            boolean exists = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (fileName.equals(tableModel.getValueAt(i, 0))) {
                    exists = true;
                    break;
                }
            }
            
            // Elige el comando segun si existe o no
            // MODIFY_FILE para archivos existentes, CREATE_FILE para nuevos
            String command = exists ? FileProtocol.CMD_MODIFY_FILE : FileProtocol.CMD_CREATE_FILE;
            
            // Crea y envia la solicitud
            FileProtocol request = new FileProtocol(command, fileName, content);
            out.writeObject(request);
            out.flush();
            
            // Recibe la respuesta
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                log("[GUARDAR] Archivo guardado: " + fileName);
                JOptionPane.showMessageDialog(this, 
                    exists ? "Archivo modificado exitosamente" : "Archivo creado exitosamente", 
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
                    
                // Actualiza la lista de archivos
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
    
    // ============================================================================
    // METODO PARA ELIMINAR UN ARCHIVO
    // ============================================================================
    
    /**
     * Elimina el archivo seleccionado del servidor
     */
    private void deleteSelectedFile() {
        if (!isConnected) return;
        
        // Obtiene la fila seleccionada
        int selectedRow = fileTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo para eliminar", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtiene el nombre del archivo
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        
        // Pide confirmacion al usuario
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Esta seguro de eliminar el archivo '" + fileName + "'?", 
            "Confirmar Eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        // Si no confirmo, cancela la operacion
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            // Crea y envia solicitud de eliminacion
            FileProtocol request = new FileProtocol(FileProtocol.CMD_DELETE_FILE, fileName);
            out.writeObject(request);
            out.flush();
            
            // Recibe la respuesta
            FileProtocol response = (FileProtocol) in.readObject();
            
            if (FileProtocol.RESP_OK.equals(response.getResponse())) {
                log("[ELIMINAR] Archivo eliminado: " + fileName);
                
                // Si el archivo eliminado estaba abierto en el editor, lo limpia
                if (fileName.equals(txtFileName.getText())) {
                    txtFileName.setText("");
                    txtFileContent.setText("");
                }
                
                // Actualiza la lista
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
    
    // ============================================================================
    // METODO PARA MANEJAR ERRORES DE CONEXION
    // ============================================================================
    
    /**
     * Maneja los errores de conexion (se perdio la conexion con el servidor)
     */
    private void handleConnectionError() {
        // Marca como desconectado
        isConnected = false;
        
        // Restaura la interfaz
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        txtIP.setEnabled(true);
        txtPort.setEnabled(true);
        setOperationButtonsEnabled(false);
        
        // Cambia la barra de estado
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DESCONECTADO ] Error de conexion");
        
        // Muestra mensaje de error
        JOptionPane.showMessageDialog(this, 
            "Se perdio la conexion con el servidor", 
            "Error de Conexion", JOptionPane.ERROR_MESSAGE);
    }
    
    // ============================================================================
    // METODO PARA REGISTRAR MENSAJES EN EL LOG
    // ============================================================================
    
    /**
     * Agrega un mensaje al log con marca de tiempo
     * @param message El mensaje a registrar
     */
    private void log(String message) {
        // Ejecuta en el hilo de eventos de Swing para thread-safety
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
            
            // Mueve el scroll al final
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // ============================================================================
    // METODO MAIN - Punto de entrada
    // ============================================================================
    
    /**
     * Metodo principal para ejecutar el cliente de forma independiente
     */
    public static void main(String[] args) {
        // Intenta usar el Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, usa el por defecto
        }
        
        // Crea y muestra la ventana en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}
