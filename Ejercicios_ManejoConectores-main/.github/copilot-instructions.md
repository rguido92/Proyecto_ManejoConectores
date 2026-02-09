## Web prototype instructions (added)

This project now includes a minimal web frontend prototype (no external libs) for the `ejercicio307` students example.

What was added
- `src/server/WebServer.java` — small HTTP server (uses JDK's `com.sun.net.httpserver`) exposing REST endpoints under `/api/students` and serving static files from `web/`.
- `web/index.html`, `web/app.js`, `web/style.css` — static single-page UI to list and manage students.
- `src/ejercicio307/ManageStudents.java` — a new public method `modifyStudent(String id, Student)` and a corrected `getStudents()` so the server can reuse DB logic.

Maven + JSON
- A `pom.xml` was added so you can build the project with Maven. It configures the existing `src/` as the source directory and includes resources from `web/`.
- The server now uses Jackson (`jackson-databind`) for JSON serialization/deserialization. This replaces the previous ad-hoc parser.

How to run (Windows PowerShell)
1. Ensure the MySQL JDBC driver jar is in `lib/` (e.g. `mysql-connector-java-x.x.x.jar`).
2. Using Maven (recommended) compile and run:
```powershell
mvn -f Ejercicios_ManejoConectores-main\pom.xml compile
mvn -f Ejercicios_ManejoConectores-main\pom.xml exec:java -Dexec.mainClass="server.WebServer"
```
3. (Alternative) If you prefer manual compilation keep using the `javac`/`java` commands shown earlier — but Maven handles dependencies like Jackson and the MySQL driver.
4. Open http://localhost:8000 in your browser. The UI will call the API endpoints implemented in `WebServer`.

Notes and next steps
- This prototype uses an ad-hoc JSON parser and inlined DB credentials for speed of iteration. If you want production-ready behavior, we should:
  - DB credentials are now read from environment variables (preferred) and fall back to `config/server.properties`.
  - JSON parsing/serialization now uses Jackson. A `pom.xml` was added to manage dependencies.
  - Basic frontend validation and user messages were added to `web/app.js`.

When you confirm you want to continue, I can:
- Move DB creds into `config/` and make the server read them.
- Convert the project to Maven or Gradle and add proper JSON dependency (Jackson/Gson) so the server code is cleaner.
