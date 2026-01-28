// ============================================================================
// IMPORTACIONES - Bibliotecas necesarias para el funcionamiento del servidor
// ============================================================================

// Importa todas las clases de Swing para crear la interfaz grafica
// Incluye: JFrame, JButton, JPanel, JTextArea, JTextField, etc.
import javax.swing.*;

// Importa clases para bordes decorativos en los paneles
import javax.swing.border.*;

// Importa clases para manejo de layouts y colores
// Incluye: BorderLayout, GridBagLayout, Color, Font, Dimension, etc.
import java.awt.*;

// Importa clases para operaciones de entrada/salida
// Incluye: BufferedReader, BufferedWriter, File, FileReader, FileWriter, etc.
import java.io.*;

// Importa clases para comunicacion de red (sockets)
// Incluye: Socket, ServerSocket, InetAddress
import java.net.*;

// Importa clase para formatear fechas y horas
import java.text.SimpleDateFormat;

// Importa clase Date para obtener la hora actual
import java.util.Date;

/**
 * Clase ServerGUI - Servidor con interfaz grafica para gestion remota de archivos
 * 
 * Esta clase crea un servidor que:
 * 1. Escucha conexiones de clientes en un puerto especifico
 * 2. Recibe comandos de los clientes (listar, leer, escribir, eliminar archivos)
 * 3. Procesa los comandos y responde al cliente
 * 4. Muestra un log de todas las actividades
 * 
 * Extiende JFrame para crear una ventana grafica
 */
public class ServerGUI extends JFrame {
    
    // ============================================================================
    // ATRIBUTOS DE LA INTERFAZ GRAFICA
    // ============================================================================
    
    // Area de texto donde se muestra el registro de actividades (log)
    private JTextArea logArea;
    
    // Boton para iniciar el servidor
    private JButton btnStartServer;
    
    // Boton para detener el servidor
    private JButton btnStopServer;
    
    // Campo de texto para ingresar el numero de puerto
    private JTextField txtPort;
    
    // Campo de texto para mostrar/ingresar el directorio de trabajo
    private JTextField txtDirectory;
    
    // Boton para seleccionar el directorio mediante un explorador
    private JButton btnSelectDir;
    
    // Etiqueta que muestra el estado actual del servidor
    private JLabel lblStatus;
    
    // Panel que contiene la barra de estado (cambia de color segun estado)
    private JPanel statusPanel;
    
    // ============================================================================
    // ATRIBUTOS DE RED Y CONTROL
    // ============================================================================
    
    // Socket del servidor que escucha conexiones entrantes
    // ServerSocket es diferente a Socket: este ESCUCHA, no se conecta
    private ServerSocket serverSocket;
    
    // Bandera que indica si el servidor esta ejecutandose
    // Se usa para controlar el bucle de aceptacion de conexiones
    private boolean isRunning = false;
    
    // Hilo separado donde se ejecuta el servidor
    // Necesario para no bloquear la interfaz grafica
    private Thread serverThread;
    
    // Ruta del directorio donde se almacenan los archivos
    private String workingDirectory;
    
    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================
    
    /**
     * Constructor de ServerGUI
     * Inicializa la interfaz grafica y configura el directorio por defecto
     */
    public ServerGUI() {
        // Llama al metodo que construye todos los componentes graficos
        initComponents();
        
        // Establece el directorio de trabajo por defecto
        // System.getProperty("user.home") obtiene la carpeta del usuario (ej: /home/usuario)
        workingDirectory = System.getProperty("user.home") + "/ServerFiles";
        
        // Muestra la ruta en el campo de texto
        txtDirectory.setText(workingDirectory);
        
        // Crea el directorio si no existe
        File dir = new File(workingDirectory);  // Crea objeto File con la ruta
        if (!dir.exists()) {                    // Verifica si el directorio existe
            dir.mkdirs();                       // Si no existe, lo crea (incluyendo padres)
        }
    }
    
    // ============================================================================
    // METODO DE INICIALIZACION DE COMPONENTES GRAFICOS
    // ============================================================================
    
