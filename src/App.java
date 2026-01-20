import javax.swing.*;
import java.awt.*;

/**
 * Aplicacion principal - Lanzador de Servidor o Cliente
 * Sistema de gestion de archivos remoto via Sockets
 */
public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            showLauncher();
        });
    }
    
    private static void showLauncher() {
        JFrame launcher = new JFrame("Socket File Manager - Selector");
        launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        launcher.setSize(450, 300);
        launcher.setLocationRelativeTo(null);
        launcher.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(45, 45, 48));
        
        // Titulo
        JLabel titleLabel = new JLabel("Sistema de Archivos por Socket", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 122, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        buttonPanel.setBackground(new Color(45, 45, 48));
        
        // Boton Servidor
        JButton btnServer = new JButton("<html><center><b>SERVIDOR</b><br><small>Abrir conexion y<br>gestionar archivos</small></center></html>");
        btnServer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnServer.setBackground(new Color(40, 167, 69));
        btnServer.setForeground(Color.WHITE);
        btnServer.setFocusPainted(false);
        btnServer.setPreferredSize(new Dimension(170, 120));
        btnServer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnServer.addActionListener(e -> {
            launcher.dispose();
            new ServerGUI().setVisible(true);
        });
        
        // Boton Cliente
        JButton btnClient = new JButton("<html><center><b>CLIENTE</b><br><small>Conectar a servidor<br>y operar archivos</small></center></html>");
        btnClient.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClient.setBackground(new Color(0, 122, 204));
        btnClient.setForeground(Color.WHITE);
        btnClient.setFocusPainted(false);
        btnClient.setPreferredSize(new Dimension(170, 120));
        btnClient.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClient.addActionListener(e -> {
            launcher.dispose();
            new ClientGUI().setVisible(true);
        });
        
        buttonPanel.add(btnServer);
        buttonPanel.add(btnClient);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Informacion
        JLabel infoLabel = new JLabel("<html><center><small>Para usar en la misma computadora: Abre Servidor y luego Cliente<br>Para otra PC: Usa la IP del servidor en el cliente</small></center></html>", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(150, 150, 150));
        mainPanel.add(infoLabel, BorderLayout.SOUTH);
        
        launcher.add(mainPanel);
        launcher.setVisible(true);
    }
}
