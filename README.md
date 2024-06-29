Proyecto de Gestión de Alquileres de Libros de una Biblioteca con MVC y JDBC
Descripción del Proyecto

En este proyecto se desarrollará una aplicación que permita gestionar los alquileres de libros de una biblioteca utilizando el patrón de diseño MVC (Modelo-Vista-Controlador). La biblioteca consta de varios socios y libros, y la aplicación permitirá ver y gestionar la información de ambos.

Un socio puede alquilar múltiples libros disponibles y devolverlos cuando desee. Además, la aplicación permitirá ver la información histórica de los alquileres.
Funcionalidades de la Aplicación
    Ver la información de los socios de la biblioteca.
    Ver los libros disponibles (no alquilados) en un momento dado.
    Ver los libros que están alquilados, incluyendo la fecha de alquiler.
    Ver un historial de los libros alquilados en el pasado, mostrando quién alquiló cada libro y en qué fechas.

Modelo
El modelo incluye clases para representar los datos de la biblioteca y las operaciones de acceso a la base de datos utilizando JDBC.

Vista
La vista incluye las interfaces gráficas de usuario para mostrar y manipular la información de la biblioteca.

Controlador
El controlador maneja los eventos de la vista y realiza las operaciones correspondientes en el modelo.
