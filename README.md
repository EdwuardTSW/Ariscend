# Ariscend 

Ariscend es una aplicación web personal enfocada en organización, disciplina y progreso diario.

Su objetivo es ayudar al usuario a mantener el control de sus hábitos, pendientes, finanzas y notas en un solo lugar, mediante una interfaz moderna, sencilla y enfocada.

## Backend

El backend está construido con Java 21, Spring Boot, Spring Data JPA, PostgreSQL,
Jakarta Validation y Springdoc OpenAPI.

Módulos disponibles:

- Usuarios
- Hábitos y finalizaciones
- Pendientes
- Notas
- Finanzas, categorías, tarjetas, movimientos y metas

### Ejecución local

1. Iniciar PostgreSQL en `localhost:5432`.
2. Crear la base de datos `ariscend_db`.
3. Configurar `DB_PASSWORD` en el entorno o en la configuración de ejecución del IDE.
4. Ejecutar desde `Ariscend_back`:

```powershell
.\gradlew.bat bootRun
```

La contraseña de PostgreSQL no debe escribirse en archivos versionados.

### Documentación API

Con la aplicación iniciada:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Los recursos personales nuevos usan rutas anidadas por usuario:

```text
/api/users/{userId}/habits
/api/users/{userId}/tasks
/api/users/{userId}/notes
/api/users/{userId}/finance
```

El `userId` proporciona aislamiento lógico mientras no exista autenticación. La
identidad real se incorporará posteriormente con Spring Security y JWT.

### Verificación

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test --tests "com.ariscend.backend.service.*ServiceTest"
```

## Frontend

El frontend vive en `Ariscend_front` y utiliza Next.js, React, TypeScript,
Tailwind CSS, componentes compatibles con shadcn/ui, Lucide y Framer Motion.

Con el backend ejecutándose en el puerto 8080:

```powershell
cd Ariscend_front
npm install
npm run dev
```

Abrir `http://localhost:3000`. El frontend usa un proxy interno de Next.js y
lee la URL del backend desde `BACKEND_URL`. Consulta `.env.example` para la
configuración local sin credenciales.

Verificación del frontend:

```powershell
npm run lint
npm run typecheck
npm run build
```
