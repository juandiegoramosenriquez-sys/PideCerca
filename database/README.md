# Base de datos PideCerca

PideCerca usa **PostgreSQL** (en Supabase). El backend crea y actualiza las tablas solo, gracias a `spring.jpa.hibernate.ddl-auto=update`, así que no hace falta ejecutar nada a mano.

## Archivos

- `schema.sql`: estructura completa de las tablas, relaciones e índices. Sirve como documentación o para crear la base desde cero en otro servidor PostgreSQL.

## Datos de prueba

Los datos de prueba los carga el backend automáticamente la primera vez que arranca (clase `DataInitializer`), solo si la base no tiene negocios. Las contraseñas se guardan cifradas con BCrypt.

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Administrador | admin@pidecerca.com | admin123 |
| Negocio (Hamburguesería Don Lucho) | negocio@pidecerca.com | negocio123 |
| Negocio (Pollería La Esquina) | polleria@pidecerca.com | negocio123 |
| Negocio (Salchipapas El Gordito) | salchipapas@pidecerca.com | negocio123 |
| Cliente | cliente@pidecerca.com | cliente123 |
| Cliente | luis@pidecerca.com | cliente123 |

Para no cargar datos de prueba, arranca el backend con la variable de entorno `DEMO=false`. Para cambiar el administrador inicial usa `ADMIN_CORREO` y `ADMIN_PASSWORD`.