    /**
     * Construye y configura todos los componentes de la interfaz grafica
     */
    private void initComponents() {
        // Establece el titulo de la ventana
        setTitle("Servidor de Archivos - Socket");
        
        // Define que hacer cuando se cierra la ventana (terminar la aplicacion)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Establece el tamanio de la ventana en pixeles (ancho x alto)
        setSize(800, 600);
        
        // Centra la ventana en la pantalla
        setLocationRelativeTo(null);
        
        // ========================================================================
        // PANEL PRINCIPAL - Contenedor de todos los elementos
        // ========================================================================
        
        // Crea panel principal con BorderLayout (divide en Norte, Sur, Este, Oeste, Centro)
        // Los numeros (10, 10) son los espacios horizontal y vertical entre componentes
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Establece un borde vacio alrededor del panel (margen interno)
        // Los valores son: arriba, izquierda, abajo, derecha
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Establece el color de fondo del panel (gris oscuro)
        mainPanel.setBackground(new Color(45, 45, 48));
        
        // ========================================================================
        // PANEL DE CONFIGURACION - Contiene puerto, directorio y botones
        // ========================================================================
        
        // Crea panel con GridBagLayout (layout flexible tipo cuadricula)
        JPanel configPanel = new JPanel(new GridBagLayout());
        
        // Establece color de fondo
        configPanel.setBackground(new Color(60, 60, 65));
        
        // Crea un borde con titulo alrededor del panel
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),  // Linea azul de 2px
            " Configuracion del Servidor ",  // Texto del titulo
            TitledBorder.LEFT,               // Alineacion del titulo
            TitledBorder.TOP,                // Posicion del titulo
            new Font("Segoe UI", Font.BOLD, 14),  // Fuente del titulo
            Color.WHITE                      // Color del texto
        ));
        
        // Objeto para configurar la posicion de cada componente en el GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Espacio alrededor de cada componente (arriba, izquierda, abajo, derecha)
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Hace que los componentes se expandan horizontalmente
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --------------------------------------------------------------------
        // FILA 0: Etiqueta y campo de texto para el Puerto
        // --------------------------------------------------------------------
        
        // Crea etiqueta "Puerto:"
        JLabel lblPort = new JLabel("Puerto:");
        lblPort.setForeground(Color.WHITE);  // Texto blanco
        lblPort.setFont(new Font("Segoe UI", Font.BOLD, 12));  // Fuente
        
        // Posiciona en columna 0, fila 0
        gbc.gridx = 0; 
        gbc.gridy = 0;
        configPanel.add(lblPort, gbc);  // Agrega al panel con las restricciones
        
        // Crea campo de texto con valor por defecto "5000" y 10 columnas de ancho
        txtPort = new JTextField("5000", 10);
        txtPort.setFont(new Font("Consolas", Font.PLAIN, 14));  // Fuente monoespaciada
        
        // Posiciona en columna 1, fila 0
        gbc.gridx = 1; 
        gbc.gridy = 0;
        configPanel.add(txtPort, gbc);
        
        // --------------------------------------------------------------------
        // FILA 1: Etiqueta, campo de texto y boton para el Directorio
        // --------------------------------------------------------------------
        
        // Crea etiqueta "Directorio:"
        JLabel lblDir = new JLabel("Directorio:");
        lblDir.setForeground(Color.WHITE);
        lblDir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; 
        gbc.gridy = 1;
        configPanel.add(lblDir, gbc);
        
        // Crea campo de texto con 30 columnas de ancho
        txtDirectory = new JTextField(30);
        txtDirectory.setFont(new Font("Consolas", Font.PLAIN, 12));
        gbc.gridx = 1; 
        gbc.gridy = 1;
        configPanel.add(txtDirectory, gbc);
        
        // Crea boton para seleccionar directorio
        btnSelectDir = new JButton("Seleccionar");
        btnSelectDir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSelectDir.setBackground(new Color(70, 70, 75));  // Fondo gris
        btnSelectDir.setForeground(Color.WHITE);            // Texto blanco
        btnSelectDir.setFocusPainted(false);  // Quita el borde de enfoque
        
        // Agrega listener: cuando se hace clic, llama a selectDirectory()
        // La flecha -> es una expresion lambda (forma corta de escribir una funcion)
        btnSelectDir.addActionListener(e -> selectDirectory());
        
        gbc.gridx = 2; 
        gbc.gridy = 1;
        configPanel.add(btnSelectDir, gbc);
        
        // --------------------------------------------------------------------
        // FILA 2: Botones de Iniciar y Detener servidor
        // --------------------------------------------------------------------
        
        // Panel para contener los botones centrados
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(60, 60, 65));
        
        // Boton INICIAR SERVIDOR
        btnStartServer = new JButton("Iniciar Servidor");
        btnStartServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStartServer.setBackground(new Color(40, 167, 69));  // Verde
        btnStartServer.setForeground(Color.WHITE);
        btnStartServer.setFocusPainted(false);
        btnStartServer.setPreferredSize(new Dimension(180, 40));  // Tamanio fijo
        
        // Cuando se hace clic, llama al metodo startServer()
        btnStartServer.addActionListener(e -> startServer());
        
        // Boton DETENER SERVIDOR
        btnStopServer = new JButton("Detener Servidor");
        btnStopServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStopServer.setBackground(new Color(220, 53, 69));  // Rojo
        btnStopServer.setForeground(Color.WHITE);
        btnStopServer.setFocusPainted(false);
        btnStopServer.setPreferredSize(new Dimension(180, 40));
        btnStopServer.setEnabled(false);  // Deshabilitado inicialmente
        
        // Cuando se hace clic, llama al metodo stopServer()
        btnStopServer.addActionListener(e -> stopServer());
        
        // Agrega ambos botones al panel
        buttonPanel.add(btnStartServer);
        buttonPanel.add(btnStopServer);
        
        // Posiciona el panel de botones ocupando 3 columnas
        gbc.gridx = 0; 
        gbc.gridy = 2;
        gbc.gridwidth = 3;  // Ocupa 3 columnas
        configPanel.add(buttonPanel, gbc);
        
        // Agrega el panel de configuracion en la parte NORTE del panel principal
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // ========================================================================
        // PANEL DE LOG - Muestra el registro de actividades
        // ========================================================================
        
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(30, 30, 32));
        
        // Borde con titulo
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
            " Registro de Actividades del Cliente ",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.WHITE
        ));
        
        // Area de texto para el log
        logArea = new JTextArea();
        logArea.setEditable(false);  // No se puede editar
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));  // Fuente monoespaciada
        logArea.setBackground(new Color(30, 30, 32));  // Fondo oscuro
        logArea.setForeground(new Color(0, 255, 127));  // Texto verde
        logArea.setCaretColor(Color.WHITE);  // Color del cursor
        
        // Agrega scroll al area de texto
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setBackground(new Color(30, 30, 32));
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Boton para limpiar el log
        JButton btnClearLog = new JButton("Limpiar Log");
        btnClearLog.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClearLog.setBackground(new Color(70, 70, 75));
        btnClearLog.setForeground(Color.WHITE);
        
        // Al hacer clic, limpia el contenido del log
        btnClearLog.addActionListener(e -> logArea.setText(""));
        
        // Panel para el boton de limpiar (alineado a la derecha)
        JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logButtonPanel.setBackground(new Color(30, 30, 32));
        logButtonPanel.add(btnClearLog);
        logPanel.add(logButtonPanel, BorderLayout.SOUTH);
        
        // Agrega el panel de log en el CENTRO del panel principal
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        // ========================================================================
        // PANEL DE ESTADO - Barra inferior que muestra el estado del servidor
        // ========================================================================
        
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(220, 53, 69));  // Rojo = detenido
        
        lblStatus = new JLabel("[ DETENIDO ] Servidor Detenido");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        statusPanel.add(lblStatus);
        
        // Agrega el panel de estado en la parte SUR del panel principal
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Agrega el panel principal a la ventana
        add(mainPanel);
    }
    
    // ============================================================================
    // METODO PARA SELECCIONAR DIRECTORIO
    // ============================================================================
    
    /**
     * Abre un dialogo para seleccionar el directorio de trabajo
     */
    private void selectDirectory() {
        // Crea un selector de archivos
        JFileChooser chooser = new JFileChooser();
        
        // Configura para seleccionar solo directorios (no archivos)
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // Establece el directorio inicial del selector
        chooser.setCurrentDirectory(new File(workingDirectory));
        
        // Muestra el dialogo y espera la seleccion del usuario
        // showOpenDialog retorna un codigo que indica si se selecciono algo
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Si el usuario selecciono un directorio, obtiene la ruta
            workingDirectory = chooser.getSelectedFile().getAbsolutePath();
            
            // Actualiza el campo de texto
            txtDirectory.setText(workingDirectory);
            
            // Registra el cambio en el log
            log("[INFO] Directorio de trabajo cambiado a: " + workingDirectory);
        }
    }
    
    // ============================================================================
    // METODO PARA INICIAR EL SERVIDOR
    // ============================================================================
    
    /**
     * Inicia el servidor y comienza a escuchar conexiones
     */
    private void startServer() {
        try {
            // Obtiene el puerto del campo de texto y lo convierte a entero
            // trim() elimina espacios en blanco al inicio y final
            int port = Integer.parseInt(txtPort.getText().trim());
            
            // Obtiene el directorio de trabajo
            workingDirectory = txtDirectory.getText().trim();
            
            // Crea el directorio si no existe
            File dir = new File(workingDirectory);
            if (!dir.exists()) {
                dir.mkdirs();  // Crea el directorio y sus padres si es necesario
            }
            
            // ================================================================
            // CREAR EL SERVERSOCKET - Este es el corazon del servidor
            // ================================================================
            // ServerSocket escucha en el puerto especificado
            // Cuando un cliente intenta conectarse, accept() creara un Socket
            serverSocket = new ServerSocket(port);
            
            // Marca el servidor como activo
            isRunning = true;
            
            // ================================================================
            // ACTUALIZAR INTERFAZ - Cambiar estado de botones y etiquetas
            // ================================================================
            
            btnStartServer.setEnabled(false);   // Deshabilita boton iniciar
            btnStopServer.setEnabled(true);     // Habilita boton detener
            txtPort.setEnabled(false);          // Deshabilita campo puerto
            txtDirectory.setEnabled(false);     // Deshabilita campo directorio
            btnSelectDir.setEnabled(false);     // Deshabilita boton seleccionar
            
            // Cambia el color de la barra de estado a verde
            statusPanel.setBackground(new Color(40, 167, 69));
            lblStatus.setText("[ ACTIVO ] Servidor Activo en puerto " + port);
            
            // Registra mensajes en el log
            log("[OK] Servidor iniciado en puerto " + port);
            log("[INFO] Directorio de trabajo: " + workingDirectory);
            
            // Obtiene y muestra la IP local de la maquina
            // Esto es util para que el cliente sepa a que IP conectarse
            log("[INFO] IP Local: " + InetAddress.getLocalHost().getHostAddress());
            log("[ESPERA] Esperando conexiones de clientes...");
            
            // ================================================================
            // CREAR HILO DEL SERVIDOR - Para no bloquear la interfaz
            // ================================================================
            // El servidor debe ejecutarse en un hilo separado porque:
            // 1. accept() es bloqueante (espera hasta que llegue una conexion)
            // 2. Si se ejecutara en el hilo principal, la interfaz se congelaria
            
            serverThread = new Thread(() -> {
                // Este bucle se ejecuta mientras el servidor este activo
                while (isRunning) {
                    try {
                        // ============================================
                        // ACCEPT() - Espera y acepta una conexion
                        // ============================================
                        // Este metodo BLOQUEA hasta que un cliente se conecte
                        // Cuando se conecta, retorna un nuevo Socket para comunicarse
                        Socket clientSocket = serverSocket.accept();
                        
                        // Obtiene la IP del cliente que se conecto
                        String clientIP = clientSocket.getInetAddress().getHostAddress();
                        
                        // Registra la conexion en el log
                        log("[CONEXION] Cliente conectado desde: " + clientIP);
                        
                        // ============================================
                        // CREAR HILO PARA EL CLIENTE
                        // ============================================
                        // Cada cliente se maneja en su propio hilo
                        // Esto permite atender multiples clientes simultaneamente
                        new Thread(new ClientHandler(clientSocket)).start();
                        
                    } catch (IOException e) {
                        // Si ocurre un error y el servidor sigue activo, lo registra
                        if (isRunning) {
                            log("[ERROR] Error aceptando conexion: " + e.getMessage());
                        }
                        // Si isRunning es false, el error es esperado (servidor detenido)
                    }
                }
            });
            
            // Inicia la ejecucion del hilo
            serverThread.start();
            
        } catch (NumberFormatException e) {
            // Error si el puerto no es un numero valido
            JOptionPane.showMessageDialog(this, "Puerto invalido", "Error", JOptionPane.ERROR_MESSAGE);
            
        } catch (IOException e) {
            // Error al crear el ServerSocket (puerto en uso, permisos, etc.)
            log("[ERROR] Error al iniciar servidor: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al iniciar servidor: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ============================================================================
    // METODO PARA DETENER EL SERVIDOR
    // ============================================================================
    
    /**
     * Detiene el servidor y cierra todas las conexiones
     */
    private void stopServer() {
        // Marca el servidor como inactivo
        // Esto hara que el bucle while(isRunning) termine
        isRunning = false;
        
        try {
            // Cierra el ServerSocket si existe y esta abierto
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();  // Esto tambien interrumpe accept()
            }
        } catch (IOException e) {
            log("[AVISO] Error al cerrar servidor: " + e.getMessage());
        }
        
        // Restaura el estado de los botones
        btnStartServer.setEnabled(true);
        btnStopServer.setEnabled(false);
        txtPort.setEnabled(true);
        txtDirectory.setEnabled(true);
        btnSelectDir.setEnabled(true);
        
        // Cambia el color de la barra de estado a rojo
        statusPanel.setBackground(new Color(220, 53, 69));
        lblStatus.setText("[ DETENIDO ] Servidor Detenido");
        
        log("[STOP] Servidor detenido");
    }
    
    // ============================================================================
    // METODO PARA REGISTRAR MENSAJES EN EL LOG
    // ============================================================================
    
    /**
     * Agrega un mensaje al area de log con marca de tiempo
     * 
     * @param message El mensaje a registrar
     */
    private void log(String message) {
        // SwingUtilities.invokeLater asegura que la actualizacion de la UI
        // se haga en el hilo de eventos de Swing (EDT)
        // Esto es necesario porque log() puede ser llamado desde otros hilos
        SwingUtilities.invokeLater(() -> {
            // Crea formateador de hora
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            
            // Agrega el mensaje con la hora actual
            logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
            
            // Mueve el scroll al final para mostrar el mensaje mas reciente
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // ============================================================================
    // CLASE INTERNA: ClientHandler - Maneja la comunicacion con un cliente
    // ============================================================================
    
    /**
     * Clase que maneja la comunicacion con un cliente especifico
     * Implementa Runnable para poder ejecutarse en un hilo separado
     */
    private class ClientHandler implements Runnable {
        
        // Socket para comunicarse con el cliente
        private Socket socket;
        
        // Stream para recibir objetos del cliente
        private ObjectInputStream in;
        
        // Stream para enviar objetos al cliente
        private ObjectOutputStream out;
        
        // IP del cliente (para mostrar en el log)
        private String clientIP;
        
        /**
         * Constructor que recibe el socket del cliente
         * 
         * @param socket Socket de conexion con el cliente
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
            // Obtiene la IP del cliente para identificarlo en los logs
            this.clientIP = socket.getInetAddress().getHostAddress();
        }
        
        /**
         * Metodo principal que se ejecuta cuando inicia el hilo
         * Maneja toda la comunicacion con el cliente
         */
        @Override
        public void run() {
            try {
                // ============================================================
                // CREAR STREAMS DE COMUNICACION
                // ============================================================
                // ObjectOutputStream permite enviar objetos Java serializados
                // ObjectInputStream permite recibir objetos Java serializados
                // IMPORTANTE: El output debe crearse ANTES que el input
                // debido a como funcionan los headers de serializacion
                
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                // Bucle principal: procesa peticiones mientras el socket este abierto
                while (!socket.isClosed()) {
                    try {
                        // ====================================================
                        // RECIBIR PETICION DEL CLIENTE
                        // ====================================================
                        // readObject() bloquea hasta recibir un objeto
                        // El cast (FileProtocol) convierte el objeto generico
                        FileProtocol request = (FileProtocol) in.readObject();
                        
                        // Procesa la peticion y obtiene la respuesta
                        FileProtocol response = processRequest(request);
                        
                        // ====================================================
                        // ENVIAR RESPUESTA AL CLIENTE
                        // ====================================================
                        out.writeObject(response);  // Envia el objeto
                        out.flush();                // Fuerza el envio inmediato
                        
                    } catch (EOFException e) {
                        // EOFException ocurre cuando el cliente cierra la conexion
                        // Es una forma normal de terminar, no es un error
                        break;
                        
                    } catch (ClassNotFoundException e) {
                        // Error si el objeto recibido no es del tipo esperado
                        log("[ERROR] Error de protocolo con cliente " + clientIP);
                        break;
                    }
                }
                
            } catch (IOException e) {
                // Error de comunicacion (cliente desconectado, red caida, etc.)
                log("[DESCONEXION] Cliente " + clientIP + " desconectado");
                
            } finally {
                // El bloque finally SIEMPRE se ejecuta, haya o no errores
                // Aqui cerramos el socket para liberar recursos
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    // Ignoramos errores al cerrar
                }
            }
        }
        
        /**
         * Procesa una peticion del cliente y genera la respuesta
         * 
         * @param request La peticion recibida del cliente
         * @return La respuesta a enviar al cliente
         */
        private FileProtocol processRequest(FileProtocol request) {
            // Crea objeto de respuesta vacio
            FileProtocol response = new FileProtocol();
            
            // Obtiene el comando de la peticion
            String command = request.getCommand();
            
            try {
                // Switch para manejar cada tipo de comando
                switch (command) {
                    
                    // ========================================================
                    // COMANDO: LISTAR ARCHIVOS
                    // ========================================================
                    case FileProtocol.CMD_LIST_FILES:
                        log("[LISTAR] [" + clientIP + "] Solicito listar archivos");
                        
                        // Crea objeto File apuntando al directorio de trabajo
                        File dir = new File(workingDirectory);
                        
                        // Obtiene lista de nombres de archivos/carpetas
                        String[] files = dir.list();
                        
                        // Configura la respuesta
                        response.setCommand(FileProtocol.RESP_FILE_LIST);
                        response.setFileList(files != null ? files : new String[0]);
                        response.setResponse(FileProtocol.RESP_OK);
                        break;
                    
                    // ========================================================
                    // COMANDO: LEER ARCHIVO
                    // ========================================================
                    case FileProtocol.CMD_READ_FILE:
                        // Obtiene el nombre del archivo de la peticion
                        String fileName = request.getFileName();
                        log("[LEER] [" + clientIP + "] Leyendo archivo: " + fileName);
                        
                        // Crea objeto File combinando directorio + nombre
                        File file = new File(workingDirectory, fileName);
                        
                        // Verifica que existe y es un archivo (no directorio)
                        if (file.exists() && file.isFile()) {
                            // StringBuilder para construir el contenido
                            StringBuilder content = new StringBuilder();
                            
                            // try-with-resources: cierra el reader automaticamente
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                String line;
                                // Lee linea por linea hasta el final (null)
                                while ((line = reader.readLine()) != null) {
                                    content.append(line).append("\n");
                                }
                            }
                            
                            // Configura respuesta exitosa con el contenido
                            response.setCommand(FileProtocol.RESP_FILE_CONTENT);
                            response.setContent(content.toString());
                            response.setResponse(FileProtocol.RESP_OK);
                            log("[OK] [" + clientIP + "] Archivo leido exitosamente: " + fileName);
                            
                        } else {
                            // El archivo no existe o no es un archivo valido
                            response.setResponse(FileProtocol.RESP_ERROR);
                            response.setContent("Archivo no encontrado: " + fileName);
                            log("[ERROR] [" + clientIP + "] Archivo no encontrado: " + fileName);
                        }
                        break;
                    
                    // ========================================================
                    // COMANDOS: CREAR O ESCRIBIR ARCHIVO
                    // ========================================================
                    case FileProtocol.CMD_CREATE_FILE:
                    case FileProtocol.CMD_WRITE_FILE:
                        String newFileName = request.getFileName();
                        String newContent = request.getContent();
                        log("[ESCRIBIR] [" + clientIP + "] Creando/Escribiendo archivo: " + newFileName);
                        
                        // Crea el archivo
                        File newFile = new File(workingDirectory, newFileName);
                        
                        // Escribe el contenido usando BufferedWriter
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
                            // Si el contenido es null, escribe cadena vacia
                            writer.write(newContent != null ? newContent : "");
                        }
                        
                        response.setResponse(FileProtocol.RESP_OK);
                        response.setContent("Archivo creado/escrito exitosamente");
                        log("[OK] [" + clientIP + "] Archivo creado/escrito: " + newFileName);
                        break;
                    
                    // ========================================================
                    // COMANDO: MODIFICAR ARCHIVO EXISTENTE
                    // ========================================================
                    case FileProtocol.CMD_MODIFY_FILE:
                        String modFileName = request.getFileName();
                        String modContent = request.getContent();
                        log("[MODIFICAR] [" + clientIP + "] Modificando archivo: " + modFileName);
                        
                        File modFile = new File(workingDirectory, modFileName);
                        
                        // Solo modifica si el archivo existe
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
                    
                    // ========================================================
                    // COMANDO: ELIMINAR ARCHIVO
                    // ========================================================
                    case FileProtocol.CMD_DELETE_FILE:
                        String delFileName = request.getFileName();
                        log("[ELIMINAR] [" + clientIP + "] Eliminando archivo: " + delFileName);
                        
                        File delFile = new File(workingDirectory, delFileName);
                        
                        // Verifica que existe e intenta eliminar
                        // delete() retorna true si la eliminacion fue exitosa
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
                    
                    // ========================================================
                    // COMANDO: DESCONECTAR
                    // ========================================================
                    case FileProtocol.CMD_DISCONNECT:
                        log("[DESCONEXION] [" + clientIP + "] Cliente solicito desconexion");
                        response.setResponse(FileProtocol.RESP_OK);
                        response.setContent("Desconectado exitosamente");
                        
                        // Cierra el socket del cliente
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Ignorar errores al cerrar
                        }
                        break;
                    
                    // ========================================================
                    // COMANDO DESCONOCIDO
                    // ========================================================
                    default:
                        response.setResponse(FileProtocol.RESP_ERROR);
                        response.setContent("Comando desconocido: " + command);
                        log("[AVISO] [" + clientIP + "] Comando desconocido: " + command);
                }
                
            } catch (IOException e) {
                // Error al leer/escribir archivos
                response.setResponse(FileProtocol.RESP_ERROR);
                response.setContent("Error: " + e.getMessage());
                log("[ERROR] [" + clientIP + "] Error procesando solicitud: " + e.getMessage());
            }
            
            // Retorna la respuesta para enviarla al cliente
            return response;
        }
    }
    
    // ============================================================================
    // METODO MAIN - Punto de entrada si se ejecuta directamente
    // ============================================================================
    
    /**
     * Metodo principal para ejecutar el servidor de forma independiente
     */
    public static void main(String[] args) {
        // Intenta usar el Look and Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, usa el Look and Feel por defecto de Java
        }
        
        // Ejecuta la creacion de la ventana en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);  // Crea y muestra la ventana
        });
    }
}
