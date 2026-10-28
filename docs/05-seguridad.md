# 05 · Seguridad

Copiada de `core/security` de task-manager y ajustada a un TPV.

## 1. Cadena de filtros
```
Request → CORS → JwtAuthenticationFilter → RateLimitingFilter → authorizeHttpRequests → Controller
```

## 2. `SecurityConfig`
- `csrf` deshabilitado (stateless; token en header, sin cookies).
- `SessionCreationPolicy.STATELESS`.
- `permitAll`: `/auth/**`, `/api-docs/**`, `/actuator/health`. Resto `authenticated()`.
- Sin `@EnableMethodSecurity` ni `@PreAuthorize`: no hay roles; estar autenticado es estar autorizado salvo la propiedad de la venta.
- 401/403 delegados al `HandlerExceptionResolver` → mismo `ErrorResponseDTO`.
- `BCryptPasswordEncoder`.

## 3. JWT (doble token)
| Token | Vida | Uso | Clave |
|---|---|---|---|
| Access | 1 h | `Authorization: Bearer` | `JWT_SECRET` |
| Refresh | 7 días | solo `/auth/refresh` | `JWT_REFRESH_SECRET` |

Claims: `sub` (userId), `email`, `type` (`access`/`refresh`), `iat`, `exp`. El filtro exige `type=access`.

## 4. Autorización
- **Sin roles**: el único usuario del sistema es el cajero.
- **Propiedad**: una venta solo la puede consultar u operar el cajero que la abrió → `PermissionValidator.validateHasPermissions(sale.getCashierId())` en el service (403 si no).
- `AuthenticatedUserProvider` (puerto) + `SpringSecurityUserProvider` (adapter): la aplicación no conoce Spring Security.

## 5. Rate limiting (Bucket4j, por IP)
| Ámbito | Límite | Motivo |
|---|---|---|
| Global | 300 req/min | Una caja escanea mucho |
| `/auth/login` | 10 req/min | Fuerza bruta |
429 con `Retry-After` y `ErrorResponseDTO`. Configurable por `RATE_LIMIT_*`.

## 6. CORS
Orígenes por entorno vía `CORS_ALLOWED_ORIGINS`: `http://localhost:[*]` (local/docker), dominio de Vercel de dev (preview) y de prod. Métodos `GET, POST, PATCH, DELETE, OPTIONS`; headers `Authorization, Content-Type`; `maxAge` 3600.

## 7. Secretos
- Configuración en `application-<perfil>.properties`, como task-manager.
- `local` / `docker`: valores de desarrollo escritos en el propio archivo (BD local, secretos JWT de desarrollo, cajero demo).
- `dev` / `prod`: los valores sensibles son placeholders `${VARIABLE}` en `application-dev|prod.properties`, resueltos por Spring con las variables de entorno del servicio de Render. Nunca se commitean credenciales reales.
- GitHub Actions: secrets del repositorio (deploy hooks).
- Variables de Render: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `CORS_ALLOWED_ORIGINS`, `SEED_CASHIER_PASSWORD` (solo dev/prod).

## 8. Otras medidas
- Validación de toda entrada; regex en path variables.
- Login con mensaje genérico ("Invalid credentials.").
- Nunca se devuelven hashes, stacktraces ni SQL.
- El precio lo decide el servidor.
- Logs sin tokens ni contraseñas.
- Un cajero demo por entorno, con credenciales distintas en cada uno, documentadas en los README para que el evaluador pueda probar dev y prod.
