// ============================================================================
// IMPORTACIONES - Bibliotecas necesarias para la aplicacion
// ============================================================================

// Importa todas las clases de Swing para crear la interfaz grafica
// Swing es la biblioteca de Java para crear interfaces graficas (GUI)
import javax.swing.*;

// Importa clases de AWT para layouts, colores, fuentes y dimensiones
// AWT (Abstract Window Toolkit) es la base de Swing
import java.awt.*;

/**
 * Clase App - Punto de entrada principal de la aplicacion
 * 
 * Esta clase funciona como un "lanzador" que permite al usuario
 * elegir si quiere iniciar el Servidor o el Cliente.
 * 
 * Flujo de ejecucion:
 * 1. Se ejecuta esta clase (java App)
 * 2. Muestra una ventana con dos botones: "Servidor" y "Cliente"
 * 3. Al hacer clic en un boton, abre la aplicacion correspondiente
 * 4. Este lanzador se cierra y se abre la ventana seleccionada
 */
public class App {
    
    // ============================================================================
    // METODO MAIN - Punto de entrada del programa
    // ============================================================================
    
    /**
     * Metodo principal que Java ejecuta al iniciar el programa
     * @param args Argumentos de linea de comandos (no se usan en este programa)
     */
    public static void main(String[] args) {
        
        // ====================================================================
        // CONFIGURAR EL LOOK AND FEEL
        // ====================================================================
        // El "Look and Feel" define la apariencia visual de los componentes
        // Intentamos usar el del sistema operativo para verse nativo
        try {
            // Obtiene el nombre de la clase del L&F del sistema
            // En Windows: com.sun.java.swing.plaf.windows.WindowsLookAndFeel
            // En Linux/GTK: com.sun.java.swing.plaf.gtk.GTKLookAndFeel
            // En Mac: com.apple.laf.AquaLookAndFeel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        } catch (Exception e) {
            // Si falla, Java usara el Look and Feel por defecto (Metal)
            // Esto no es un error critico, solo afecta la apariencia
        }
        
        // ====================================================================
        // EJECUTAR EN EL HILO DE EVENTOS DE SWING (EDT)
        // ====================================================================
        // SwingUtilities.invokeLater() ejecuta el codigo en el EDT
        // El EDT (Event Dispatch Thread) es el hilo donde deben ejecutarse
        // todas las operaciones de la interfaz grafica en Swing
        // Esto garantiza que la UI sea thread-safe
        SwingUtilities.invokeLater(() -> {
            // Llama al metodo que muestra la ventana del lanzador
            showLauncher();
        });
    }
    
    // ============================================================================
    // METODO SHOWLAUNCHER - Crea y muestra la ventana del selector
    // ============================================================================
    
    /**
     * Crea y muestra la ventana del lanzador con los botones de seleccion
     * Esta ventana permite elegir entre iniciar Servidor o Cliente
     */
    private static void showLauncher() {
        
        // ====================================================================
        // CREAR LA VENTANA PRINCIPAL (JFrame)
        // ====================================================================
        // JFrame es la clase que representa una ventana con barra de titulo
        JFrame launcher = new JFrame("Seleccionar Aplicacion");
        
        // Define que hacer al cerrar la ventana
        // EXIT_ON_CLOSE termina el programa completamente
        launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Establece el tamanio de la ventana (ancho x alto en pixeles)
        launcher.setSize(400, 200);
        
        // Centra la ventana en la pantalla
        // null = relativo a toda la pantalla
        launcher.setLocationRelativeTo(null);
        
        // Evita que el usuario pueda redimensionar la ventana
        launcher.setResizable(false);
        
        // ====================================================================
        // CREAR EL PANEL PRINCIPAL
        // ====================================================================
        // JPanel es un contenedor que agrupa otros componentes
        // GridLayout(3, 1) crea una cuadricula de 3 filas y 1 columna
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        
        // Establece el color de fondo del panel (gris oscuro)
        // new Color(R, G, B) crea un color con valores 0-255
        panel.setBackground(new Color(45, 45, 48));
        
        // Agrega un margen interno (padding) al panel
        // Los numeros son: arriba, izquierda, abajo, derecha
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // ====================================================================
        // CREAR LA ETIQUETA DE TITULO
        // ====================================================================
        // JLabel muestra texto (no editable por el usuario)
        JLabel lblTitle = new JLabel("Sistema de Gestion de Archivos");
        
        // CENTER alinea el texto al centro horizontalmente
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Establece la fuente: familia, estilo, tamanio
        // Font.BOLD = texto en negrita
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Color del texto (blanco)
        lblTitle.setForeground(Color.WHITE);
        
        // ====================================================================
        // CREAR EL BOTON DE SERVIDOR
        // ====================================================================
        // JButton es un boton clickeable
        JButton btnServer = new JButton("Iniciar como Servidor");
        
        // Fuente del texto del boton
        btnServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Color de fondo del boton (verde)
        btnServer.setBackground(new Color(40, 167, 69));
        
        // Color del texto del boton (blanco)
        btnServer.setForeground(Color.WHITE);
        
        // Elimina el rectangulo de enfoque que aparece al hacer clic
        btnServer.setFocusPainted(false);
        
        // --------------------------------------------------------------------
        // AGREGAR ACTION LISTENER (manejador de eventos)
        // --------------------------------------------------------------------
        // addActionListener define que hacer cuando se hace clic en el boton
        // La expresion lambda (e -> {...}) es una funcion anonima
        // 'e' es el evento de accion (contiene info del clic)
        btnServer.addActionListener(e -> {
            // Cierra la ventana del lanzador
            // dispose() libera los recursos de la ventana
            launcher.dispose();
            
            // Crea una nueva instancia de ServerGUI (el servidor)
            // setVisible(true) hace que la ventana sea visible
            new ServerGUI().setVisible(true);
        });
        
        // ====================================================================
        // CREAR EL BOTON DE CLIENTE
        // ====================================================================
        JButton btnClient = new JButton("Iniciar como Cliente");
        btnClient.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClient.setBackground(new Color(0, 123, 255));  // Azul
        btnClient.setForeground(Color.WHITE);
        btnClient.setFocusPainted(false);
        
        // Manejador de eventos para el boton Cliente
        btnClient.addActionListener(e -> {
            // Cierra el lanzador
            launcher.dispose();
            
            // Abre la ventana del cliente
            new ClientGUI().setVisible(true);
        });
        
        // ====================================================================
        // AGREGAR COMPONENTES AL PANEL
        // ====================================================================
        // El orden de agregado determina la posicion en el GridLayout
        // Fila 1: Titulo
        // Fila 2: Boton Servidor
        // Fila 3: Boton Cliente
        panel.add(lblTitle);      // Primera fila
        panel.add(btnServer);     // Segunda fila
        panel.add(btnClient);     // Tercera fila
        
        // ====================================================================
        // AGREGAR EL PANEL A LA VENTANA Y MOSTRAR
        // ====================================================================
        // Agrega el panel al contenido de la ventana
        launcher.add(panel);
        
        // Hace visible la ventana
        // Si esto no se llama, la ventana existe pero no se ve
        launcher.setVisible(true);
    }
}
