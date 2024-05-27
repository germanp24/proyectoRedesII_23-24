![Image](https://www.uclm.es/images/logos/Logo_uclm.png)

# Proyecto de Redes II (2023-24)

Este es el repositorio que contiene el proyecto de la asignatura Redes II (2023-24).

## Integrantes del Equipo ("Grupo 1")

- Germán Pajarero
- Houda El Ouahabi
- Oumaima Darkaoui
- Zineb El Ouaazizi

## Ejecución del Proyecto

Para el correcto uso del proyecto se debe hacer lo siguiente:
* Descargar código fuente.
* Asegurarse que en RCS (servidor) esté correctamente especificado el archivo .jks del servidor (ver variables).
* Compilar archivos .java como javac
* Ejecutar los archivos ya compilados con la siguiente sintáxis:
  * **java RCS [modo] [puerto] [número máximo de clientes]**
    * **Modo** -> "normal" o "ssl"
    * **Puerto** -> Nosotros hemos escogido 8008
    * **Número máximo de clientes** -> Número arbitrario >0

  * **java RCC [modo] [host] [puerto] [nombre del directorio de cliente]**
    * **Modo** -> "normal" o "ssl"
    * **Host** -> IP del servidor
    * **Puerto** -> Nosotros hemos escogido 8008 (debe coincidir con el servidor)
    * **Nombre del directorio de cliente** -> Nombre para generar el directorio de cliente, se usará para las pruebas con archivos.