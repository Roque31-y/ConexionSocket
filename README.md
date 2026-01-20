# Sistema de Gestion de Archivos por Socket

## Descripcion

Este proyecto implementa un sistema cliente-servidor para la gestion remota de archivos utilizando sockets TCP/IP en Java. Permite a un cliente conectarse a un servidor y realizar operaciones como crear, leer, modificar y eliminar archivos de forma remota a traves de una interfaz grafica.

---

## Requisitos del Sistema

- **Java JDK 8** o superior
- Sistema operativo: Windows, Linux o macOS
- Para conexion en red: Ambas computadoras deben estar en la misma red

---

## Estructura del Proyecto

```
socket/
├── src/
│   ├── App.java           # Lanzador principal (selector servidor/cliente)
│   ├── ServerGUI.java     # Interfaz grafica del servidor
│   ├── ClientGUI.java     # Interfaz grafica del cliente
│   └── FileProtocol.java  # Protocolo de comunicacion
├── bin/                   # Archivos compilados (.class)
├── README.md              # Este archivo
└── documentacion_tecnica.txt  # Explicacion tecnica detallada
```

---

## Compilacion

Abra una terminal en la carpeta del proyecto y ejecute:

```bash
javac -d bin src/*.java
```

Esto compilara todos los archivos Java y colocara los archivos .class en la carpeta `bin/`.

---

## Ejecucion

### Iniciar la aplicacion

```bash
java -cp bin App
```

Esto abrira una ventana de seleccion donde podra elegir iniciar como **Servidor** o como **Cliente**.

### Iniciar solo el servidor

```bash
java -cp bin ServerGUI
```

### Iniciar solo el cliente

```bash
java -cp bin ClientGUI
```

---

## Manual de Usuario

### Uso en una sola computadora (modo local)

1. **Ejecutar el programa**: `java -cp bin App`
2. **Abrir el Servidor**:
   - Haga clic en el boton "SERVIDOR"
   - Configure el puerto (por defecto: 5000)
   - Seleccione el directorio donde se guardaran los archivos
   - Haga clic en "Iniciar Servidor"
   - El servidor mostrara su IP local en el log

3. **Abrir el Cliente** (en otra ventana):
   - Ejecute nuevamente: `java -cp bin App`
   - Haga clic en el boton "CLIENTE"
   - En "IP del Servidor" ingrese: `127.0.0.1` (localhost)
   - En "Puerto" ingrese: `5000` (o el que configuro en el servidor)
   - Haga clic en "Conectar"

### Uso entre dos computadoras (modo red)

1. **En la computadora SERVIDOR**:
   - Ejecute: `java -cp bin ServerGUI`
   - Inicie el servidor
   - Anote la IP que aparece en el log (ejemplo: 192.168.1.100)
   - Asegurese de que el firewall permita conexiones en el puerto

2. **En la computadora CLIENTE**:
   - Ejecute: `java -cp bin ClientGUI`
   - En "IP del Servidor" ingrese la IP del servidor (ejemplo: 192.168.1.100)
   - En "Puerto" ingrese el mismo puerto del servidor
   - Haga clic en "Conectar"

---

## Operaciones Disponibles

### En el Cliente

| Boton | Funcion |
|-------|---------|
| **Conectar** | Establece conexion con el servidor |
| **Desconectar** | Cierra la conexion con el servidor |
| **Actualizar** | Refresca la lista de archivos del servidor |
| **Abrir** | Abre el archivo seleccionado para ver/editar |
| **Crear Nuevo** | Prepara el editor para crear un nuevo archivo |
| **Guardar** | Guarda el archivo (nuevo o modificado) en el servidor |
| **Eliminar** | Elimina el archivo seleccionado del servidor |

### En el Servidor

| Boton | Funcion |
|-------|---------|
| **Iniciar Servidor** | Abre el puerto y comienza a escuchar conexiones |
| **Detener Servidor** | Cierra el puerto y desconecta todos los clientes |
| **Seleccionar** | Elige el directorio donde se almacenan los archivos |
| **Limpiar Log** | Borra el historial de actividades |

---

## Flujo de Trabajo Tipico

1. Inicie el servidor y espere a que muestre "Esperando conexiones..."
2. Conecte el cliente ingresando IP y puerto correctos
3. La lista de archivos se actualizara automaticamente
4. Seleccione un archivo y haga clic en "Abrir" para verlo
5. Modifique el contenido en el editor
6. Haga clic en "Guardar" para enviar los cambios al servidor
7. Para crear un nuevo archivo:
   - Haga clic en "Crear Nuevo"
   - Ingrese el nombre del archivo
   - Escriba el contenido
   - Haga clic en "Guardar"
8. Al terminar, haga clic en "Desconectar"

---

## Solucion de Problemas

### "No se pudo conectar al servidor"
- Verifique que el servidor este iniciado
- Confirme que la IP y el puerto sean correctos
- Si esta en red, verifique que ambas computadoras esten en la misma red
- Revise que el firewall no este bloqueando el puerto

### "Error al iniciar servidor"
- El puerto puede estar en uso por otra aplicacion
- Pruebe con otro puerto (ejemplo: 5001, 8080)

### Los archivos no aparecen
- Verifique que el directorio del servidor exista
- Haga clic en "Actualizar" para refrescar la lista

---

## Indicadores de Estado

### Servidor
- **Barra verde**: Servidor activo y escuchando
- **Barra roja**: Servidor detenido

### Cliente
- **Barra verde**: Conectado al servidor
- **Barra roja**: Desconectado

---

## Notas Importantes

- Los archivos se almacenan en el directorio configurado en el servidor
- Por defecto, el directorio es: `[carpeta_usuario]/ServerFiles`
- El servidor puede atender multiples clientes simultaneamente
- Todas las operaciones del cliente se registran en el log del servidor
- La comunicacion es mediante TCP, garantizando la entrega de datos

---

## Autor

Proyecto de ejemplo para demostracion de sockets en Java con interfaz grafica.

## Licencia

Este proyecto es de uso educativo.
