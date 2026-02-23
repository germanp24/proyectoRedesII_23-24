![Logo UCLM](https://www.uclm.es/images/logos/Logo_uclm.png)

# 🌐 Proyecto de Redes II (2023-24)

Repositorio del proyecto de la asignatura **Redes II** — Universidad de Castilla-La Mancha (UCLM)

---

## 👥 Integrantes del Equipo — *Grupo 1*

| Nombre |
|--------|
| Germán Pajarero |
| Houda El Ouahabi |
| Oumaima Darkaoui |
| Zineb El Ouaazizi |

---

## 🚀 Ejecución del Proyecto

### Requisitos previos

- Tener instalado el JDK (Java Development Kit).
- Asegurarse de que en **RCS (servidor)** esté correctamente especificado el archivo `.jks` del servidor (ver variables de configuración).

### Pasos

1. **Descargar** el código fuente del repositorio.
2. **Compilar** los archivos `.java`:
   ```bash
   javac *.java
   ```
3. **Ejecutar** los archivos compilados con la siguiente sintaxis:

---

### 🖥️ Servidor — RCS

```bash
java RCS [modo] [puerto] [número máximo de clientes]
```

| Parámetro | Descripción | Valor usado |
|-----------|-------------|-------------|
| `modo` | `normal` o `ssl` | — |
| `puerto` | Puerto de escucha del servidor | `8008` |
| `número máximo de clientes` | Número arbitrario > 0 | — |

**Ejemplo:**
```bash
java RCS ssl 8008 10
```

---

### 💻 Cliente — RCC

```bash
java RCC [modo] [host] [puerto] [nombre del directorio de cliente]
```

| Parámetro | Descripción | Valor usado |
|-----------|-------------|-------------|
| `modo` | `normal` o `ssl` | — |
| `host` | IP del servidor | — |
| `puerto` | Debe coincidir con el del servidor | `8008` |
| `nombre del directorio de cliente` | Nombre para el directorio del cliente (usado en pruebas con archivos) | — |

**Ejemplo:**
```bash
java RCC ssl 192.168.1.10 8008 cliente1
```

---

## 📄 Licencia

Este proyecto ha sido desarrollado con fines académicos en el marco de la asignatura Redes II de la UCLM.
